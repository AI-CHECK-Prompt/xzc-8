
package com.mine.safety.controller;

import com.mine.safety.entity.Route;
import com.mine.safety.entity.RoutePoint;
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
        routePlanningService.deleteRoute(id);
        return ResponseEntity.noContent().build();
    }
}
