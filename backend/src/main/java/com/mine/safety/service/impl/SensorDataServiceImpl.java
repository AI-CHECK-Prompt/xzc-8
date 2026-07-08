
package com.mine.safety.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mine.safety.entity.SensorData;
import com.mine.safety.mapper.SensorDataMapper;
import com.mine.safety.service.SensorDataService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SensorDataServiceImpl extends ServiceImpl<SensorDataMapper, SensorData> implements SensorDataService {
    
    @Override
    public SensorData saveData(SensorData data) {
        data.setCreateTime(LocalDateTime.now());
        data.setCollectTime(LocalDateTime.now());
        baseMapper.insert(data);
        return data;
    }
    
    @Override
    public List<SensorData> getRecentData(String pointCode, int limit) {
        LambdaQueryWrapper<SensorData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SensorData::getPointCode, pointCode);
        wrapper.orderByDesc(SensorData::getCollectTime);
        wrapper.last("LIMIT " + limit);
        return baseMapper.selectList(wrapper);
    }
    
    @Override
    public List<SensorData> getDataByPoint(String pointCode) {
        LambdaQueryWrapper<SensorData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SensorData::getPointCode, pointCode);
        wrapper.orderByDesc(SensorData::getCollectTime);
        return baseMapper.selectList(wrapper);
    }
}
