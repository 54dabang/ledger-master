package com.ledger.framework.aspectj;

import com.alibaba.fastjson2.JSON;
import com.ledger.common.core.domain.entity.SysUser;
import com.ledger.common.core.domain.model.LoginUser;
import com.ledger.common.core.text.Convert;
import com.ledger.common.enums.BusinessStatus;
import com.ledger.common.enums.BusinessType;
import com.ledger.common.enums.OperatorType;
import com.ledger.common.utils.ExceptionUtil;
import com.ledger.common.utils.SecurityUtils;
import com.ledger.common.utils.ServletUtils;
import com.ledger.common.utils.StringUtils;
import com.ledger.common.utils.ip.IpUtils;
import com.ledger.framework.manager.AsyncManager;
import com.ledger.framework.manager.factory.AsyncFactory;
import com.ledger.system.domain.SysOperLog;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.NamedThreadLocal;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 接口超时监控切面
 * 监控所有 @RestController 注解的类，如果方法执行超过1秒，则记录超时日志
 *
 * @author ledger
 */
@Aspect
@Component
public class TimeoutMonitorAspect {

    private static final Logger log = LoggerFactory.getLogger(TimeoutMonitorAspect.class);
    private static final Logger timeoutLogger = LoggerFactory.getLogger("sys-timeout-log");

    /**
     * 超时阈值（毫秒），默认1秒
     */
    @Value("${timeout.threshold:1000}")
    private long timeoutThreshold;

    /**
     * 计算操作消耗时间
     */
    private static final ThreadLocal<Long> TIME_THREADLOCAL = new NamedThreadLocal<>("Cost Time");

    /**
     * 切入点：所有 @RestController 注解的类
     */
    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        TIME_THREADLOCAL.set(System.currentTimeMillis());

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();

        Object result = null;
        Exception ex = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            ex = e;
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - TIME_THREADLOCAL.get();
            
            // 判断是否超时
            if (costTime > timeoutThreshold) {
                handleTimeoutLog(joinPoint, method, costTime, ex, result);
            }
            
            TIME_THREADLOCAL.remove();
        }
    }

    /**
     * 处理超时日志
     */
    private void handleTimeoutLog(ProceedingJoinPoint joinPoint, Method method, 
                                  long costTime, Exception ex, Object jsonResult) {
        try {
            // 获取当前的用户
            LoginUser loginUser = SecurityUtils.getLoginUserWithoutEpx();

            // 创建操作日志对象
            SysOperLog operLog = new SysOperLog();
            operLog.setStatus(BusinessStatus.SUCCESS.ordinal());
            
            // 请求的IP地址
            String ip = IpUtils.getIpAddr();
            operLog.setOperIp(ip);
            
            // 请求的URL
            String url = ServletUtils.getRequest().getRequestURI();
            operLog.setOperUrl(StringUtils.substring(url, 0, 255));
            
            // 用户信息
            if (loginUser != null) {
                operLog.setOperName(loginUser.getUsername());
                SysUser currentUser = loginUser.getUser();
                if (StringUtils.isNotNull(currentUser) && StringUtils.isNotNull(currentUser.getDept())) {
                    operLog.setDeptName(currentUser.getDept().getDeptName());
                }
            }

            // 异常信息
            if (ex != null) {
                operLog.setStatus(BusinessStatus.FAIL.ordinal());
                operLog.setErrorMsg(StringUtils.substring(
                    Convert.toStr(ex.getMessage(), ExceptionUtil.getExceptionMessage(ex)), 
                    0, 2000
                ));
            }

            // 方法名称
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            operLog.setMethod(className + "." + methodName + "()");
            
            // 请求方式
            operLog.setRequestMethod(ServletUtils.getRequest().getMethod());
            
            // 设置业务类型为超时
            operLog.setBusinessType(BusinessType.OTHER.ordinal());
            
            // 设置标题为"操作超时"
            operLog.setTitle("操作超时");
            operLog.setOperatorType(OperatorType.MANAGE.ordinal());
            // 设置耗时
            operLog.setCostTime(costTime);
            
            // 设置请求参数
            try {
                String params = JSON.toJSONString(joinPoint.getArgs());
                operLog.setOperParam(StringUtils.substring(params, 0, 2000));
            } catch (Exception e) {
                log.error("获取请求参数失败", e);
            }
            
            // 设置响应结果
            if (StringUtils.isNotNull(jsonResult)) {
                try {
                    String responseJson = JSON.toJSONString(jsonResult);
                    operLog.setJsonResult(StringUtils.substring(responseJson, 0, 2000));
                } catch (Exception e) {
                    log.error("序列化响应结果失败", e);
                }
            }

            // 记录超时日志到文件
            logTimeoutToFile(operLog, ex);
            
            // 异步保存到数据库
            AsyncManager.me().execute(AsyncFactory.recordOper(operLog));
            
            log.warn("接口超时: {}.{}, 耗时: {}ms, 阈值: {}ms", 
                    className, methodName, costTime, timeoutThreshold);

        } catch (Exception exp) {
            log.error("记录超时日志时发生异常: {}", exp.getMessage());
            exp.printStackTrace();
        }
    }

    /**
     * 将超时日志记录到文件
     */
    private void logTimeoutToFile(SysOperLog operLog, Exception e) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("[TIMEOUT] module: ").append(operLog.getTitle()).append(", ");
            sb.append("user: ").append(operLog.getOperName()).append(", ");
            sb.append("IP: ").append(operLog.getOperIp()).append(", ");
            sb.append("URL: ").append(operLog.getOperUrl()).append(", ");
            sb.append("method: ").append(operLog.getMethod()).append(", ");
            sb.append("reqMethod: ").append(operLog.getRequestMethod()).append(", ");
            sb.append("params: ").append(operLog.getOperParam()).append(", ");
            sb.append("costTime: ").append(operLog.getCostTime()).append("ms");
            sb.append("(threshold: ").append(timeoutThreshold).append("ms)");

            if (e != null) {
                sb.append(", response: ").append(operLog.getJsonResult()).append(", ");
                sb.append(", error: ").append(operLog.getErrorMsg());
                timeoutLogger.error(sb.toString(), e);
            } else {
                sb.append(", response: ").append(operLog.getJsonResult());
                timeoutLogger.warn(sb.toString());
            }
        } catch (Exception ex) {
            log.error("记录超时日志到文件时发生异常: ", ex);
        }
    }
}
