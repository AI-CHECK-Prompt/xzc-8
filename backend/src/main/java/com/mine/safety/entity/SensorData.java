
package com.mine.safety.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sensor_data")
public class SensorData {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long pointId;
    
    private String pointCode;
    
    private String dataType;
    
    private Double value;
    
    private String unit;
    
    private LocalDateTime collectTime;
    
    private LocalDateTime createTime;
}
