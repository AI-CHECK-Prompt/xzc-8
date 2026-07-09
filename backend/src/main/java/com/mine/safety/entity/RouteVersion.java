package com.mine.safety.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("route_version")
public class RouteVersion {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long routeId;
    
    private String routeCode;
    
    private Integer versionNumber;
    
    private String changeReason;
    
    private String changeType;
    
    private Integer totalPoints;
    
    private Double totalDistance;
    
    private Integer estimatedTime;
    
    private String operator;
    
    private String beforeSnapshot;
    
    private String afterSnapshot;
    
    private LocalDateTime createTime;
}