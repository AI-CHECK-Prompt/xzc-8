
package com.mine.safety.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mine.safety.entity.MonitoringPoint;
import com.mine.safety.mapper.MonitoringPointMapper;
import com.mine.safety.service.MonitoringPointService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MonitoringPointServiceImpl extends ServiceImpl<MonitoringPointMapper, MonitoringPoint> implements MonitoringPointService {
    
    @Override
    public List<MonitoringPoint> getAllPoints() {
        return baseMapper.selectList(null);
    }
    
    @Override
    public MonitoringPoint getPointById(Long id) {
        return baseMapper.selectById(id);
    }
    
    @Override
    public MonitoringPoint getPointByCode(String pointCode) {
        LambdaQueryWrapper<MonitoringPoint> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MonitoringPoint::getPointCode, pointCode);
        return baseMapper.selectOne(wrapper);
    }
    
    @Override
    public MonitoringPoint createPoint(MonitoringPoint point) {
        point.setCreateTime(LocalDateTime.now());
        point.setUpdateTime(LocalDateTime.now());
        point.setStatus("NORMAL");
        baseMapper.insert(point);
        return point;
    }
    
    @Override
    public MonitoringPoint updatePoint(MonitoringPoint point) {
        point.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(point);
        return point;
    }
    
    @Override
    public void deletePoint(Long id) {
        baseMapper.deleteById(id);
    }
    
    @Override
    public void updatePointStatus(String pointCode, String status) {
        MonitoringPoint point = getPointByCode(pointCode);
        if (point != null) {
            point.setStatus(status);
            point.setUpdateTime(LocalDateTime.now());
            baseMapper.updateById(point);
        }
    }
}
