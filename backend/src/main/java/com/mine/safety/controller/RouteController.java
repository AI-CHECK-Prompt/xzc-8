
package com.mine.safety.controller;

import com.mine.safety.entity.Route;
import com.mine.safety.entity.RouteChangeLog;
import com.mine.safety.entity.RoutePoint;
import com.mine.safety.entity.RouteVersion;
import com.mine.safety.service.InspectionTaskService;
import com.mine.safety.service.RoutePlanningService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/route")
@CrossOrigin(origins = "*")
public class RouteController {
    
    private final RoutePlanningService routePlanningService;
    private final InspectionTaskService inspectionTaskService;
    
    public RouteController(RoutePlanningService routePlanningService, InspectionTaskService inspectionTaskService) {
        this.routePlanningService = routePlanningService;
        this.inspectionTaskService = inspectionTaskService;
    }
    
    @GetMapping
    public ResponseEntity<List<Route>> getAllRoutes() {
        return ResponseEntity.ok(routePlanningService.getAllRoutes());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Route> getRouteById(@PathVariable Long id) {
        Route route = routePlanningService.getRouteById(id);
        return route != null ? ResponseEntity.ok(route) : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/{id}/points")
    public ResponseEntity<List<RoutePoint>> getRoutePoints(@PathVariable Long id) {
        return ResponseEntity.ok(inspectionTaskService.getRoutePoints(id));
    }
    
    @PostMapping
    public ResponseEntity<Route> createRoute(@RequestBody Map<String, Object> body) {
        String routeName = (String) body.get("routeName");
        List<String> pointCodes = (List<String>) body.get("pointCodes");
        String startPointCode = (String) body.get("startPointCode");
        
        Route route = routePlanningService.createRoute(routeName, pointCodes, startPointCode);
        return ResponseEntity.ok(route);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Route> updateRoute(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String routeName = (String) body.get("routeName");
        List<String> pointCodes = (List<String>) body.get("pointCodes");
        
        Route route = routePlanningService.updateRoute(id, routeName, pointCodes);
        return route != null ? ResponseEntity.ok(route) : ResponseEntity.notFound().build();
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoute(@PathVariable Long id) {
        try {
            routePlanningService.deleteRoute(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.CONFLICT).build();
        }
    }
    
    @PostMapping("/{id}/recalculate")
    public ResponseEntity<Route> recalculateRoute(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String reason = (String) body.get("reason");
        List<String> addPointCodes = (List<String>) body.get("addPointCodes");
        List<String> removePointCodes = (List<String>) body.get("removePointCodes");
        List<String> statusChangePointCodes = (List<String>) body.get("statusChangePointCodes");
        
        Route route = routePlanningService.recalculateRoute(id, reason, addPointCodes, removePointCodes, statusChangePointCodes);
        return route != null ? ResponseEntity.ok(route) : ResponseEntity.notFound().build();
    }
    
    @PostMapping("/{id}/recommend")
    public ResponseEntity<Route> generateRecommendedRoute(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String currentLocationCode = (String) body.get("currentLocationCode");
        Double currentX = body.get("currentX") != null ? ((Number) body.get("currentX")).doubleValue() : null;
        Double currentY = body.get("currentY") != null ? ((Number) body.get("currentY")).doubleValue() : null;
        
        Route route = routePlanningService.generateRecommendedRoute(id, currentLocationCode, currentX, currentY);
        return route != null ? ResponseEntity.ok(route) : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/{id}/versions")
    public ResponseEntity<List<RouteVersion>> getRouteVersions(@PathVariable Long id) {
        return ResponseEntity.ok(routePlanningService.getRouteVersions(id));
    }
    
    @GetMapping("/version/{versionId}")
    public ResponseEntity<RouteVersion> getRouteVersionById(@PathVariable Long versionId) {
        RouteVersion version = routePlanningService.getRouteVersionById(versionId);
        return version != null ? ResponseEntity.ok(version) : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/version/{versionId}/changelog")
    public ResponseEntity<List<RouteChangeLog>> getVersionChangeLogs(@PathVariable Long versionId) {
        return ResponseEntity.ok(routePlanningService.getVersionChangeLogs(versionId));
    }
    
    @GetMapping("/{id}/versions/compare")
    public ResponseEntity<Map<String, Object>> compareVersions(@PathVariable Long id, 
                                                               @RequestParam Integer version1, 
                                                               @RequestParam Integer version2) {
        return ResponseEntity.ok(routePlanningService.compareVersions(id, version1, version2));
    }
    
    @PostMapping("/{id}/accept-recommend")
    public ResponseEntity<Route> acceptRecommendedRoute(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long recommendedVersionId = Long.parseLong(body.get("recommendedVersionId").toString());
        
        Route route = routePlanningService.acceptRecommendedRoute(id, recommendedVersionId);
        return route != null ? ResponseEntity.ok(route) : ResponseEntity.notFound().build();
    }
    
    @PostMapping("/{id}/assign-inspectors")
    public ResponseEntity<List<String>> assignPointsToInspectors(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        List<String> inspectors = (List<String>) body.get("inspectors");
        
        List<String> assignedPoints = routePlanningService.assignPointsToInspectors(id, inspectors);
        return ResponseEntity.ok(assignedPoints);
    }
    
    @PutMapping("/{id}/point-status")
    public ResponseEntity<Void> updatePointInspectionStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String pointCode = (String) body.get("pointCode");
        String status = (String) body.get("status");
        String inspector = (String) body.get("inspector");
        
        routePlanningService.updatePointInspectionStatus(id, pointCode, status, inspector);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/{id}/reorder")
    public ResponseEntity<Route> reorderRoutePoints(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        List<String> pointCodes = (List<String>) body.get("pointCodes");
        
        Route route = routePlanningService.reorderRoutePoints(id, pointCodes);
        return route != null ? ResponseEntity.ok(route) : ResponseEntity.notFound().build();
    }
}
