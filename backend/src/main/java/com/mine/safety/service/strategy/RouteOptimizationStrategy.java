package com.mine.safety.service.strategy;

import com.mine.safety.entity.MonitoringPoint;

import java.util.List;

public interface RouteOptimizationStrategy {
    List<String> optimize(RouteOptimizationContext context);
    
    double calculateCost(RouteOptimizationContext context, String currentCode, String nextCode);
}