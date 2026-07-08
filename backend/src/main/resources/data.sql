
INSERT INTO monitoring_point (point_code, point_name, location, device_type, device_code, status, ip_address, port, description, create_time, update_time) VALUES
('P001', '一号矿井-瓦斯监测点', '一号矿井100米深处', '瓦斯传感器', 'D001', 'NORMAL', '192.168.1.101', 8080, '监测瓦斯浓度', NOW(), NOW()),
('P002', '一号矿井-温度监测点', '一号矿井80米深处', '温度传感器', 'D002', 'NORMAL', '192.168.1.102', 8080, '监测环境温度', NOW(), NOW()),
('P003', '一号矿井-湿度监测点', '一号矿井80米深处', '湿度传感器', 'D003', 'NORMAL', '192.168.1.103', 8080, '监测环境湿度', NOW(), NOW()),
('P004', '二号矿井-瓦斯监测点', '二号矿井120米深处', '瓦斯传感器', 'D004', 'NORMAL', '192.168.1.104', 8080, '监测瓦斯浓度', NOW(), NOW()),
('P005', '二号矿井-温度监测点', '二号矿井100米深处', '温度传感器', 'D005', 'NORMAL', '192.168.1.105', 8080, '监测环境温度', NOW(), NOW()),
('P006', '二号矿井-风速监测点', '二号矿井通风口', '风速传感器', 'D006', 'NORMAL', '192.168.1.106', 8080, '监测风速', NOW(), NOW());

INSERT INTO alarm_rule (point_id, point_code, data_type, compare_type, threshold_value, alarm_level, enabled, description, create_time, update_time) VALUES
(1, 'P001', '瓦斯浓度', 'GT', 0.5, 'WARNING', '1', '瓦斯浓度超过0.5%触发预警', NOW(), NOW()),
(1, 'P001', '瓦斯浓度', 'GT', 1.0, 'DANGER', '1', '瓦斯浓度超过1.0%触发危险告警', NOW(), NOW()),
(2, 'P002', '温度', 'GT', 35.0, 'WARNING', '1', '温度超过35度触发预警', NOW(), NOW()),
(2, 'P002', '温度', 'GT', 40.0, 'DANGER', '1', '温度超过40度触发危险告警', NOW(), NOW()),
(4, 'P004', '瓦斯浓度', 'GT', 0.5, 'WARNING', '1', '瓦斯浓度超过0.5%触发预警', NOW(), NOW()),
(4, 'P004', '瓦斯浓度', 'GT', 1.0, 'DANGER', '1', '瓦斯浓度超过1.0%触发危险告警', NOW(), NOW()),
(5, 'P005', '温度', 'GT', 35.0, 'WARNING', '1', '温度超过35度触发预警', NOW(), NOW()),
(6, 'P006', '风速', 'LT', 1.0, 'WARNING', '1', '风速低于1m/s触发预警', NOW(), NOW());
