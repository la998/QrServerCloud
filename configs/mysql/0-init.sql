-- init.sql
-- Author: changlong
-- Version: 1.0
-- Description: 初始化数据库及权限配置

/* ======================= 数据库初始化 ======================= */

-- Nacos 配置中心数据库
CREATE DATABASE IF NOT EXISTS nacos_config CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'nacos'@'%' IDENTIFIED WITH mysql_native_password BY 'nacos123456';
GRANT ALL PRIVILEGES ON nacos_config.* TO 'nacos'@'%' WITH GRANT OPTION;

-- Seata 事务数据库
CREATE DATABASE IF NOT EXISTS seata CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'seata'@'%' IDENTIFIED WITH mysql_native_password BY 'seata123456';
GRANT ALL PRIVILEGES ON seata.* TO 'seata'@'%';

-- auth-service 认证模块 数据库
CREATE DATABASE IF NOT EXISTS auth_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
GRANT SELECT, INSERT, UPDATE, DELETE ON auth_db.* TO 'nacos'@'%';

-- user-service 用户模块 数据库
CREATE DATABASE IF NOT EXISTS user_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

FLUSH PRIVILEGES;
