
package com.mine.safety.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mine.safety.entity.AlarmRule;
import com.mine.safety.mapper.AlarmRuleMapper;
import com.mine.safety.service.AlarmRuleService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlarmRuleServiceImpl extends ServiceImpl<AlarmRuleMapper, AlarmRule> implements AlarmRuleService {
    
    @Override
    public List<AlarmRule> getEnabledRulesByPointCode(String pointCode) {
        LambdaQueryWrapper<AlarmRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlarmRule::getPointCode, pointCode);
        wrapper.eq(AlarmRule::getEnabled, "1");
        return baseMapper.selectList(wrapper);
    }
    
    @Override
    public AlarmRule createRule(AlarmRule rule) {
        rule.setCreateTime(LocalDateTime.now());
        rule.setUpdateTime(LocalDateTime.now());
        rule.setEnabled("1");
        baseMapper.insert(rule);
        return rule;
    }
    
    @Override
    public AlarmRule updateRule(AlarmRule rule) {
        rule.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(rule);
        return rule;
    }
    
    @Override
    public void deleteRule(Long id) {
        baseMapper.deleteById(id);
    }
    
    @Override
    public void enableRule(Long id, boolean enabled) {
        AlarmRule rule = baseMapper.selectById(id);
        if (rule != null) {
            rule.setEnabled(enabled ? "1" : "0");
            rule.setUpdateTime(LocalDateTime.now());
            baseMapper.updateById(rule);
        }
    }
}
