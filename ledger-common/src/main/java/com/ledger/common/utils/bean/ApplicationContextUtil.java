package com.ledger.common.utils.bean;



import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * 静态获取 Spring 容器中的 Bean
 *
 * 用法：
 *    FooService foo = ApplicationContextUtil.getBean("fooService");
 *    FooService foo = ApplicationContextUtil.getBean(FooService.class);
 */
@Component   // 必须让 Spring 扫描到
public class ApplicationContextUtil implements ApplicationContextAware {

    private static ApplicationContext CONTEXT;

    /**
     * Spring 容器启动时自动回调，把 ApplicationContext 传进来
     */
    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        CONTEXT = applicationContext;
    }

    /**
     * 根据 beanName 获取 Bean（需要强转）
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String beanName) {
        checkApplicationContext();
        return (T) CONTEXT.getBean(beanName);
    }

    /**
     * 根据类型获取 Bean
     */
    public static <T> T getBean(Class<T> clazz) {
        checkApplicationContext();
        return CONTEXT.getBean(clazz);
    }

    /**
     * 根据 beanName + 类型获取 Bean
     */
    public static <T> T getBean(String beanName, Class<T> clazz) {
        checkApplicationContext();
        return CONTEXT.getBean(beanName, clazz);
    }

    /**
     * 获取 Spring 环境变量、系统属性等
     */
    public static String getProperty(String key) {
        checkApplicationContext();
        return CONTEXT.getEnvironment().getProperty(key);
    }

    private static void checkApplicationContext() {
        if (CONTEXT == null) {
            throw new IllegalStateException("ApplicationContext 未注入，请确认 Spring 容器已启动且 ApplicationContextUtil 被 Spring 扫描到");
        }
    }
}