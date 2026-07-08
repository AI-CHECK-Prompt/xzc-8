
CREATE TABLE IF NOT EXISTS monitoring_point (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    point_code VARCHAR(50) NOT NULL UNIQUE,
    point_name VARCHAR(100) NOT NULL,
    location VARCHAR(200),
    device_type VARCHAR(50),
    device_code VARCHAR(50),
    status VARCHAR(20) DEFAULT 'NORMAL',
    ip_address VARCHAR(50),
    port INT,
    description TEXT,
    create_time DATETIME,
    update_time DATETIME
);

CREATE TABLE IF NOT EXISTS sensor_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    point_id BIGINT,
    point_code VARCHAR(50) NOT NULL,
    data_type VARCHAR(50) NOT NULL,
    value DOUBLE NOT NULL,
    unit VARCHAR(20),
    collect_time DATETIME,
    create_time DATETIME,
    INDEX idx_point_code (point_code),
    INDEX idx_collect_time (collect_time)
);

CREATE TABLE IF NOT EXISTS alarm_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    point_id BIGINT,
    point_code VARCHAR(50) NOT NULL,
    data_type VARCHAR(50) NOT NULL,
    compare_type VARCHAR(10) NOT NULL,
    threshold_value DOUBLE NOT NULL,
    alarm_level VARCHAR(20) DEFAULT 'WARNING',
    enabled VARCHAR(1) DEFAULT '1',
    description TEXT,
    create_time DATETIME,
    update_time DATETIME,
    INDEX idx_point_code (point_code)
);

CREATE TABLE IF NOT EXISTS alarm_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    point_id BIGINT,
    point_code VARCHAR(50) NOT NULL,
    point_name VARCHAR(100),
    data_type VARCHAR(50) NOT NULL,
    current_value DOUBLE NOT NULL,
    unit VARCHAR(20),
    alarm_level VARCHAR(20),
    alarm_message TEXT,
    status VARCHAR(20) DEFAULT 'UNHANDLED',
    trigger_time DATETIME,
    handle_time DATETIME,
    handle_user VARCHAR(50),
    handle_result TEXT,
    create_time DATETIME,
    INDEX idx_point_code (point_code),
    INDEX idx_status (status),
    INDEX idx_trigger_time (trigger_time)
);

CREATE TABLE IF NOT EXISTS route (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    route_name VARCHAR(100) NOT NULL,
    route_code VARCHAR(50) NOT NULL UNIQUE,
    total_points INT DEFAULT 0,
    total_distance DOUBLE DEFAULT 0,
    estimated_time INT DEFAULT 0,
    start_point_code VARCHAR(50),
    end_point_code VARCHAR(50),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    description TEXT,
    creator VARCHAR(50),
    create_time DATETIME,
    update_time DATETIME,
    INDEX idx_route_code (route_code)
);

CREATE TABLE IF NOT EXISTS route_point (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    route_id BIGINT,
    route_code VARCHAR(50) NOT NULL,
    point_id BIGINT,
    point_code VARCHAR(50) NOT NULL,
    point_name VARCHAR(100),
    sequence INT NOT NULL,
    distance_from_prev DOUBLE DEFAULT 0,
    cumulative_distance DOUBLE DEFAULT 0,
    create_time DATETIME,
    INDEX idx_route_code (route_code),
    INDEX idx_point_code (point_code)
);

CREATE TABLE IF NOT EXISTS inspection_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_code VARCHAR(50) NOT NULL UNIQUE,
    route_id BIGINT,
    route_code VARCHAR(50),
    route_name VARCHAR(100),
    assignee VARCHAR(50) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    scheduled_start_time DATETIME,
    scheduled_end_time DATETIME,
    actual_start_time DATETIME,
    actual_end_time DATETIME,
    completed_points INT DEFAULT 0,
    total_points INT DEFAULT 0,
    traveled_distance DOUBLE DEFAULT 0,
    saved_distance DOUBLE DEFAULT 0,
    remarks TEXT,
    create_time DATETIME,
    update_time DATETIME,
    INDEX idx_task_code (task_code),
    INDEX idx_status (status)
);
