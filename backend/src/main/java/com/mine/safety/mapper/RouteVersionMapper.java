package com.mine.safety.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mine.safety.entity.RouteVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RouteVersionMapper extends BaseMapper<RouteVersion> {
    List<RouteVersion> selectByRouteId(@Param("routeId") Long routeId);
    
    Integer selectMaxVersionNumber(@Param("routeId") Long routeId);
    
    List<RouteVersion> selectByRouteIdAndVersionRange(@Param("routeId") Long routeId, 
                                                      @Param("minVersion") Integer minVersion, 
                                                      @Param("maxVersion") Integer maxVersion);
}