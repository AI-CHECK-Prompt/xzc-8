package com.mine.safety.service.impl;

import com.mine.safety.entity.AlarmRecord;
import com.mine.safety.entity.AlarmRule;
import com.mine.safety.entity.MonitoringPoint;
import com.mine.safety.entity.SensorData;
import com.mine.safety.service.AlarmEngineService;
import com.mine.safety.service.AlarmRecordService;
import com.mine.safety.service.AlarmRuleService;
import com.mine.safety.service.MonitoringPointService;
import com.mine.safety.service.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlarmEngineServiceImpl implements AlarmEngineService {
    
    private static final Logger logger = LoggerFactory.getLogger(AlarmEngineServiceImpl.class);
    
    private final AlarmRuleService alarmRuleService;
    private final AlarmRecordService alarmRecordService;
    private final MonitoringPointService monitoringPointService;
    private final WebSocketService webSocketService;
    
    public AlarmEngineServiceImpl(AlarmRuleService alarmRuleService, 
                                  AlarmRecordService alarmRecordService,
                                  MonitoringPointService monitoringPointService,
                                  WebSocketService webSocketService) {
        this.alarmRuleService = alarmRuleService;
        this.alarmRecordService = alarmRecordService;
        this.monitoringPointService = monitoringPointService;
        this.webSocketService = webSocketService;
    }
    
    @Override
    public void checkAndTriggerAlarm(SensorData data) {
        List<AlarmRule> rules = alarmRuleService.getEnabledRulesByPointCode(data.getPointCode());
        
        for (AlarmRule rule : rules) {
            if (rule.getDataType().equals(data.getDataType())) {
                if (matchCondition(data.getValue(), rule.getCompareType(), rule.getThresholdValue())) {
                    triggerAlarm(data, rule);
                }
            }
        }
    }
    
    private boolean matchCondition(Double value, String compareType, Double threshold) {
        switch (compareType) {
            case "GT":
                return value > threshold;
            case "LT":
                return value < threshold;
            case "GE":
                return value >= threshold;
            case "LE":
                return value <= threshold;
            case "EQ":
                return value.equals(threshold);
            default:
                return false;
        }
    }
    
    private void triggerAlarm(SensorData data, AlarmRule rule) {
        MonitoringPoint point = monitoringPointService.getPointByCode(data.getPointCode());
        
        AlarmRecord record = new AlarmRecord();
        record.setPointId(point != null ? point.getId() : null);
        record.setPointCode(data.getPointCode());
        record.setPointName(point != null ? point.getPointName() : "Unknown");
        record.setDataType(data.getDataType());
        record.setCurrentValue(data.getValue());
        record.setUnit(data.getUnit());
        record.setAlarmLevel(rule.getAlarmLevel());
        record.setAlarmMessage(buildAlarmMessage(data, rule));
        
        alarmRecordService.createAlarm(record);
        logger.info("【告警触发】监控点: {}, 类型: {}, 当前值: {}, 阈值: {}, 级别: {}", 
                    data.getPointCode(), data.getDataType(), data.getValue(), 
                    rule.getThresholdValue(), rule.getAlarmLevel());
        
        monitoringPointService.updatePointStatus(data.getPointCode(), "ALARM");
        webSocketService.sendAlarm(record);
    }
    
    private String buildAlarmMessage(SensorData data, AlarmRule rule) {
        String compareText;
        switch (rule.getCompareType()) {
            case "GT":
                compareText = "大于";
                break;
            case "LT":
                compareText = "小于";
                break;
            case "GE":
                compareText = "大于等于";
                break;
            case "LE":
                compareText = "小于等于";
                break;
            case "EQ":
                compareText = "等于";
                break;
            default:
                compareText = "未知";
        }
        return String.format("%s %s %s%s (阈值: %s%s)", 
                            data.getDataType(), compareText, 
                            data.getValue(), data.getUnit(),
                            rule.getThresholdValue(), data.getUnit());
    }
}