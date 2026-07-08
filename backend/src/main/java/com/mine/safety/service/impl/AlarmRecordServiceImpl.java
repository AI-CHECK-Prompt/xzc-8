
package com.mine.safety.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mine.safety.entity.AlarmRecord;
import com.mine.safety.mapper.AlarmRecordMapper;
import com.mine.safety.service.AlarmRecordService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlarmRecordServiceImpl extends ServiceImpl<AlarmRecordMapper, AlarmRecord> implements AlarmRecordService {
    
    @Override
    public AlarmRecord createAlarm(AlarmRecord record) {
        record.setStatus("UNHANDLED");
        record.setTriggerTime(LocalDateTime.now());
        record.setCreateTime(LocalDateTime.now());
        baseMapper.insert(record);
        return record;
    }
    
    @Override
    public List<AlarmRecord> getRecentAlarms(int limit) {
        LambdaQueryWrapper<AlarmRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(AlarmRecord::getTriggerTime);
        wrapper.last("LIMIT " + limit);
        return baseMapper.selectList(wrapper);
    }
    
    @Override
    public List<AlarmRecord> getAlarmsByPoint(String pointCode) {
        LambdaQueryWrapper<AlarmRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlarmRecord::getPointCode, pointCode);
        wrapper.orderByDesc(AlarmRecord::getTriggerTime);
        return baseMapper.selectList(wrapper);
    }
    
    @Override
    public List<AlarmRecord> getUnHandledAlarms() {
        LambdaQueryWrapper<AlarmRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlarmRecord::getStatus, "UNHANDLED");
        wrapper.orderByDesc(AlarmRecord::getTriggerTime);
        return baseMapper.selectList(wrapper);
    }
    
    @Override
    public AlarmRecord handleAlarm(Long id, String user, String result) {
        AlarmRecord record = baseMapper.selectById(id);
        if (record != null) {
            record.setStatus("HANDLED");
            record.setHandleTime(LocalDateTime.now());
            record.setHandleUser(user);
            record.setHandleResult(result);
            baseMapper.updateById(record);
        }
        return record;
    }
}
