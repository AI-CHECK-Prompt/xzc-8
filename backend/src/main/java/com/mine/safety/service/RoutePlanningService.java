
package com.mine.safety.service;

import com.mine.safety.entity.MonitoringPoint;
import com.mine.safety.entity.Route;
import com.mine.safety.entity.RouteChangeLog;
import com.mine.safety.entity.RouteVersion;

import java.util.List;
import java.util.Map;

public interface RoutePlanningService {
    List<String> optimizeRoute(List<MonitoringPoint> points, String startPointCode);
    
    List<String> optimizeRouteWithEnd(List<MonitoringPoint> points, String startPointCode, String endPointCode);
    
    List<String> optimizeRouteMultiFactor(List<MonitoringPoint> points, String startPointCode, 
                                          String currentLocationCode, Double currentX, Double currentY,
                                          List<String> completedPointCodes);
    
    double calculateDistance(MonitoringPoint p1, MonitoringPoint p2);
    
    double calculateTotalDistance(List<MonitoringPoint> points, List<String> order);
    
    Route createRoute(String routeName, List<String> pointCodes, String startPointCode);
    
    Route updateRoute(Long id, String routeName, List<String> pointCodes);
    
    void deleteRoute(Long id);
    
    Route getRouteById(Long id);
    
    Route getRouteByCode(String routeCode);
    
    List<Route> getAllRoutes();
    
    Route recalculateRoute(Long routeId, String reason, List<String> addPointCodes, 
                           List<String> removePointCodes, List<String> statusChangePointCodes);
    
    Route generateRecommendedRoute(Long routeId, String currentLocationCode, 
                                   Double currentX, Double currentY);
    
    List<RouteVersion> getRouteVersions(Long routeId);
    
    RouteVersion getRouteVersionById(Long versionId);
    
    List<RouteChangeLog> getVersionChangeLogs(Long versionId);
    
    Map<String, Object> compareVersions(Long routeId, Integer version1, Integer version2);
    
    Route acceptRecommendedRoute(Long routeId, Long recommendedVersionId);
    
    List<String> assignPointsToInspectors(Long routeId, List<String> inspectors);
    
    void updatePointInspectionStatus(Long routeId, String pointCode, String status, String inspector);
    
    Route reorderRoutePoints(Long routeId, List<String> pointCodes);
}
