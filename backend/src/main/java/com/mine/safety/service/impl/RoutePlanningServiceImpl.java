package com.mine.safety.service.impl;

import com.mine.safety.entity.*;
import com.mine.safety.mapper.*;
import com.mine.safety.service.MonitoringPointService;
import com.mine.safety.service.RoutePlanningService;
import com.mine.safety.service.strategy.MultiFactorOptimizationStrategy;
import com.mine.safety.service.strategy.RouteOptimizationContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoutePlanningServiceImpl implements RoutePlanningService {
    
    private static final Logger logger = LoggerFactory.getLogger(RoutePlanningServiceImpl.class);
    
    private final RouteMapper routeMapper;
    private final RoutePointMapper routePointMapper;
    private final RouteVersionMapper routeVersionMapper;
    private final RouteChangeLogMapper routeChangeLogMapper;
    private final MonitoringPointService monitoringPointService;
    private final InspectionTaskMapper inspectionTaskMapper;
    private final MultiFactorOptimizationStrategy optimizationStrategy;
    private final ObjectMapper objectMapper;
    
    public RoutePlanningServiceImpl(RouteMapper routeMapper, 
                                     RoutePointMapper routePointMapper,
                                     RouteVersionMapper routeVersionMapper,
                                     RouteChangeLogMapper routeChangeLogMapper,
                                     MonitoringPointService monitoringPointService,
                                     InspectionTaskMapper inspectionTaskMapper,
                                     MultiFactorOptimizationStrategy optimizationStrategy) {
        this.routeMapper = routeMapper;
        this.routePointMapper = routePointMapper;
        this.routeVersionMapper = routeVersionMapper;
        this.routeChangeLogMapper = routeChangeLogMapper;
        this.monitoringPointService = monitoringPointService;
        this.inspectionTaskMapper = inspectionTaskMapper;
        this.optimizationStrategy = optimizationStrategy;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
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
    
    @Override
    public List<String> optimizeRouteMultiFactor(List<MonitoringPoint> points, String startPointCode,
                                                 String currentLocationCode, Double currentX, Double currentY,
                                                 List<String> completedPointCodes) {
        RouteOptimizationContext context = new RouteOptimizationContext();
        context.setAllPoints(points);
        context.setCompletedPointCodes(completedPointCodes != null ? completedPointCodes : new ArrayList<>());
        
        List<String> pendingCodes = new ArrayList<>();
        Map<String, MonitoringPoint> pointMap = new HashMap<>();
        for (MonitoringPoint point : points) {
            pointMap.put(point.getPointCode(), point);
            if (!context.getCompletedPointCodes().contains(point.getPointCode())) {
                pendingCodes.add(point.getPointCode());
            }
        }
        context.setPendingPointCodes(pendingCodes);
        context.setCurrentLocationCode(currentLocationCode);
        context.setCurrentX(currentX);
        context.setCurrentY(currentY);
        context.setCurrentTime(LocalDateTime.now());
        
        return optimizationStrategy.optimize(context);
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
        
        double x1 = p1.getX() != null ? p1.getX() : 0;
        double y1 = p1.getY() != null ? p1.getY() : 0;
        double x2 = p2.getX() != null ? p2.getX() : 0;
        double y2 = p2.getY() != null ? p2.getY() : 0;
        
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
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
        
        List<String> optimizedOrder = optimizeRouteMultiFactor(points, startPointCode, null, null, null, null);
        
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
            routePoint.setInspectionStatus("PENDING");
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
        
        createVersion(route, "INITIAL", "路线创建", "admin");
        
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
        
        List<RoutePoint> oldPoints = routePointMapper.selectByRouteCode(route.getRouteCode());
        Map<String, RoutePoint> oldPointMap = oldPoints.stream()
            .collect(Collectors.toMap(RoutePoint::getPointCode, p -> p));
        
        routePointMapper.deleteByRouteCode(route.getRouteCode());
        
        List<MonitoringPoint> points = new ArrayList<>();
        for (String code : pointCodes) {
            MonitoringPoint point = monitoringPointService.getPointByCode(code);
            if (point != null) {
                points.add(point);
            }
        }
        
        List<String> optimizedOrder = optimizeRouteMultiFactor(points, route.getStartPointCode(), null, null, null, null);
        
        route.setRouteName(routeName);
        route.setTotalPoints(optimizedOrder.size());
        route.setEndPointCode(optimizedOrder.get(optimizedOrder.size() - 1));
        route.setUpdateTime(LocalDateTime.now());
        
        double totalDistance = 0;
        double cumulativeDistance = 0;
        
        for (int i = 0; i < optimizedOrder.size(); i++) {
            String pointCode = optimizedOrder.get(i);
            MonitoringPoint point = monitoringPointService.getPointByCode(pointCode);
            RoutePoint original = oldPointMap.get(pointCode);
            
            RoutePoint routePoint = new RoutePoint();
            routePoint.setRouteId(route.getId());
            routePoint.setRouteCode(route.getRouteCode());
            routePoint.setPointId(point != null ? point.getId() : null);
            routePoint.setPointCode(pointCode);
            routePoint.setPointName(point != null ? point.getPointName() : "Unknown");
            routePoint.setSequence(i + 1);
            routePoint.setCreateTime(LocalDateTime.now());
            
            if (original != null) {
                routePoint.setInspectionStatus(original.getInspectionStatus());
                routePoint.setActualInspectionTime(original.getActualInspectionTime());
                routePoint.setInspector(original.getInspector());
            } else {
                routePoint.setInspectionStatus("PENDING");
            }
            
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
        
        createVersion(route, "MANUAL", "路线手动更新", "admin");
        
        return route;
    }
    
    @Override
    @Transactional
    public void deleteRoute(Long id) {
        Route route = routeMapper.selectById(id);
        if (route == null) {
            return;
        }
        
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<InspectionTask> taskWrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        taskWrapper.eq(InspectionTask::getRouteId, id);
        taskWrapper.in(InspectionTask::getStatus, "PENDING", "IN_PROGRESS");
        
        long incompleteTaskCount = inspectionTaskMapper.selectCount(taskWrapper);
        if (incompleteTaskCount > 0) {
            throw new IllegalStateException("该路线存在未完成的巡检任务，无法删除");
        }
        
        routePointMapper.deleteByRouteCode(route.getRouteCode());
        routeVersionMapper.delete(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RouteVersion>()
                .eq(RouteVersion::getRouteId, id));
        routeChangeLogMapper.delete(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RouteChangeLog>()
                .eq(RouteChangeLog::getRouteId, id));
        routeMapper.deleteById(id);
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
    
    @Override
    @Transactional
    public Route recalculateRoute(Long routeId, String reason, List<String> addPointCodes,
                                   List<String> removePointCodes, List<String> statusChangePointCodes) {
        Route route = routeMapper.selectById(routeId);
        if (route == null) {
            return null;
        }
        
        List<RoutePoint> existingPoints = routePointMapper.selectByRouteCode(route.getRouteCode());
        
        Map<String, RoutePoint> completedPointMap = existingPoints.stream()
            .filter(p -> "COMPLETED".equals(p.getInspectionStatus()))
            .collect(Collectors.toMap(RoutePoint::getPointCode, p -> p));
        List<String> completedPointCodes = new ArrayList<>(completedPointMap.keySet());
        
        List<String> pendingPointCodes = existingPoints.stream()
            .filter(p -> "PENDING".equals(p.getInspectionStatus()))
            .map(RoutePoint::getPointCode)
            .collect(Collectors.toList());
        
        Set<String> currentCodes = new HashSet<>(pendingPointCodes);
        if (addPointCodes != null) {
            currentCodes.addAll(addPointCodes);
        }
        if (removePointCodes != null) {
            currentCodes.removeAll(removePointCodes);
        }
        
        List<MonitoringPoint> points = new ArrayList<>();
        for (String code : currentCodes) {
            MonitoringPoint point = monitoringPointService.getPointByCode(code);
            if (point != null) {
                points.add(point);
            }
        }
        
        for (String code : completedPointCodes) {
            MonitoringPoint point = monitoringPointService.getPointByCode(code);
            if (point != null) {
                points.add(point);
            }
        }
        
        String lastCompletedCode = completedPointCodes.isEmpty() ? route.getStartPointCode() 
            : completedPointCodes.get(completedPointCodes.size() - 1);
        
        List<String> optimizedOrder = optimizeRouteMultiFactor(points, lastCompletedCode, 
            lastCompletedCode, null, null, completedPointCodes);
        
        routePointMapper.deleteByRouteCode(route.getRouteCode());
        
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
            
            if (completedPointMap.containsKey(pointCode)) {
                RoutePoint original = completedPointMap.get(pointCode);
                routePoint.setInspectionStatus("COMPLETED");
                routePoint.setActualInspectionTime(original.getActualInspectionTime());
                routePoint.setInspector(original.getInspector());
            } else if (removePointCodes != null && removePointCodes.contains(pointCode)) {
                routePoint.setInspectionStatus("SKIPPED");
            } else {
                routePoint.setInspectionStatus("PENDING");
            }
            
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
        
        route.setTotalPoints(optimizedOrder.size());
        route.setEndPointCode(optimizedOrder.get(optimizedOrder.size() - 1));
        route.setTotalDistance(Math.round(totalDistance * 100.0) / 100.0);
        route.setEstimatedTime((int) (totalDistance / 50 + 30));
        route.setUpdateTime(LocalDateTime.now());
        routeMapper.updateById(route);
        
        createVersion(route, "RECALCULATE", reason, "system");
        
        logger.info("【路线重新计算】路线ID: {}, 原因: {}, 新点数: {}", routeId, reason, optimizedOrder.size());
        
        return route;
    }
    
    @Override
    @Transactional
    public Route generateRecommendedRoute(Long routeId, String currentLocationCode,
                                          Double currentX, Double currentY) {
        Route route = routeMapper.selectById(routeId);
        if (route == null) {
            return null;
        }
        
        List<RoutePoint> existingPoints = routePointMapper.selectByRouteCode(route.getRouteCode());
        
        List<String> completedPointCodes = existingPoints.stream()
            .filter(p -> "COMPLETED".equals(p.getInspectionStatus()))
            .map(RoutePoint::getPointCode)
            .collect(Collectors.toList());
        
        List<String> pendingPointCodes = existingPoints.stream()
            .filter(p -> "PENDING".equals(p.getInspectionStatus()))
            .map(RoutePoint::getPointCode)
            .collect(Collectors.toList());
        
        List<MonitoringPoint> points = new ArrayList<>();
        for (String code : pendingPointCodes) {
            MonitoringPoint point = monitoringPointService.getPointByCode(code);
            if (point != null) {
                points.add(point);
            }
        }
        for (String code : completedPointCodes) {
            MonitoringPoint point = monitoringPointService.getPointByCode(code);
            if (point != null) {
                points.add(point);
            }
        }
        
        String lastCompletedCode = completedPointCodes.isEmpty() ? route.getStartPointCode() 
            : completedPointCodes.get(completedPointCodes.size() - 1);
        
        List<String> optimizedOrder = optimizeRouteMultiFactor(points, lastCompletedCode, 
            currentLocationCode, currentX, currentY, completedPointCodes);
        
        Route recommendedRoute = new Route();
        recommendedRoute.setRouteName(route.getRouteName() + "_RECOMMENDED");
        recommendedRoute.setRouteCode("R" + System.currentTimeMillis() + "_REC");
        recommendedRoute.setTotalPoints(optimizedOrder.size());
        recommendedRoute.setStartPointCode(lastCompletedCode);
        recommendedRoute.setEndPointCode(optimizedOrder.get(optimizedOrder.size() - 1));
        recommendedRoute.setStatus("RECOMMENDED");
        recommendedRoute.setCreator("system");
        recommendedRoute.setCreateTime(LocalDateTime.now());
        recommendedRoute.setUpdateTime(LocalDateTime.now());
        
        routeMapper.insert(recommendedRoute);
        
        double totalDistance = 0;
        double cumulativeDistance = 0;
        
        for (int i = 0; i < optimizedOrder.size(); i++) {
            String pointCode = optimizedOrder.get(i);
            MonitoringPoint point = monitoringPointService.getPointByCode(pointCode);
            
            RoutePoint routePoint = new RoutePoint();
            routePoint.setRouteId(recommendedRoute.getId());
            routePoint.setRouteCode(recommendedRoute.getRouteCode());
            routePoint.setPointId(point != null ? point.getId() : null);
            routePoint.setPointCode(pointCode);
            routePoint.setPointName(point != null ? point.getPointName() : "Unknown");
            routePoint.setSequence(i + 1);
            routePoint.setInspectionStatus(completedPointCodes.contains(pointCode) ? "COMPLETED" : "PENDING");
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
        
        recommendedRoute.setTotalDistance(Math.round(totalDistance * 100.0) / 100.0);
        recommendedRoute.setEstimatedTime((int) (totalDistance / 50 + 30));
        routeMapper.updateById(recommendedRoute);
        
        createVersion(recommendedRoute, "RECOMMEND", "系统推荐路线", "system");
        
        logger.info("【推荐路线生成】原路线ID: {}, 推荐路线ID: {}", routeId, recommendedRoute.getId());
        
        return recommendedRoute;
    }
    
    @Override
    public List<RouteVersion> getRouteVersions(Long routeId) {
        return routeVersionMapper.selectByRouteId(routeId);
    }
    
    @Override
    public RouteVersion getRouteVersionById(Long versionId) {
        return routeVersionMapper.selectById(versionId);
    }
    
    @Override
    public List<RouteChangeLog> getVersionChangeLogs(Long versionId) {
        return routeChangeLogMapper.selectByVersionId(versionId);
    }
    
    @Override
    public Map<String, Object> compareVersions(Long routeId, Integer version1, Integer version2) {
        Map<String, Object> result = new HashMap<>();
        
        RouteVersion v1 = routeVersionMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RouteVersion>()
                .eq(RouteVersion::getRouteId, routeId)
                .eq(RouteVersion::getVersionNumber, version1));
        
        RouteVersion v2 = routeVersionMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RouteVersion>()
                .eq(RouteVersion::getRouteId, routeId)
                .eq(RouteVersion::getVersionNumber, version2));
        
        result.put("version1", v1);
        result.put("version2", v2);
        
        if (v1 != null && v2 != null) {
            List<RouteChangeLog> logs = routeChangeLogMapper.selectByRouteId(routeId);
            List<RouteChangeLog> relevantLogs = logs.stream()
                .filter(log -> log.getVersionId().equals(v2.getId()))
                .collect(Collectors.toList());
            result.put("changes", relevantLogs);
            
            try {
                List<String> beforeOrder = v1.getBeforeSnapshot() != null 
                    ? objectMapper.readValue(v1.getBeforeSnapshot(), List.class) : new ArrayList<>();
                List<String> afterOrder = v2.getAfterSnapshot() != null 
                    ? objectMapper.readValue(v2.getAfterSnapshot(), List.class) : new ArrayList<>();
                
                List<String> added = afterOrder.stream()
                    .filter(code -> !beforeOrder.contains(code))
                    .collect(Collectors.toList());
                List<String> removed = beforeOrder.stream()
                    .filter(code -> !afterOrder.contains(code))
                    .collect(Collectors.toList());
                
                Map<String, Integer> positionChanges = new HashMap<>();
                for (int i = 0; i < afterOrder.size(); i++) {
                    String code = afterOrder.get(i);
                    int oldIndex = beforeOrder.indexOf(code);
                    if (oldIndex >= 0 && oldIndex != i) {
                        positionChanges.put(code, oldIndex);
                    }
                }
                
                result.put("addedPoints", added);
                result.put("removedPoints", removed);
                result.put("positionChanges", positionChanges);
            } catch (JsonProcessingException e) {
                logger.error("解析版本快照失败", e);
            }
        }
        
        return result;
    }
    
    @Override
    @Transactional
    public Route acceptRecommendedRoute(Long routeId, Long recommendedVersionId) {
        Route route = routeMapper.selectById(routeId);
        Route recommendedRoute = routeMapper.selectById(recommendedVersionId);
        
        if (route == null || recommendedRoute == null) {
            return null;
        }
        
        List<RoutePoint> recommendedPoints = routePointMapper.selectByRouteCode(recommendedRoute.getRouteCode());
        
        routePointMapper.deleteByRouteCode(route.getRouteCode());
        
        for (RoutePoint rp : recommendedPoints) {
            RoutePoint newRp = new RoutePoint();
            newRp.setRouteId(route.getId());
            newRp.setRouteCode(route.getRouteCode());
            newRp.setPointId(rp.getPointId());
            newRp.setPointCode(rp.getPointCode());
            newRp.setPointName(rp.getPointName());
            newRp.setSequence(rp.getSequence());
            newRp.setDistanceFromPrev(rp.getDistanceFromPrev());
            newRp.setCumulativeDistance(rp.getCumulativeDistance());
            newRp.setInspectionStatus(rp.getInspectionStatus());
            newRp.setActualInspectionTime(rp.getActualInspectionTime());
            newRp.setInspector(rp.getInspector());
            newRp.setCreateTime(LocalDateTime.now());
            routePointMapper.insert(newRp);
        }
        
        route.setTotalPoints(recommendedRoute.getTotalPoints());
        route.setTotalDistance(recommendedRoute.getTotalDistance());
        route.setEstimatedTime(recommendedRoute.getEstimatedTime());
        route.setEndPointCode(recommendedRoute.getEndPointCode());
        route.setUpdateTime(LocalDateTime.now());
        routeMapper.updateById(route);
        
        routeMapper.deleteById(recommendedVersionId);
        
        createVersion(route, "ACCEPT_RECOMMEND", "采纳推荐路线", "admin");
        
        logger.info("【采纳推荐路线】原路线ID: {}, 推荐路线ID: {}", routeId, recommendedVersionId);
        
        return route;
    }
    
    @Override
    @Transactional
    public List<String> assignPointsToInspectors(Long routeId, List<String> inspectors) {
        Route route = routeMapper.selectById(routeId);
        if (route == null || inspectors == null || inspectors.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<RoutePoint> pendingPoints = routePointMapper.selectByRouteCode(route.getRouteCode()).stream()
            .filter(p -> "PENDING".equals(p.getInspectionStatus()))
            .collect(Collectors.toList());
        
        int inspectorCount = inspectors.size();
        Map<String, List<String>> assignments = new HashMap<>();
        for (String inspector : inspectors) {
            assignments.put(inspector, new ArrayList<>());
        }
        
        for (int i = 0; i < pendingPoints.size(); i++) {
            String inspector = inspectors.get(i % inspectorCount);
            assignments.get(inspector).add(pendingPoints.get(i).getPointCode());
            
            RoutePoint rp = pendingPoints.get(i);
            rp.setInspector(inspector);
            routePointMapper.updateById(rp);
        }
        
        logger.info("【巡检人员分配】路线ID: {}, 巡检人员: {}, 分配点数: {}", routeId, inspectors, pendingPoints.size());
        
        return pendingPoints.stream().map(RoutePoint::getPointCode).collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void updatePointInspectionStatus(Long routeId, String pointCode, String status, String inspector) {
        Route route = routeMapper.selectById(routeId);
        if (route == null) {
            return;
        }
        
        RoutePoint routePoint = routePointMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RoutePoint>()
                .eq(RoutePoint::getRouteCode, route.getRouteCode())
                .eq(RoutePoint::getPointCode, pointCode));
        
        if (routePoint != null) {
            routePoint.setInspectionStatus(status);
            routePoint.setInspector(inspector);
            if ("COMPLETED".equals(status)) {
                routePoint.setActualInspectionTime(LocalDateTime.now());
            }
            routePointMapper.updateById(routePoint);
        }
    }
    
    @Override
    @Transactional
    public Route reorderRoutePoints(Long routeId, List<String> pointCodes) {
        Route route = routeMapper.selectById(routeId);
        if (route == null) {
            return null;
        }
        
        List<RoutePoint> existingPoints = routePointMapper.selectByRouteCode(route.getRouteCode());
        Map<String, RoutePoint> pointMap = existingPoints.stream()
            .collect(Collectors.toMap(RoutePoint::getPointCode, p -> p));
        
        routePointMapper.deleteByRouteCode(route.getRouteCode());
        
        double totalDistance = 0;
        double cumulativeDistance = 0;
        
        for (int i = 0; i < pointCodes.size(); i++) {
            String pointCode = pointCodes.get(i);
            MonitoringPoint point = monitoringPointService.getPointByCode(pointCode);
            RoutePoint original = pointMap.get(pointCode);
            
            RoutePoint routePoint = new RoutePoint();
            routePoint.setRouteId(route.getId());
            routePoint.setRouteCode(route.getRouteCode());
            routePoint.setPointId(point != null ? point.getId() : null);
            routePoint.setPointCode(pointCode);
            routePoint.setPointName(point != null ? point.getPointName() : "Unknown");
            routePoint.setSequence(i + 1);
            routePoint.setCreateTime(LocalDateTime.now());
            
            if (original != null) {
                routePoint.setInspectionStatus(original.getInspectionStatus());
                routePoint.setActualInspectionTime(original.getActualInspectionTime());
                routePoint.setInspector(original.getInspector());
            } else {
                routePoint.setInspectionStatus("PENDING");
            }
            
            if (i > 0) {
                MonitoringPoint prevPoint = monitoringPointService.getPointByCode(pointCodes.get(i - 1));
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
        
        route.setTotalPoints(pointCodes.size());
        route.setEndPointCode(pointCodes.get(pointCodes.size() - 1));
        route.setTotalDistance(Math.round(totalDistance * 100.0) / 100.0);
        route.setEstimatedTime((int) (totalDistance / 50 + 30));
        route.setUpdateTime(LocalDateTime.now());
        routeMapper.updateById(route);
        
        createVersion(route, "MANUAL_REORDER", "手动调整路线顺序", "admin");
        
        logger.info("【路线顺序调整】路线ID: {}, 新顺序: {}", routeId, pointCodes);
        
        return route;
    }
    
    private void createVersion(Route route, String changeType, String changeReason, String operator) {
        Integer maxVersion = routeVersionMapper.selectMaxVersionNumber(route.getId());
        Integer newVersion = maxVersion != null ? maxVersion + 1 : 1;
        
        String beforeSnapshot = null;
        if (maxVersion != null) {
            RouteVersion prevVersion = routeVersionMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RouteVersion>()
                    .eq(RouteVersion::getRouteId, route.getId())
                    .eq(RouteVersion::getVersionNumber, maxVersion));
            if (prevVersion != null) {
                beforeSnapshot = prevVersion.getAfterSnapshot();
            }
        }
        
        List<RoutePoint> currentPoints = routePointMapper.selectByRouteCode(route.getRouteCode());
        List<String> pointCodes = currentPoints.stream()
            .map(RoutePoint::getPointCode)
            .collect(Collectors.toList());
        
        String afterSnapshot = null;
        try {
            afterSnapshot = objectMapper.writeValueAsString(pointCodes);
        } catch (JsonProcessingException e) {
            logger.error("序列化路线快照失败", e);
        }
        
        RouteVersion version = new RouteVersion();
        version.setRouteId(route.getId());
        version.setRouteCode(route.getRouteCode());
        version.setVersionNumber(newVersion);
        version.setChangeReason(changeReason);
        version.setChangeType(changeType);
        version.setTotalPoints(route.getTotalPoints());
        version.setTotalDistance(route.getTotalDistance());
        version.setEstimatedTime(route.getEstimatedTime());
        version.setOperator(operator);
        version.setBeforeSnapshot(beforeSnapshot);
        version.setAfterSnapshot(afterSnapshot);
        version.setCreateTime(LocalDateTime.now());
        
        routeVersionMapper.insert(version);
        
        List<String> beforePointCodes = new ArrayList<>();
        if (beforeSnapshot != null) {
            try {
                beforePointCodes = objectMapper.readValue(beforeSnapshot, List.class);
            } catch (JsonProcessingException e) {
                logger.error("解析变更前快照失败", e);
            }
        }
        
        Map<String, RoutePoint> currentPointMap = new HashMap<>();
        List<RoutePoint> allPoints = routePointMapper.selectByRouteCode(route.getRouteCode());
        for (RoutePoint rp : allPoints) {
            currentPointMap.put(rp.getPointCode(), rp);
        }
        
        for (int i = 0; i < beforePointCodes.size(); i++) {
            String pointCode = beforePointCodes.get(i);
            if (!currentPointMap.containsKey(pointCode)) {
                MonitoringPoint mp = monitoringPointService.getPointByCode(pointCode);
                RouteChangeLog log = new RouteChangeLog();
                log.setVersionId(version.getId());
                log.setRouteId(route.getId());
                log.setPointCode(pointCode);
                log.setPointName(mp != null ? mp.getPointName() : pointCode);
                log.setChangeType("REMOVED");
                log.setOldSequence(i + 1);
                log.setNewSequence(null);
                log.setOperator(operator);
                log.setCreateTime(LocalDateTime.now());
                routeChangeLogMapper.insert(log);
            }
        }
        
        for (RoutePoint rp : allPoints) {
            int oldIndex = beforePointCodes.indexOf(rp.getPointCode());
            RouteChangeLog log = new RouteChangeLog();
            log.setVersionId(version.getId());
            log.setRouteId(route.getId());
            log.setPointCode(rp.getPointCode());
            log.setPointName(rp.getPointName());
            log.setOperator(operator);
            log.setCreateTime(LocalDateTime.now());
            
            if (oldIndex < 0) {
                log.setChangeType("ADDED");
                log.setOldSequence(null);
                log.setNewSequence(rp.getSequence());
            } else if (oldIndex + 1 != rp.getSequence()) {
                log.setChangeType("REORDERED");
                log.setOldSequence(oldIndex + 1);
                log.setNewSequence(rp.getSequence());
            } else {
                log.setChangeType("UNCHANGED");
                log.setOldSequence(rp.getSequence());
                log.setNewSequence(rp.getSequence());
            }
            
            routeChangeLogMapper.insert(log);
        }
    }
}