package com.mine.safety.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mine.safety.entity.AlarmRecord;
import com.mine.safety.entity.SensorData;
import com.mine.safety.service.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class WebSocketServiceImpl implements WebSocketService {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketServiceImpl.class);
    
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicLong sequenceCounter = new AtomicLong(0);
    
    public void registerSession(String key, WebSocketSession session) {
        sessions.put(key, session);
        logger.info("WebSocket session registered: {}", key);
    }
    
    public void unregisterSession(String key) {
        sessions.remove(key);
        logger.info("WebSocket session unregistered: {}", key);
    }
    
    @Override
    public void sendSensorData(SensorData data) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "sensorData");
            message.put("data", data);
            broadcast(message);
        } catch (Exception e) {
            logger.error("发送传感器数据失败", e);
        }
    }
    
    @Override
    public void sendAlarm(AlarmRecord record) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "alarm");
            message.put("data", record);
            broadcast(message);
        } catch (Exception e) {
            logger.error("发送告警数据失败", e);
        }
    }
    
    @Override
    public void sendPointStatus(String pointCode, String status) {
        try {
            Map<String, Object> innerData = new HashMap<>();
            innerData.put("pointCode", pointCode);
            innerData.put("status", status);
            
            Map<String, Object> message = new HashMap<>();
            message.put("type", "pointStatus");
            message.put("data", innerData);
            broadcast(message);
        } catch (Exception e) {
            logger.error("发送监控点状态失败", e);
        }
    }
    
    private void broadcast(Map<String, Object> message) throws IOException {
        message.put("seq", sequenceCounter.incrementAndGet());
        String json = objectMapper.writeValueAsString(message);
        for (WebSocketSession session : sessions.values()) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(json));
            }
        }
    }
}