
package com.mine.safety.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("route_point")
public class RoutePoint {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long routeId;
    
    private String routeCode;
    
    private Long pointId;
    
    private String pointCode;
    
    private String pointName;
    
    private Integer sequence;
    
    private Double distanceFromPrev;
    
    private Double cumulativeDistance;
    
    private LocalDateTime createTime;
}
