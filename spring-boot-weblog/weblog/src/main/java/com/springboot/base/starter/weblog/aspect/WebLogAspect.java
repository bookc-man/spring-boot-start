package com.springboot.base.starter.weblog.aspect;

import com.alibaba.fastjson.JSON;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class WebLogAspect {

    private final static Logger logger = LoggerFactory.getLogger(WebLogAspect.class);
    /**
     * 换行符
     */
    private static final String LINE_SEPARATOR = System.lineSeparator();

    /**
     * 以自定义 @WebLog 注解为切点
     */
    @Pointcut("(@annotation(org.springframework.web.bind.annotation.PostMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.GetMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.DeleteMapping)" +
            "|| @annotation(org.springframework.web.bind.annotation.PutMapping))" +
            "&& !@annotation(com.springboot.base.starter.weblog.annotation.IgnoreWebLog)")
    public void webLog() {
    }

    /**
     * 在切点之前织入
     *
     * @param joinPoint
     */
    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint) {
        // 开始打印请求日志
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        // 打印请求相关参数
        logger.info("========================================== Start ==========================================");
        // 打印请求 url
        logger.info("URL            : " + request.getRequestURL().toString());
        // 打印 Http method
        logger.info("HTTP Method    : " + request.getMethod());
        // 打印调用 controller 的全路径以及执行方法
        logger.info("Class Method   : " + joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
        // 打印请求的 IP
        logger.info("IP             : " + request.getRemoteAddr());
        // 打印请求入参
        logger.info("Request Args   : " + JSON.toJSONString(joinPoint.getArgs()));
    }

    /**
     * 在切点之后织入
     */
    @After("webLog()")
    public void doAfter() {
        // 接口结束后换行，方便分割查看
        logger.info("=========================================== End ===========================================" + LINE_SEPARATOR);
    }

    /**
     * 环绕
     *
     * @param proceedingJoinPoint
     * @return
     * @throws Throwable
     */
    @Around("webLog()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = proceedingJoinPoint.proceed();
        // 打印出参
        logger.info("Response Args  : " + JSON.toJSONString(result));
        // 执行耗时
        logger.info("Time-Consuming : " + (System.currentTimeMillis() - startTime) + "ms");
        return result;
    }
}

