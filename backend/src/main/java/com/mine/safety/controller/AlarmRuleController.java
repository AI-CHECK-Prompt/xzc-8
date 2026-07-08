
package com.mine.safety.controller;

import com.mine.safety.entity.AlarmRule;
import com.mine.safety.service.AlarmRuleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alarm/rules")
@CrossOrigin(origins = "*")
public class AlarmRuleController {
    
    private final AlarmRuleService alarmRuleService;
    
    public AlarmRuleController(AlarmRuleService alarmRuleService) {
        this.alarmRuleService = alarmRuleService;
    }
    
    @GetMapping
    public ResponseEntity<List<AlarmRule>> getAllRules() {
        return ResponseEntity.ok(alarmRuleService.list());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AlarmRule> getRuleById(@PathVariable Long id) {
        AlarmRule rule = alarmRuleService.getById(id);
        return rule != null ? ResponseEntity.ok(rule) : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/point/{pointCode}")
    public ResponseEntity<List<AlarmRule>> getRulesByPoint(@PathVariable String pointCode) {
        return ResponseEntity.ok(alarmRuleService.getEnabledRulesByPointCode(pointCode));
    }
    
    @PostMapping
    public ResponseEntity<AlarmRule> createRule(@RequestBody AlarmRule rule) {
        return ResponseEntity.ok(alarmRuleService.createRule(rule));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<AlarmRule> updateRule(@PathVariable Long id, @RequestBody AlarmRule rule) {
        rule.setId(id);
        return ResponseEntity.ok(alarmRuleService.updateRule(rule));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        alarmRuleService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{id}/enable/{enabled}")
    public ResponseEntity<Void> enableRule(@PathVariable Long id, @PathVariable boolean enabled) {
        alarmRuleService.enableRule(id, enabled);
        return ResponseEntity.ok().build();
    }
}
