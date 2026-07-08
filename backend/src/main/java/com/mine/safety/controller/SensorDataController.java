
package com.mine.safety.controller;

import com.mine.safety.entity.SensorData;
import com.mine.safety.service.AlarmEngineService;
import com.mine.safety.service.SensorDataService;
import com.mine.safety.service.WebSocketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sensor")
@CrossOrigin(origins = "*")
public class SensorDataController {
    
    private final SensorDataService sensorDataService;
    private final AlarmEngineService alarmEngineService;
    private final WebSocketService webSocketService;
    
    public SensorDataController(SensorDataService sensorDataService, 
                                AlarmEngineService alarmEngineService,
                                WebSocketService webSocketService) {
        this.sensorDataService = sensorDataService;
        this.alarmEngineService = alarmEngineService;
        this.webSocketService = webSocketService;
    }
    
    @PostMapping("/data")
    public ResponseEntity<SensorData> collectData(@RequestBody SensorData data) {
        SensorData saved = sensorDataService.saveData(data);
        alarmEngineService.checkAndTriggerAlarm(data);
        webSocketService.sendSensorData(saved);
        return ResponseEntity.ok(saved);
    }
    
    @GetMapping("/data/{pointCode}")
    public ResponseEntity<List<SensorData>> getDataByPoint(@PathVariable String pointCode) {
        return ResponseEntity.ok(sensorDataService.getDataByPoint(pointCode));
    }
    
    @GetMapping("/data/{pointCode}/recent/{limit}")
    public ResponseEntity<List<SensorData>> getRecentData(@PathVariable String pointCode, @PathVariable int limit) {
        return ResponseEntity.ok(sensorDataService.getRecentData(pointCode, limit));
    }
}
