
package com.mine.safety.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mine.safety.entity.InspectionTask;
import com.mine.safety.entity.Route;
import com.mine.safety.entity.RoutePoint;
import com.mine.safety.mapper.InspectionTaskMapper;
import com.mine.safety.mapper.RouteMapper;
import com.mine.safety.mapper.RoutePointMapper;
import com.mine.safety.service.InspectionTaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class InspectionTaskServiceImpl extends ServiceImpl<InspectionTaskMapper, InspectionTask> implements InspectionTaskService {
    
    private final RouteMapper routeMapper;
    private final RoutePointMapper routePointMapper;
    
    public InspectionTaskServiceImpl(RouteMapper routeMapper, RoutePointMapper routePointMapper) {
        this.routeMapper = routeMapper;
        this.routePointMapper = routePointMapper;
    }
    
    @Override
    @Transactional
    public InspectionTask createTask(Long routeId, String assignee, String scheduledStartTime, String scheduledEndTime) {
        Route route = routeMapper.selectById(routeId);
        if (route == null) {
            return null;
        }
        
        InspectionTask task = new InspectionTask();
        task.setTaskCode("T" + System.currentTimeMillis());
        task.setRouteId(routeId);
        task.setRouteCode(route.getRouteCode());
        task.setRouteName(route.getRouteName());
        task.setAssignee(assignee);
        task.setStatus("PENDING");
        task.setTotalPoints(route.getTotalPoints());
        task.setCompletedPoints(0);
        task.setTraveledDistance(0.0);
        task.setSavedDistance(0.0);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (scheduledStartTime != null) {
            task.setScheduledStartTime(LocalDateTime.parse(scheduledStartTime, formatter));
        }
        if (scheduledEndTime != null) {
            task.setScheduledEndTime(LocalDateTime.parse(scheduledEndTime, formatter));
        }
        
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        
        baseMapper.insert(task);
        
        return task;
    }
    
    @Override
    @Transactional
    public InspectionTask startTask(Long id) {
        InspectionTask task = baseMapper.selectById(id);
        if (task != null && "PENDING".equals(task.getStatus())) {
            task.setStatus("IN_PROGRESS");
            task.setActualStartTime(LocalDateTime.now());
            task.setUpdateTime(LocalDateTime.now());
            baseMapper.updateById(task);
        }
        return task;
    }
    
    @Override
    @Transactional
    public InspectionTask completeTask(Long id) {
        InspectionTask task = baseMapper.selectById(id);
        if (task != null && "IN_PROGRESS".equals(task.getStatus())) {
            task.setStatus("COMPLETED");
            task.setActualEndTime(LocalDateTime.now());
            task.setCompletedPoints(task.getTotalPoints());
            task.setSavedDistance(calculateSavedDistance(id));
            task.setUpdateTime(LocalDateTime.now());
            baseMapper.updateById(task);
        }
        return task;
    }
    
    @Override
    @Transactional
    public InspectionTask updateTaskProgress(Long id, Integer completedPoints, Double traveledDistance) {
        InspectionTask task = baseMapper.selectById(id);
        if (task != null && "IN_PROGRESS".equals(task.getStatus())) {
            task.setCompletedPoints(completedPoints);
            task.setTraveledDistance(traveledDistance);
            task.setUpdateTime(LocalDateTime.now());
            baseMapper.updateById(task);
        }
        return task;
    }
    
    @Override
    public List<InspectionTask> getTasksByAssignee(String assignee) {
        LambdaQueryWrapper<InspectionTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InspectionTask::getAssignee, assignee);
        wrapper.orderByDesc(InspectionTask::getCreateTime);
        return baseMapper.selectList(wrapper);
    }
    
    @Override
    public List<InspectionTask> getTasksByStatus(String status) {
        LambdaQueryWrapper<InspectionTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InspectionTask::getStatus, status);
        wrapper.orderByDesc(InspectionTask::getCreateTime);
        return baseMapper.selectList(wrapper);
    }
    
    @Override
    public List<RoutePoint> getRoutePoints(Long routeId) {
        Route route = routeMapper.selectById(routeId);
        if (route == null) {
            return new ArrayList<>();
        }
        return routePointMapper.selectByRouteCode(route.getRouteCode());
    }
    
    @Override
    public List<InspectionTask> getTasksForStatistics() {
        return baseMapper.selectList(null);
    }
    
    @Override
    public double calculateSavedDistance(Long taskId) {
        InspectionTask task = baseMapper.selectById(taskId);
        if (task == null) return 0.0;
        
        Route route = routeMapper.selectById(task.getRouteId());
        if (route == null) return 0.0;
        
        double optimizedDistance = route.getTotalDistance();
        double naiveDistance = optimizedDistance * 1.5;
        
        return Math.round((naiveDistance - optimizedDistance) * 100.0) / 100.0;
    }
}
