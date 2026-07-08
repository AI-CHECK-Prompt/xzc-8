package com.mine.safety.controller;

import com.mine.safety.entity.InspectionTask;
import com.mine.safety.entity.RoutePoint;
import com.mine.safety.service.InspectionTaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/task")
@CrossOrigin(origins = "*")
public class InspectionTaskController {
    
    private final InspectionTaskService inspectionTaskService;
    
    public InspectionTaskController(InspectionTaskService inspectionTaskService) {
        this.inspectionTaskService = inspectionTaskService;
    }
    
    @GetMapping
    public ResponseEntity<List<InspectionTask>> getAllTasks() {
        return ResponseEntity.ok(inspectionTaskService.getTasksForStatistics());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<InspectionTask> getTaskById(@PathVariable Long id) {
        InspectionTask task = inspectionTaskService.getById(id);
        return task != null ? ResponseEntity.ok(task) : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/assignee/{assignee}")
    public ResponseEntity<List<InspectionTask>> getTasksByAssignee(@PathVariable String assignee) {
        return ResponseEntity.ok(inspectionTaskService.getTasksByAssignee(assignee));
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<InspectionTask>> getTasksByStatus(@PathVariable String status) {
        return ResponseEntity.ok(inspectionTaskService.getTasksByStatus(status));
    }
    
    @GetMapping("/{id}/route-points")
    public ResponseEntity<List<RoutePoint>> getTaskRoutePoints(@PathVariable Long id) {
        InspectionTask task = inspectionTaskService.getById(id);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(inspectionTaskService.getRoutePoints(task.getRouteId()));
    }
    
    @PostMapping
    public ResponseEntity<InspectionTask> createTask(@RequestBody Map<String, Object> body) {
        Long routeId = Long.parseLong(body.get("routeId").toString());
        String assignee = (String) body.get("assignee");
        String scheduledStartTime = (String) body.get("scheduledStartTime");
        String scheduledEndTime = (String) body.get("scheduledEndTime");
        
        InspectionTask task = inspectionTaskService.createTask(routeId, assignee, scheduledStartTime, scheduledEndTime);
        return task != null ? ResponseEntity.ok(task) : ResponseEntity.badRequest().build();
    }
    
    @PutMapping("/{id}/start")
    public ResponseEntity<InspectionTask> startTask(@PathVariable Long id) {
        InspectionTask task = inspectionTaskService.startTask(id);
        return task != null ? ResponseEntity.ok(task) : ResponseEntity.notFound().build();
    }
    
    @PutMapping("/{id}/complete")
    public ResponseEntity<InspectionTask> completeTask(@PathVariable Long id) {
        InspectionTask task = inspectionTaskService.completeTask(id);
        return task != null ? ResponseEntity.ok(task) : ResponseEntity.notFound().build();
    }
    
    @PutMapping("/{id}/progress")
    public ResponseEntity<InspectionTask> updateProgress(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer completedPoints = (Integer) body.get("completedPoints");
        Double traveledDistance = (Double) body.get("traveledDistance");
        
        InspectionTask task = inspectionTaskService.updateTaskProgress(id, completedPoints, traveledDistance);
        return task != null ? ResponseEntity.ok(task) : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        List<InspectionTask> tasks = inspectionTaskService.getTasksForStatistics();
        
        long totalTasks = tasks.size();
        long completedTasks = tasks.stream().filter(t -> "COMPLETED".equals(t.getStatus())).count();
        long inProgressTasks = tasks.stream().filter(t -> "IN_PROGRESS".equals(t.getStatus())).count();
        long pendingTasks = tasks.stream().filter(t -> "PENDING".equals(t.getStatus())).count();
        
        double totalSavedDistance = tasks.stream().mapToDouble(InspectionTask::getSavedDistance).sum();
        double avgSavedDistance = totalTasks > 0 ? totalSavedDistance / totalTasks : 0;
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalTasks", totalTasks);
        result.put("completedTasks", completedTasks);
        result.put("inProgressTasks", inProgressTasks);
        result.put("pendingTasks", pendingTasks);
        result.put("totalSavedDistance", Math.round(totalSavedDistance * 100.0) / 100.0);
        result.put("avgSavedDistance", Math.round(avgSavedDistance * 100.0) / 100.0);
        
        return ResponseEntity.ok(result);
    }
}