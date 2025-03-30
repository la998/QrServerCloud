-- custom.sql
-- 设置nacos默认用户
USE nacos_config;
-- 明文密码为 nacos123456，加密后为 $2a$10$rB7qa6.CMCOYvylenPClZeodSuaJxkKdujI7b.OWz0tWksOkbcRCe
INSERT INTO `users` (`username`, `password`, `enabled`)
VALUES ('nacos', '$2a$10$rB7qa6.CMCOYvylenPClZeodSuaJxkKdujI7b.OWz0tWksOkbcRCe', TRUE);

-- 赋予 nacos 用户角色
INSERT INTO `roles` (`username`, `role`)
VALUES ('nacos', 'ROLE_ADMIN');