package com.mine.safety.service.strategy;

import com.mine.safety.entity.MonitoringPoint;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class MultiFactorOptimizationStrategy implements RouteOptimizationStrategy {
    
    private static final double PRIORITY_WEIGHT = 10.0;
    private static final double DISTANCE_WEIGHT = 1.0;
    private static final double DWELL_TIME_WEIGHT = 0.5;
    private static final double TIME_WINDOW_WEIGHT = 5.0;
    
    @Override
    public List<String> optimize(RouteOptimizationContext context) {
        List<String> result = new ArrayList<>();
        if (context.getCompletedPointCodes() != null) {
            result.addAll(context.getCompletedPointCodes());
        }
        
        Map<String, MonitoringPoint> pointMap = new HashMap<>();
        for (MonitoringPoint point : context.getAllPoints()) {
            pointMap.put(point.getPointCode(), point);
        }
        
        Set<String> pendingSet = new HashSet<>();
        if (context.getPendingPointCodes() != null) {
            pendingSet.addAll(context.getPendingPointCodes());
        }
        
        String currentCode = determineCurrentLocation(context, pointMap);
        
        if (!result.isEmpty()) {
            currentCode = result.get(result.size() - 1);
        }
        
        while (!pendingSet.isEmpty()) {
            String nextCode = findBestNextPoint(context, currentCode, pendingSet, pointMap);
            if (nextCode == null) {
                break;
            }
            result.add(nextCode);
            pendingSet.remove(nextCode);
            currentCode = nextCode;
        }
        
        return result;
    }
    
    private String determineCurrentLocation(RouteOptimizationContext context, Map<String, MonitoringPoint> pointMap) {
        if (context.getCurrentLocationCode() != null && pointMap.containsKey(context.getCurrentLocationCode())) {
            return context.getCurrentLocationCode();
        }
        if (context.getCurrentX() != null && context.getCurrentY() != null) {
            double minDist = Double.MAX_VALUE;
            String closest = null;
            for (Map.Entry<String, MonitoringPoint> entry : pointMap.entrySet()) {
                MonitoringPoint p = entry.getValue();
                if (p.getX() != null && p.getY() != null) {
                    double dist = calculateDistance(context.getCurrentX(), context.getCurrentY(), p.getX(), p.getY());
                    if (dist < minDist) {
                        minDist = dist;
                        closest = entry.getKey();
                    }
                }
            }
            return closest;
        }
        return context.getPendingPointCodes() != null && !context.getPendingPointCodes().isEmpty() 
            ? context.getPendingPointCodes().get(0) : null;
    }
    
    private String findBestNextPoint(RouteOptimizationContext context, String currentCode, 
                                     Set<String> candidates, Map<String, MonitoringPoint> pointMap) {
        String best = null;
        double minCost = Double.MAX_VALUE;
        
        for (String candidate : candidates) {
            double cost = calculateCost(context, currentCode, candidate);
            if (cost < minCost) {
                minCost = cost;
                best = candidate;
            }
        }
        
        return best;
    }
    
    @Override
    public double calculateCost(RouteOptimizationContext context, String currentCode, String nextCode) {
        Map<String, MonitoringPoint> pointMap = new HashMap<>();
        for (MonitoringPoint point : context.getAllPoints()) {
            pointMap.put(point.getPointCode(), point);
        }
        
        MonitoringPoint current = pointMap.get(currentCode);
        MonitoringPoint next = pointMap.get(nextCode);
        
        if (current == null || next == null) {
            return Double.MAX_VALUE;
        }
        
        double distanceCost = calculateDistanceCost(current, next);
        double priorityCost = calculatePriorityCost(next);
        double dwellTimeCost = calculateDwellTimeCost(next);
        double timeWindowCost = calculateTimeWindowCost(context, next);
        
        return DISTANCE_WEIGHT * distanceCost + 
               PRIORITY_WEIGHT * priorityCost + 
               DWELL_TIME_WEIGHT * dwellTimeCost + 
               TIME_WINDOW_WEIGHT * timeWindowCost;
    }
    
    private double calculateDistanceCost(MonitoringPoint current, MonitoringPoint next) {
        double x1 = current.getX() != null ? current.getX() : 0;
        double y1 = current.getY() != null ? current.getY() : 0;
        double x2 = next.getX() != null ? next.getX() : 0;
        double y2 = next.getY() != null ? next.getY() : 0;
        
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
    
    private double calculateDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
    
    private double calculatePriorityCost(MonitoringPoint point) {
        int priority = point.getPriority() != null ? point.getPriority() : 3;
        return (5 - priority) * 0.2;
    }
    
    private double calculateDwellTimeCost(MonitoringPoint point) {
        int dwellTime = point.getEstimatedDwellTime() != null ? point.getEstimatedDwellTime() : 5;
        return dwellTime / 60.0;
    }
    
    private double calculateTimeWindowCost(RouteOptimizationContext context, MonitoringPoint point) {
        if (point.getOpenStartTime() == null || point.getOpenEndTime() == null) {
            return 0;
        }
        
        LocalDateTime now = context.getCurrentTime() != null ? context.getCurrentTime() : LocalDateTime.now();
        LocalDateTime openStart = point.getOpenStartTime();
        LocalDateTime openEnd = point.getOpenEndTime();
        
        if (now.isAfter(openStart) && now.isBefore(openEnd)) {
            return 0;
        }
        
        if (now.isBefore(openStart)) {
            long minutes = Duration.between(now, openStart).toMinutes();
            return minutes / 60.0;
        }
        
        return Double.MAX_VALUE;
    }
}