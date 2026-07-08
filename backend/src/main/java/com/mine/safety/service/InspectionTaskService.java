
package com.mine.safety.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mine.safety.entity.InspectionTask;
import com.mine.safety.entity.RoutePoint;

import java.util.List;

public interface InspectionTaskService extends IService<InspectionTask> {
    InspectionTask createTask(Long routeId, String assignee, String scheduledStartTime, String scheduledEndTime);
    
    InspectionTask startTask(Long id);
    
    InspectionTask completeTask(Long id);
    
    InspectionTask updateTaskProgress(Long id, Integer completedPoints, Double traveledDistance);
    
    List<InspectionTask> getTasksByAssignee(String assignee);
    
    List<InspectionTask> getTasksByStatus(String status);
    
    List<RoutePoint> getRoutePoints(Long routeId);
    
    List<InspectionTask> getTasksForStatistics();
    
    double calculateSavedDistance(Long taskId);
}
