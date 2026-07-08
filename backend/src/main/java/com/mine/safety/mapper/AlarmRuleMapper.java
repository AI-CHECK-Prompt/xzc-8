
package com.mine.safety.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mine.safety.entity.AlarmRule;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AlarmRuleMapper extends BaseMapper<AlarmRule> {
    List<AlarmRule> selectEnabledRulesByPointCode(String pointCode);
}
