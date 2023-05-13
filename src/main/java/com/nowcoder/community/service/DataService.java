package com.nowcoder.community.service;

import com.nowcoder.community.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class DataService {

    @Autowired
    private RedisTemplate redisTemplate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    // 将指定的ip计入Uv
    public void recordUv(String ip) {
        String redisKey = RedisUtil.getUvKey(dateFormat.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(redisKey, ip);
    }

    // 统计指定日期范围内的Uv
    public long calculateUv(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        // 整理该日期范围内的key
        List<String> list = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)) {
            String key = RedisUtil.getUvKey(dateFormat.format(calendar.getTime()));
            list.add(key);
            calendar.add(Calendar.DATE, 1);
        }
        // 合并数据
        String redisKey = RedisUtil.getUvKey(dateFormat.format(start), dateFormat.format(end));
        redisTemplate.opsForHyperLogLog().union(redisKey, list.toArray());
        // 返回统计的结果
        return redisTemplate.opsForHyperLogLog().size(redisKey);
    }

    // 将指定用户计入Dau
    public void recordDau(int userId) {
        String redisKey = RedisUtil.getDauKey(dateFormat.format(new Date()));
        redisTemplate.opsForValue().setBit(redisKey, userId, true);
    }

    // 统计指定日期范围内的Dau
    public long calculateDau(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        // 整理该日期范围内的key
        List<byte[]> list = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)) {
            String key = RedisUtil.getDauKey(dateFormat.format(calendar.getTime()));
            list.add(key.getBytes());
            calendar.add(Calendar.DATE, 1);
        }
        // 进行or运算
        return (long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                String redisKey = RedisUtil.getDauKey(dateFormat.format(start), dateFormat.format(end));
                redisConnection.bitOp(RedisStringCommands.BitOperation.OR,
                        redisKey.getBytes(), list.toArray(new byte[0][0]));
                return redisConnection.bitCount(redisKey.getBytes());
            }
        });
    }
}

