
package com.mine.safety.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mine.safety.entity.SensorData;

import java.util.List;

public interface SensorDataService extends IService<SensorData> {
    SensorData saveData(SensorData data);
    
    List<SensorData> getRecentData(String pointCode, int limit);
    
    List<SensorData> getDataByPoint(String pointCode);
}
