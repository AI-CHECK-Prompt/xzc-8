package com.mine.safety.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("route_change_log")
public class RouteChangeLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long versionId;
    
    private Long routeId;
    
    private String pointCode;
    
    private String pointName;
    
    private String changeType;
    
    private Integer oldSequence;
    
    private Integer newSequence;
    
    private String operator;
    
    private LocalDateTime createTime;
}