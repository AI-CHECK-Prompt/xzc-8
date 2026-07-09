ALTER TABLE monitoring_point
ADD COLUMN priority INT DEFAULT 3 COMMENT '优先级：1-紧急, 2-高, 3-中, 4-低, 5-普通',
ADD COLUMN open_start_time DATETIME COMMENT '开放开始时间',
ADD COLUMN open_end_time DATETIME COMMENT '开放结束时间',
ADD COLUMN estimated_dwell_time INT DEFAULT 5 COMMENT '预计停留时间（分钟）',
ADD COLUMN x DOUBLE COMMENT '位置坐标X',
ADD COLUMN y DOUBLE COMMENT '位置坐标Y';

ALTER TABLE route_point
ADD COLUMN inspection_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '巡检状态：COMPLETED/PENDING/SKIPPED',
ADD COLUMN actual_inspection_time DATETIME COMMENT '实际巡检时间',
ADD COLUMN inspector VARCHAR(50) COMMENT '巡检人员';

CREATE TABLE IF NOT EXISTS route_version (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    route_id BIGINT NOT NULL COMMENT '路线ID',
    route_code VARCHAR(50) NOT NULL COMMENT '路线编码',
    version_number INT NOT NULL COMMENT '版本号',
    change_reason VARCHAR(200) COMMENT '变更原因',
    change_type VARCHAR(50) COMMENT '变更类型',
    total_points INT COMMENT '总点数',
    total_distance DOUBLE COMMENT '总距离',
    estimated_time INT COMMENT '预计时间',
    operator VARCHAR(50) COMMENT '操作人',
    before_snapshot TEXT COMMENT '变更前快照',
    after_snapshot TEXT COMMENT '变更后快照',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_route_id (route_id),
    INDEX idx_version_number (version_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='路线版本表';

CREATE TABLE IF NOT EXISTS route_change_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version_id BIGINT NOT NULL COMMENT '版本ID',
    route_id BIGINT NOT NULL COMMENT '路线ID',
    point_code VARCHAR(50) COMMENT '监控点编码',
    point_name VARCHAR(100) COMMENT '监控点名称',
    change_type VARCHAR(50) COMMENT '变更类型',
    old_sequence INT COMMENT '原顺序',
    new_sequence INT COMMENT '新顺序',
    operator VARCHAR(50) COMMENT '操作人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_version_id (version_id),
    INDEX idx_route_id (route_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='路线变更日志表';