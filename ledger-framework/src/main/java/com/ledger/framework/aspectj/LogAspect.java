package com.ledger.framework.aspectj;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson2.JSON;
import com.ledger.common.annotation.Log;
import com.ledger.common.core.domain.entity.SysUser;
import com.ledger.common.core.domain.model.LoginUser;
import com.ledger.common.core.text.Convert;
import com.ledger.common.enums.BusinessStatus;
import com.ledger.common.enums.BusinessType;
import com.ledger.common.enums.HttpMethod;
import com.ledger.common.enums.OperatorType;
import com.ledger.common.filter.PropertyPreExcludeFilter;
import com.ledger.common.utils.*;
import com.ledger.common.utils.ip.IpUtils;
import com.ledger.framework.manager.AsyncManager;
import com.ledger.framework.manager.factory.AsyncFactory;
import com.ledger.system.domain.SysOperLog;

import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NamedThreadLocal;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 操作日志记录处理
 *
 * @author ledger
 */
@Aspect
@Component
public class LogAspect {

    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);
    private static final Logger operLogger = LoggerFactory.getLogger("sys-oper-log");

    /**
     * 排除敏感属性字段
     */
    public static final String[] EXCLUDE_PROPERTIES = {"password", "oldPassword", "newPassword", "confirmPassword"};

    /**
     * 计算操作消耗时间
     */
    private static final ThreadLocal<Long> TIME_THREADLOCAL = new NamedThreadLocal<>("Cost Time");

    /* ========================================================================================== */

    /**
     * 规则1：方法上有 @Log 注解
     */
    @Pointcut("@annotation(com.ledger.common.annotation.Log)")
    public void logAnnotationPointcut() {
    }

    /**
     * 规则2：包 com.ledger..* 下，类上有 @RestController 注解的所有方法
     */
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController com.ledger..*)")
    public void restControllerPointcut() {
    }

    /**
     * 合并两条规则
     */
    @Pointcut("logAnnotationPointcut() || restControllerPointcut()")
    public void logPointcut() {
    }

    /**
     * 环绕通知：记录请求/响应/异常
     */
    @Around("logPointcut()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        TIME_THREADLOCAL.set(System.currentTimeMillis());

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();

        // 1. 尝试从方法上拿 @Log
        Log logAnnotation = method.getAnnotation(Log.class);
        // 2. 拿不到再从类上拿
        if (logAnnotation == null) {
            logAnnotation = method.getDeclaringClass().getAnnotation(Log.class);
        }
        // 3. 还是拿不到，使用默认实例（避免后续 NPE）
        if (logAnnotation == null) {
            logAnnotation = DEFAULT_LOG;
        }

        Object result = null;
        Exception ex = null;
        try {
            result = joinPoint.proceed();
        } catch (Exception e) {
            ex = e;
            throw e;
        } finally {
            long cost = System.currentTimeMillis() - TIME_THREADLOCAL.get();
            handleLog(joinPoint, logAnnotation, ex, result);
            TIME_THREADLOCAL.remove();
        }
        return result;
    }

    /* ========================================================================================== */

    /**
     * 默认 @Log 实例，仅用于 restControllerPointcut 场景
     */
    private static final Log DEFAULT_LOG = new Log() {
        @Override
        public String title() { return ""; }

        @Override
        public BusinessType businessType() { return BusinessType.OTHER; }

        @Override
        public OperatorType operatorType() { return OperatorType.MANAGE; }

        @Override
        public boolean isSaveRequestData() { return true; }

        @Override
        public boolean isSaveResponseData() { return true; }

        @Override
        public String[] excludeParamNames() { return new String[0]; }

        @Override
        public Class<? extends java.lang.annotation.Annotation> annotationType() { return Log.class; }
    };

    /* ========================================================================================== */

    protected void handleLog(ProceedingJoinPoint joinPoint, Log controllerLog, final Exception e, Object jsonResult) {
        try {
            // 获取当前的用户
            LoginUser loginUser = SecurityUtils.getLoginUser();

            // *========数据库日志=========*//
            SysOperLog operLog = new SysOperLog();
            operLog.setStatus(BusinessStatus.SUCCESS.ordinal());
            // 请求的地址
            String ip = IpUtils.getIpAddr();
            operLog.setOperIp(ip);
            operLog.setOperUrl(StringUtils.substring(ServletUtils.getRequest().getRequestURI(), 0, 255));
            if (loginUser != null) {
                operLog.setOperName(loginUser.getUsername());
                SysUser currentUser = loginUser.getUser();
                if (StringUtils.isNotNull(currentUser) && StringUtils.isNotNull(currentUser.getDept())) {
                    operLog.setDeptName(currentUser.getDept().getDeptName());
                }
            }

            if (e != null) {
                operLog.setStatus(BusinessStatus.FAIL.ordinal());
                operLog.setErrorMsg(StringUtils.substring(Convert.toStr(e.getMessage(), ExceptionUtil.getExceptionMessage(e)), 0, 2000));
            }
            // 设置方法名称
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            operLog.setMethod(className + "." + methodName + "()");
            // 设置请求方式
            operLog.setRequestMethod(ServletUtils.getRequest().getMethod());
            // 处理设置注解上的参数
            getControllerMethodDescription(joinPoint, controllerLog, operLog, jsonResult);
            // 设置消耗时间
            operLog.setCostTime(System.currentTimeMillis() - TIME_THREADLOCAL.get());

            // 记录操作日志到文件
            logOperToFile(operLog, e);
            if(controllerLog != DEFAULT_LOG){
                // 保存数据库
                AsyncManager.me().execute(AsyncFactory.recordOper(operLog));
            }

        } catch (Exception exp) {
            // 记录本地异常日志
            log.error("异常信息:{}", exp.getMessage());
            exp.printStackTrace();
        }
    }

    /**
     * 将操作日志记录到文件
     */
    private void logOperToFile(SysOperLog operLog, Exception e) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("操作模块: ").append(operLog.getTitle()).append(", ");
            sb.append("操作人员: ").append(operLog.getOperName()).append(", ");
            sb.append("操作IP: ").append(operLog.getOperIp()).append(", ");
            sb.append("请求URL: ").append(operLog.getOperUrl()).append(", ");
            sb.append("请求方法: ").append(operLog.getMethod()).append(", ");
            sb.append("请求方式: ").append(operLog.getRequestMethod()).append(", ");
            sb.append("操作参数: ").append(operLog.getOperParam()).append(", ");
            sb.append("返回参数: ").append(operLog.getJsonResult()).append(", ");
            sb.append("消耗时间: ").append(operLog.getCostTime()).append("毫秒");

            if (e != null) {
                // 错误日志
                sb.append(", 错误信息: ").append(operLog.getErrorMsg());
                operLogger.error(sb.toString(), e);
            } else if (operLog.getStatus() != null && operLog.getStatus() == BusinessStatus.FAIL.ordinal()) {
                // 失败状态日志
                sb.append(", 错误信息: ").append(operLog.getErrorMsg());
                operLogger.warn(sb.toString());
            } else {
                // 正常日志
                operLogger.info(sb.toString());
            }
        } catch (Exception ex) {
            log.error("记录操作日志到文件时发生异常: ", ex);
        }
    }

    public void getControllerMethodDescription(ProceedingJoinPoint joinPoint, Log log, SysOperLog operLog, Object jsonResult) throws Exception {
        // 设置action动作
        operLog.setBusinessType(log.businessType().ordinal());
        // 设置标题
        operLog.setTitle(log.title());
        // 设置操作人类别
        operLog.setOperatorType(log.operatorType().ordinal());
        // 是否需要保存request，参数和值
        if (log.isSaveRequestData()) {
            // 获取参数的信息，传入到数据库中。
            setRequestValue(joinPoint, operLog, log.excludeParamNames());
        }
        // 是否需要保存response，参数和值
        if (log.isSaveResponseData() && StringUtils.isNotNull(jsonResult)) {
            operLog.setJsonResult(StringUtils.substring(JSON.toJSONString(jsonResult), 0, 2000));
        }
    }

    private void setRequestValue(ProceedingJoinPoint joinPoint, SysOperLog operLog, String[] excludeParamNames) throws Exception {
        Map<?, ?> paramsMap = ServletUtils.getParamMap(ServletUtils.getRequest());
        String requestMethod = operLog.getRequestMethod();
        if (StringUtils.isEmpty(paramsMap) && StringUtils.equalsAny(requestMethod, HttpMethod.PUT.name(), HttpMethod.POST.name(), HttpMethod.DELETE.name())) {
            String params = argsArrayToString(joinPoint.getArgs(), excludeParamNames);
            operLog.setOperParam(StringUtils.substring(params, 0, 2000));
        } else {
            operLog.setOperParam(StringUtils.substring(JSON.toJSONString(paramsMap, excludePropertyPreFilter(excludeParamNames)), 0, 2000));
        }
    }

    private String argsArrayToString(Object[] paramsArray, String[] excludeParamNames) {
        String params = "";
        if (paramsArray != null && paramsArray.length > 0) {
            for (Object o : paramsArray) {
                if (StringUtils.isNotNull(o) && !isFilterObject(o)) {
                    try {
                        String jsonObj = JSON.toJSONString(o, excludePropertyPreFilter(excludeParamNames));
                        params += jsonObj + " ";
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        return params.trim();
    }

    public PropertyPreExcludeFilter excludePropertyPreFilter(String[] excludeParamNames) {
        return new PropertyPreExcludeFilter().addExcludes(ArrayUtils.addAll(EXCLUDE_PROPERTIES, excludeParamNames));
    }

    public boolean isFilterObject(final Object o) {
        Class<?> clazz = o.getClass();
        if (clazz.isArray()) {
            return clazz.getComponentType().isAssignableFrom(MultipartFile.class);
        } else if (Collection.class.isAssignableFrom(clazz)) {
            Collection<?> collection = (Collection<?>) o;
            for (Object value : collection) {
                return value instanceof MultipartFile;
            }
        } else if (Map.class.isAssignableFrom(clazz)) {
            Map<?, ?> map = (Map<?, ?>) o;
            for (Object value : map.entrySet()) {
                Map.Entry<?, ?> entry = (Map.Entry<?, ?>) value;
                return entry.getValue() instanceof MultipartFile;
            }
        }
        return o instanceof MultipartFile || o instanceof HttpServletRequest || o instanceof HttpServletResponse
                || o instanceof BindingResult;
    }
}