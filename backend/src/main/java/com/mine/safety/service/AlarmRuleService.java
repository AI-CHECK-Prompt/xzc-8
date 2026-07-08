
package com.mine.safety.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mine.safety.entity.AlarmRule;

import java.util.List;

public interface AlarmRuleService extends IService<AlarmRule> {
    List<AlarmRule> getEnabledRulesByPointCode(String pointCode);
    
    AlarmRule createRule(AlarmRule rule);
    
    AlarmRule updateRule(AlarmRule rule);
    
    void deleteRule(Long id);
    
    void enableRule(Long id, boolean enabled);
}
