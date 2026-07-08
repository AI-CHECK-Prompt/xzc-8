
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
