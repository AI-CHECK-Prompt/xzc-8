
package com.mine.safety.service;

import com.mine.safety.entity.AlarmRecord;
import com.mine.safety.entity.SensorData;

public interface WebSocketService {
    void sendSensorData(SensorData data);
    
    void sendAlarm(AlarmRecord record);
    
    void sendPointStatus(String pointCode, String status);
}
