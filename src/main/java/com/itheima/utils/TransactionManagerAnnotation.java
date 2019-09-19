package com.itheima.utils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 和事务管理相关的工具类，它包含了，开启事务，提交事务，回滚事务和释放连接
 */
@Component("transaction")
@Aspect
public class TransactionManagerAnnotation {

    @Autowired
    private ConnectionUtils connectionUtils;

    public void setConnectionUtils(ConnectionUtils connectionUtils) {
        this.connectionUtils = connectionUtils;
    }

    @Pointcut("execution(* com.itheima.service.impl.*.*(..))")
    private void pt1() {}


    /**
     * 开启事务
     */
    public  void beginTransaction(){
        try {
            connectionUtils.getThreadConnection().setAutoCommit(false);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 提交事务
     */
    public  void commit(){
        try {
            connectionUtils.getThreadConnection().commit();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 回滚事务
     */
    public  void rollback(){
        try {
            connectionUtils.getThreadConnection().rollback();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * 释放连接
     */
    public  void release(){
        try {
            connectionUtils.getThreadConnection().close();//还回连接池中
            connectionUtils.removeConnection();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 环绕通知  注解时指定顺序
     * @param pjp
     * @return
     */
   //@Around("execution(* com.itheima.service.impl.*.*(..))")
    @Around("pt1()")
    public Object transactionAround(ProceedingJoinPoint pjp) {
                    //定义返回值
                Object rtValue = null;
                try {
                    //获取方法执行所需的参数
                    Object[] args = pjp.getArgs();
                    //前置通知：开启事务
                    beginTransaction();
                    //执行方法
                    rtValue = pjp.proceed(args);
                    //后置通知：提交事务
                    commit();
                }catch(Throwable e) {
                     //异常通知：回滚事务
                    rollback();
                    e.printStackTrace();
                }finally {
                     //最终通知：释放资源
                    release();
                }
                return rtValue;
    }
}
