
package com.mine.safety.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("route")
public class Route {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String routeName;
    
    private String routeCode;
    
    private Integer totalPoints;
    
    private Double totalDistance;
    
    private Integer estimatedTime;
    
    private String startPointCode;
    
    private String endPointCode;
    
    private String status;
    
    private String description;
    
    private String creator;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
