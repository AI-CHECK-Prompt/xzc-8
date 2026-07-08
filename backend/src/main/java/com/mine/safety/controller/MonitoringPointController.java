
package com.mine.safety.controller;

import com.mine.safety.entity.MonitoringPoint;
import com.mine.safety.service.MonitoringPointService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/points")
@CrossOrigin(origins = "*")
public class MonitoringPointController {
    
    private final MonitoringPointService monitoringPointService;
    
    public MonitoringPointController(MonitoringPointService monitoringPointService) {
        this.monitoringPointService = monitoringPointService;
    }
    
    @GetMapping
    public ResponseEntity<List<MonitoringPoint>> getAllPoints() {
        return ResponseEntity.ok(monitoringPointService.getAllPoints());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<MonitoringPoint> getPointById(@PathVariable Long id) {
        MonitoringPoint point = monitoringPointService.getPointById(id);
        return point != null ? ResponseEntity.ok(point) : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/code/{pointCode}")
    public ResponseEntity<MonitoringPoint> getPointByCode(@PathVariable String pointCode) {
        MonitoringPoint point = monitoringPointService.getPointByCode(pointCode);
        return point != null ? ResponseEntity.ok(point) : ResponseEntity.notFound().build();
    }
    
    @PostMapping
    public ResponseEntity<MonitoringPoint> createPoint(@RequestBody MonitoringPoint point) {
        return ResponseEntity.ok(monitoringPointService.createPoint(point));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<MonitoringPoint> updatePoint(@PathVariable Long id, @RequestBody MonitoringPoint point) {
        point.setId(id);
        return ResponseEntity.ok(monitoringPointService.updatePoint(point));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePoint(@PathVariable Long id) {
        monitoringPointService.deletePoint(id);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{pointCode}/status/{status}")
    public ResponseEntity<Void> updateStatus(@PathVariable String pointCode, @PathVariable String status) {
        monitoringPointService.updatePointStatus(pointCode, status);
        return ResponseEntity.ok().build();
    }
}
