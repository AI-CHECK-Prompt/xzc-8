
package com.mine.safety.service.impl;

import com.mine.safety.entity.MonitoringPoint;
import com.mine.safety.entity.Route;
import com.mine.safety.entity.RoutePoint;
import com.mine.safety.mapper.RouteMapper;
import com.mine.safety.mapper.RoutePointMapper;
import com.mine.safety.service.MonitoringPointService;
import com.mine.safety.service.RoutePlanningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class RoutePlanningServiceImpl implements RoutePlanningService {
    
    private static final Logger logger = LoggerFactory.getLogger(RoutePlanningServiceImpl.class);
    
    private final RouteMapper routeMapper;
    private final RoutePointMapper routePointMapper;
    private final MonitoringPointService monitoringPointService;
    
    public RoutePlanningServiceImpl(RouteMapper routeMapper, 
                                     RoutePointMapper routePointMapper,
                                     MonitoringPointService monitoringPointService) {
        this.routeMapper = routeMapper;
        this.routePointMapper = routePointMapper;
        this.monitoringPointService = monitoringPointService;
    }
    
    @Override
    public List<String> optimizeRoute(List<MonitoringPoint> points, String startPointCode) {
        return optimizeRouteWithEnd(points, startPointCode, null);
    }
    
    @Override
    public List<String> optimizeRouteWithEnd(List<MonitoringPoint> points, String startPointCode, String endPointCode) {
        if (points == null || points.isEmpty()) {
            return new ArrayList<>();
        }
        
        Map<String, MonitoringPoint> pointMap = new HashMap<>();
        for (MonitoringPoint point : points) {
            pointMap.put(point.getPointCode(), point);
        }
        
        List<String> unvisited = new ArrayList<>();
        for (MonitoringPoint point : points) {
            if (!point.getPointCode().equals(startPointCode)) {
                unvisited.add(point.getPointCode());
            }
        }
        
        List<String> route = new ArrayList<>();
        route.add(startPointCode);
        
        String current = startPointCode;
        while (!unvisited.isEmpty()) {
            String nearest = findNearest(current, unvisited, pointMap);
            if (nearest != null) {
                route.add(nearest);
                unvisited.remove(nearest);
                current = nearest;
            } else {
                break;
            }
        }
        
        if (endPointCode != null && !endPointCode.equals(startPointCode) && !route.contains(endPointCode)) {
            int insertIndex = findOptimalInsertPosition(route, endPointCode, pointMap);
            route.add(insertIndex, endPointCode);
        }
        
        return route;
    }
    
    private String findNearest(String currentCode, List<String> candidates, Map<String, MonitoringPoint> pointMap) {
        MonitoringPoint current = pointMap.get(currentCode);
        if (current == null) return null;
        
        String nearest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (String candidate : candidates) {
            MonitoringPoint candidatePoint = pointMap.get(candidate);
            if (candidatePoint != null) {
                double distance = calculateDistance(current, candidatePoint);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = candidate;
                }
            }
        }
        
        return nearest;
    }
    
    private int findOptimalInsertPosition(List<String> route, String targetCode, Map<String, MonitoringPoint> pointMap) {
        int bestIndex = route.size();
        double minIncrease = Double.MAX_VALUE;
        
        for (int i = 0; i <= route.size(); i++) {
            String prevCode = i == 0 ? null : route.get(i - 1);
            String nextCode = i == route.size() ? null : route.get(i);
            
            double originalDistance = 0;
            if (prevCode != null && nextCode != null) {
                originalDistance = calculateDistance(pointMap.get(prevCode), pointMap.get(nextCode));
            }
            
            double newDistance = 0;
            if (prevCode != null) {
                newDistance += calculateDistance(pointMap.get(prevCode), pointMap.get(targetCode));
            }
            if (nextCode != null) {
                newDistance += calculateDistance(pointMap.get(targetCode), pointMap.get(nextCode));
            }
            
            double increase = newDistance - originalDistance;
            if (increase < minIncrease) {
                minIncrease = increase;
                bestIndex = i;
            }
        }
        
        return bestIndex;
    }
    
    @Override
    public double calculateDistance(MonitoringPoint p1, MonitoringPoint p2) {
        if (p1 == null || p2 == null) return Double.MAX_VALUE;
        
        String loc1 = p1.getLocation();
        String loc2 = p2.getLocation();
        
        try {
            String[] parts1 = loc1.replaceAll("[^0-9.-]", " ").trim().split("\\s+");
            String[] parts2 = loc2.replaceAll("[^0-9.-]", " ").trim().split("\\s+");
            
            if (parts1.length >= 2 && parts2.length >= 2) {
                double x1 = Double.parseDouble(parts1[0]);
                double y1 = Double.parseDouble(parts1[1]);
                double x2 = Double.parseDouble(parts2[0]);
                double y2 = Double.parseDouble(parts2[1]);
                
                return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
            }
        } catch (Exception e) {
            logger.warn("解析位置坐标失败: {}", e.getMessage());
        }
        
        return Math.random() * 100 + 50;
    }
    
    @Override
    public double calculateTotalDistance(List<MonitoringPoint> points, List<String> order) {
        Map<String, MonitoringPoint> pointMap = new HashMap<>();
        for (MonitoringPoint point : points) {
            pointMap.put(point.getPointCode(), point);
        }
        
        double total = 0;
        for (int i = 0; i < order.size() - 1; i++) {
            MonitoringPoint p1 = pointMap.get(order.get(i));
            MonitoringPoint p2 = pointMap.get(order.get(i + 1));
            total += calculateDistance(p1, p2);
        }
        
        return total;
    }
    
    @Override
    @Transactional
    public Route createRoute(String routeName, List<String> pointCodes, String startPointCode) {
        List<MonitoringPoint> points = new ArrayList<>();
        for (String code : pointCodes) {
            MonitoringPoint point = monitoringPointService.getPointByCode(code);
            if (point != null) {
                points.add(point);
            }
        }
        
        List<String> optimizedOrder = optimizeRoute(points, startPointCode);
        
        Route route = new Route();
        route.setRouteName(routeName);
        route.setRouteCode("R" + System.currentTimeMillis());
        route.setTotalPoints(optimizedOrder.size());
        route.setStartPointCode(startPointCode);
        route.setEndPointCode(optimizedOrder.get(optimizedOrder.size() - 1));
        route.setStatus("ACTIVE");
        route.setCreator("admin");
        route.setCreateTime(LocalDateTime.now());
        route.setUpdateTime(LocalDateTime.now());
        
        routeMapper.insert(route);
        
        double totalDistance = 0;
        double cumulativeDistance = 0;
        
        for (int i = 0; i < optimizedOrder.size(); i++) {
            String pointCode = optimizedOrder.get(i);
            MonitoringPoint point = monitoringPointService.getPointByCode(pointCode);
            
            RoutePoint routePoint = new RoutePoint();
            routePoint.setRouteId(route.getId());
            routePoint.setRouteCode(route.getRouteCode());
            routePoint.setPointId(point != null ? point.getId() : null);
            routePoint.setPointCode(pointCode);
            routePoint.setPointName(point != null ? point.getPointName() : "Unknown");
            routePoint.setSequence(i + 1);
            routePoint.setCreateTime(LocalDateTime.now());
            
            if (i > 0) {
                MonitoringPoint prevPoint = monitoringPointService.getPointByCode(optimizedOrder.get(i - 1));
                double distance = calculateDistance(prevPoint, point);
                routePoint.setDistanceFromPrev(distance);
                cumulativeDistance += distance;
                totalDistance += distance;
            } else {
                routePoint.setDistanceFromPrev(0.0);
            }
            routePoint.setCumulativeDistance(cumulativeDistance);
            
            routePointMapper.insert(routePoint);
        }
        
        route.setTotalDistance(Math.round(totalDistance * 100.0) / 100.0);
        route.setEstimatedTime((int) (totalDistance / 50 + 30));
        routeMapper.updateById(route);
        
        logger.info("【路线创建】路线: {}, 总点数: {}, 总距离: {}m", routeName, optimizedOrder.size(), totalDistance);
        
        return route;
    }
    
    @Override
    @Transactional
    public Route updateRoute(Long id, String routeName, List<String> pointCodes) {
        Route route = routeMapper.selectById(id);
        if (route == null) {
            return null;
        }
        
        routePointMapper.deleteByRouteCode(route.getRouteCode());
        
        List<MonitoringPoint> points = new ArrayList<>();
        for (String code : pointCodes) {
            MonitoringPoint point = monitoringPointService.getPointByCode(code);
            if (point != null) {
                points.add(point);
            }
        }
        
        List<String> optimizedOrder = optimizeRoute(points, route.getStartPointCode());
        
        route.setRouteName(routeName);
        route.setTotalPoints(optimizedOrder.size());
        route.setEndPointCode(optimizedOrder.get(optimizedOrder.size() - 1));
        route.setUpdateTime(LocalDateTime.now());
        
        double totalDistance = 0;
        double cumulativeDistance = 0;
        
        for (int i = 0; i < optimizedOrder.size(); i++) {
            String pointCode = optimizedOrder.get(i);
            MonitoringPoint point = monitoringPointService.getPointByCode(pointCode);
            
            RoutePoint routePoint = new RoutePoint();
            routePoint.setRouteId(route.getId());
            routePoint.setRouteCode(route.getRouteCode());
            routePoint.setPointId(point != null ? point.getId() : null);
            routePoint.setPointCode(pointCode);
            routePoint.setPointName(point != null ? point.getPointName() : "Unknown");
            routePoint.setSequence(i + 1);
            routePoint.setCreateTime(LocalDateTime.now());
            
            if (i > 0) {
                MonitoringPoint prevPoint = monitoringPointService.getPointByCode(optimizedOrder.get(i - 1));
                double distance = calculateDistance(prevPoint, point);
                routePoint.setDistanceFromPrev(distance);
                cumulativeDistance += distance;
                totalDistance += distance;
            } else {
                routePoint.setDistanceFromPrev(0.0);
            }
            routePoint.setCumulativeDistance(cumulativeDistance);
            
            routePointMapper.insert(routePoint);
        }
        
        route.setTotalDistance(Math.round(totalDistance * 100.0) / 100.0);
        route.setEstimatedTime((int) (totalDistance / 50 + 30));
        routeMapper.updateById(route);
        
        return route;
    }
    
    @Override
    @Transactional
    public void deleteRoute(Long id) {
        Route route = routeMapper.selectById(id);
        if (route != null) {
            routePointMapper.deleteByRouteCode(route.getRouteCode());
            routeMapper.deleteById(id);
        }
    }
    
    @Override
    public Route getRouteById(Long id) {
        return routeMapper.selectById(id);
    }
    
    @Override
    public Route getRouteByCode(String routeCode) {
        return routeMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Route>()
                .eq(Route::getRouteCode, routeCode));
    }
    
    @Override
    public List<Route> getAllRoutes() {
        return routeMapper.selectList(null);
    }
}
