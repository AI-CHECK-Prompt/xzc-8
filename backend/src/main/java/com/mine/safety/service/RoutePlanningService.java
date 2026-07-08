
package com.mine.safety.service;

import com.mine.safety.entity.MonitoringPoint;
import com.mine.safety.entity.Route;

import java.util.List;

public interface RoutePlanningService {
    List<String> optimizeRoute(List<MonitoringPoint> points, String startPointCode);
    
    List<String> optimizeRouteWithEnd(List<MonitoringPoint> points, String startPointCode, String endPointCode);
    
    double calculateDistance(MonitoringPoint p1, MonitoringPoint p2);
    
    double calculateTotalDistance(List<MonitoringPoint> points, List<String> order);
    
    Route createRoute(String routeName, List<String> pointCodes, String startPointCode);
    
    Route updateRoute(Long id, String routeName, List<String> pointCodes);
    
    void deleteRoute(Long id);
    
    Route getRouteById(Long id);
    
    Route getRouteByCode(String routeCode);
    
    List<Route> getAllRoutes();
}
