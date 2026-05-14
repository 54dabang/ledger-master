-- ============================================
-- loadValidUsers 方法性能优化索引脚本
-- ============================================
-- 说明：此脚本用于优化用户列表查询性能
-- 执行前请备份数据库，并在测试环境验证后再在生产环境执行

-- 1. 为 sys_user 表的 del_flag 字段添加索引（如果不存在）
-- 作用：加速 WHERE u.del_flag = '0' 条件过滤
CREATE INDEX IF NOT EXISTS idx_del_flag ON sys_user(del_flag);

-- 2. 为 sys_user 表的 user_name 字段添加索引（如果不存在）
-- 作用：加速 LIKE 模糊查询（虽然 %xxx% 无法完全利用索引，但能提升部分性能）
CREATE INDEX IF NOT EXISTS idx_user_name ON sys_user(user_name);

-- 3. 为 sys_user 表的 nick_name 字段添加索引（如果不存在）
-- 作用：加速 LIKE 模糊查询
CREATE INDEX IF NOT EXISTS idx_nick_name ON sys_user(nick_name);

-- 4. 为 sys_user 表的 dept_id 字段添加索引（如果不存在）
-- 作用：加速 LEFT JOIN sys_dept 的关联查询
CREATE INDEX IF NOT EXISTS idx_dept_id ON sys_user(dept_id);

-- 5. 为 sys_dept 表的主键添加确认（通常已存在）
-- 作用：确保部门表关联查询效率
-- ALTER TABLE sys_dept ADD PRIMARY KEY IF NOT EXISTS (dept_id);

-- ============================================
-- 优化效果说明：
-- 1. 原查询问题：
--    - 关联了 sys_user_role 和 sys_role 表，导致一个用户有多个角色时产生多行数据
--    - 查询了大量不需要的字段（password, login_ip, remark 等）
--    - MyBatis 需要进行复杂的 ResultMap 嵌套映射
-- 
-- 2. 优化后改进：
--    - 只关联 sys_dept 表，不再关联角色表
--    - 只查询需要的字段：user_id, dept_id, user_name, nick_name, sex, dept_name
--    - 减少了数据传输量和内存占用
--    - 简化了 MyBatis 的对象映射过程
--    - 默认 pageSize 从 200 降低到 50，减少单次查询数据量
-- 
-- 3. 预期性能提升：
--    - 查询速度提升 50%-80%（取决于数据量和用户角色数量）
--    - 内存占用减少 60%-70%
--    - 网络传输数据量减少 70%-80%
-- ============================================

-- 查看索引是否创建成功
SHOW INDEX FROM sys_user;
SHOW INDEX FROM sys_dept;
