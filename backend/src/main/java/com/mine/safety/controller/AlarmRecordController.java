
package com.mine.safety.controller;

import com.mine.safety.entity.AlarmRecord;
import com.mine.safety.service.AlarmRecordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alarm/records")
@CrossOrigin(origins = "*")
public class AlarmRecordController {
    
    private final AlarmRecordService alarmRecordService;
    
    public AlarmRecordController(AlarmRecordService alarmRecordService) {
        this.alarmRecordService = alarmRecordService;
    }
    
    @GetMapping
    public ResponseEntity<List<AlarmRecord>> getAllRecords() {
        return ResponseEntity.ok(alarmRecordService.list());
    }
    
    @GetMapping("/recent/{limit}")
    public ResponseEntity<List<AlarmRecord>> getRecentAlarms(@PathVariable int limit) {
        return ResponseEntity.ok(alarmRecordService.getRecentAlarms(limit));
    }
    
    @GetMapping("/point/{pointCode}")
    public ResponseEntity<List<AlarmRecord>> getAlarmsByPoint(@PathVariable String pointCode) {
        return ResponseEntity.ok(alarmRecordService.getAlarmsByPoint(pointCode));
    }
    
    @GetMapping("/unhandled")
    public ResponseEntity<List<AlarmRecord>> getUnhandledAlarms() {
        return ResponseEntity.ok(alarmRecordService.getUnHandledAlarms());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AlarmRecord> getRecordById(@PathVariable Long id) {
        AlarmRecord record = alarmRecordService.getById(id);
        return record != null ? ResponseEntity.ok(record) : ResponseEntity.notFound().build();
    }
    
    @PutMapping("/{id}/handle")
    public ResponseEntity<AlarmRecord> handleAlarm(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String user = body.get("user");
        String result = body.get("result");
        return ResponseEntity.ok(alarmRecordService.handleAlarm(id, user, result));
    }
}
