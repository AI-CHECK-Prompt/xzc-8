
package com.mine.safety.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("monitoring_point")
public class MonitoringPoint {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String pointCode;
    
    private String pointName;
    
    private String location;
    
    private Double x;
    
    private Double y;
    
    private String deviceType;
    
    private String deviceCode;
    
    private String status;
    
    private Integer priority;
    
    private LocalDateTime openStartTime;
    
    private LocalDateTime openEndTime;
    
    private Integer estimatedDwellTime;
    
    private String ipAddress;
    
    private Integer port;
    
    private String description;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
