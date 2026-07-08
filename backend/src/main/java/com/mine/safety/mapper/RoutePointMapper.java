
package com.mine.safety.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mine.safety.entity.RoutePoint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoutePointMapper extends BaseMapper<RoutePoint> {
    List<RoutePoint> selectByRouteCode(@Param("routeCode") String routeCode);
    
    void deleteByRouteCode(@Param("routeCode") String routeCode);
}
