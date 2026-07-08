
package com.mine.safety.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mine.safety.entity.AlarmRecord;

import java.util.List;

public interface AlarmRecordService extends IService<AlarmRecord> {
    AlarmRecord createAlarm(AlarmRecord record);
    
    List<AlarmRecord> getRecentAlarms(int limit);
    
    List<AlarmRecord> getAlarmsByPoint(String pointCode);
    
    List<AlarmRecord> getUnHandledAlarms();
    
    AlarmRecord handleAlarm(Long id, String user, String result);
}
