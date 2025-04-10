package com.zw.okai.scoring;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 注解, 用于标注评分策略
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface ScoringStrategyConfig {

    /**
     * 应用类型,方法返回int
     * @return
     */
    int appType();

    /**
     * 评分策略
     * @return
     */
    int scoringStrategy();
}
