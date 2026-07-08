
package com.mine.safety.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mine.safety.entity.MonitoringPoint;

import java.util.List;

public interface MonitoringPointService extends IService<MonitoringPoint> {
    List<MonitoringPoint> getAllPoints();
    
    MonitoringPoint getPointById(Long id);
    
    MonitoringPoint getPointByCode(String pointCode);
    
    MonitoringPoint createPoint(MonitoringPoint point);
    
    MonitoringPoint updatePoint(MonitoringPoint point);
    
    void deletePoint(Long id);
    
    void updatePointStatus(String pointCode, String status);
}
