
package com.mine.safety.service;

import com.mine.safety.entity.SensorData;

public interface AlarmEngineService {
    void checkAndTriggerAlarm(SensorData data);
}
