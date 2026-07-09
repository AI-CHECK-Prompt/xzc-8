package com.mine.safety.service.strategy;

import com.mine.safety.entity.MonitoringPoint;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class RouteOptimizationContext {
    private List<MonitoringPoint> allPoints;
    
    private List<String> completedPointCodes;
    
    private List<String> pendingPointCodes;
    
    private String currentLocationCode;
    
    private Double currentX;
    
    private Double currentY;
    
    private LocalDateTime currentTime;
    
    private Map<String, Integer> priorityWeights;
    
    private Integer maxDwellTime;
    
    private String inspector;
}