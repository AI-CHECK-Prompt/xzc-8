package com.mine.safety.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mine.safety.entity.RouteChangeLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RouteChangeLogMapper extends BaseMapper<RouteChangeLog> {
    List<RouteChangeLog> selectByVersionId(@Param("versionId") Long versionId);
    
    List<RouteChangeLog> selectByRouteId(@Param("routeId") Long routeId);
    
    List<RouteChangeLog> selectByVersionIds(@Param("versionIds") List<Long> versionIds);
}