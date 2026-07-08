
package com.mine.safety.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("inspection_task")
public class InspectionTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String taskCode;
    
    private Long routeId;
    
    private String routeCode;
    
    private String routeName;
    
    private String assignee;
    
    private String status;
    
    private LocalDateTime scheduledStartTime;
    
    private LocalDateTime scheduledEndTime;
    
    private LocalDateTime actualStartTime;
    
    private LocalDateTime actualEndTime;
    
    private Integer completedPoints;
    
    private Integer totalPoints;
    
    private Double traveledDistance;
    
    private Double savedDistance;
    
    private String remarks;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
