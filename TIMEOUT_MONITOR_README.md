# 接口超时监控功能说明

## 功能概述

`TimeoutMonitorAspect` 是一个自动监控所有 REST 接口执行时间的切面类。当接口执行时间超过设定的阈值（默认1秒）时，会自动记录超时日志到数据库和文件。

## 核心特性

### 1. 自动监控
- 无需手动添加注解
- 自动拦截所有 `@RestController` 注解的类
- 对所有方法生效

### 2. 超时阈值
- 默认阈值：**1000毫秒（1秒）**
- 可在代码中修改 `TIMEOUT_THRESHOLD` 常量

### 3. 日志记录
超时时会记录以下信息：
- **业务类型**：`BusinessType.TIMEOUT`（枚举常量）
- **标题**：操作超时
- **用户信息**：操作用户名、部门名称
- **请求信息**：IP地址、URL、请求方法、请求参数
- **性能数据**：实际耗时、超时阈值
- **响应结果**：接口返回数据
- **异常信息**：如有异常，记录错误消息

### 4. 双重记录
- **文件日志**：使用 logger `sys-timeout-log` 记录到文件
- **数据库日志**：异步保存到 `sys_oper_log` 表

## 使用方式

### 无需任何配置

该切面已经自动生效，只要应用启动，就会自动监控所有 REST 接口。

### 示例场景

当一个接口执行超过1秒时：

```
2026-05-14 14:30:15 [http-nio-8080-exec-1] WARN  TimeoutMonitorAspect - 
接口超时: com.ledger.business.controller.ReimbursementController.submitReimbursement(), 
耗时: 1523ms, 阈值: 1000ms
```

同时在数据库中会插入一条记录：

```sql
INSERT INTO sys_oper_log (
    title,          -- '操作超时'
    business_type,  -- BusinessType.TIMEOUT.ordinal()
    oper_name,      -- 用户名
    oper_ip,        -- IP地址
    oper_url,       -- 请求URL
    method,         -- 完整方法名
    request_method, -- GET/POST等
    oper_param,     -- 请求参数
    json_result,    -- 响应结果
    status,         -- 0成功/1失败
    cost_time,      -- 实际耗时(ms)
    oper_time       -- 操作时间
) VALUES (...);
```

## 配置说明

### 1. 修改超时阈值

编辑 `TimeoutMonitorAspect.java`：

```java
private static final long TIMEOUT_THRESHOLD = 2000; // 改为2秒
```

或者改为可配置的方式：

```java
@Value("${timeout.threshold:1000}")
private long timeoutThreshold;
```

然后在 `application.yml` 中配置：

```yaml
timeout:
  threshold: 1000  # 超时阈值（毫秒）
```

### 2. 日志配置

已在 `logback.xml` 中添加了超时日志的配置：

```xml
<!-- 超时日志记录器 -->
<logger name="sys-timeout-log" level="warn" additivity="false">
    <appender-ref ref="file_all"/>
    <appender-ref ref="console"/>
</logger>
```

日志会输出到：
- 控制台
- `/Users/leixingbang/logs/sys-all.log`（根据 logback.xml 配置）

## 与 LogAspect 的区别

| 特性 | LogAspect | TimeoutMonitorAspect |
|------|-----------|---------------------|
| 触发条件 | 有 `@Log` 注解或所有 RestController | 所有 RestController 且执行超时 |
| 记录时机 | 所有请求 | 仅超时请求（>1秒） |
| 业务类型 | 根据注解配置 | `BusinessType.TIMEOUT` |
| 用途 | 操作审计 | 性能监控 |
| 是否需要注解 | 是（可选） | 否（自动生效） |

## 注意事项

1. **性能影响**：切面本身对性能影响极小，只在超时时才进行日志记录
2. **异步保存**：数据库保存采用异步方式，不会影响主业务流程
3. **空指针安全**：已处理未登录用户的情况（`getLoginUserWithoutEpx()` 返回 null）
4. **异常安全**：即使日志记录失败，也不会影响接口的正常执行

## 排查超时问题

当发现超时日志时，可以：

1. 查看日志中的 `costTime` 字段，了解实际耗时
2. 查看 `method` 字段，定位具体的接口方法
3. 查看 `operParam` 字段，分析请求参数是否有问题
4. 查看 `jsonResult` 字段，了解响应数据大小
5. 结合代码分析接口性能瓶颈

## 字典配置

在 `BusinessType` 枚举中已添加 `TIMEOUT` 常量：

```java
public enum BusinessType {
    OTHER,
    INSERT,
    UPDATE,
    DELETE,
    GRANT,
    EXPORT,
    IMPORT,
    FORCE,
    GENCODE,
    CLEAN,
    TIMEOUT,  // 操作超时
}
```

使用时通过 `BusinessType.TIMEOUT.ordinal()` 获取对应的整数值。

确保数据库中有以下字典配置（已在 SQL 脚本中添加）：

```sql
INSERT INTO sys_dict_data VALUES(
    30, 1, '操作超时', '9', 'sys_oper_type', 
    '', 'danger', 'N', '0', 'admin', 
    sysdate(), '', NULL, '操作超时'
);
```

这样在前端展示时，业务类型 9 会显示为 "操作超时"。

## 总结

TimeoutMonitorAspect 提供了一个零配置、自动化的接口性能监控方案，帮助快速发现和定位慢接口问题，是系统性能优化的重要工具。
