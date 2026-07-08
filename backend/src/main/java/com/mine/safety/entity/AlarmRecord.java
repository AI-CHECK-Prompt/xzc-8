
package com.mine.safety.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("alarm_record")
public class AlarmRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long pointId;
    
    private String pointCode;
    
    private String pointName;
    
    private String dataType;
    
    private Double currentValue;
    
    private String unit;
    
    private String alarmLevel;
    
    private String alarmMessage;
    
    private String status;
    
    private LocalDateTime triggerTime;
    
    private LocalDateTime handleTime;
    
    private String handleUser;
    
    private String handleResult;
    
    private LocalDateTime createTime;
}
