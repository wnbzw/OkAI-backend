package com.zw.okai.manager;

import com.esotericsoftware.minlog.Log;
import com.zw.okai.common.ErrorCode;
import com.zw.okai.exception.BusinessException;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Redis 限流器,专门提供 RedisLimiter 限流基础服务
 */
@Service
public class RedisLimiterManager {

    @Resource
    private RedissonClient redissonClient;

    public void doRateLimit(String key) {
        // 获取分布式限流对象
        // 1. 获取分布式限流对象
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        redissonClient.getRateLimiter(key);
        Log.info("获取分布式限流对象",key);
        // 2. 执行限流操作
        rateLimiter.trySetRate(RateType.OVERALL,1,1, RateIntervalUnit.SECONDS);// 每秒最多2个请求
        // 3. 返回结果
        //一个操作请求一个令牌
        boolean b = rateLimiter.tryAcquire(1);
        Log.info("尝试获取令牌:"+b);
        if (!b) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST);
        }
        //原理: 令牌桶算法
    }
}
