# TODO

## 技术栈

- 前端：Vue 3、Element Plus、Vue Router、Pinia
- 后端：Spring Boot 3、Spring Security、Spring Data JPA、Spring Cache 
- 数据库：MySQL、Redis

## 功能描述

- 用户登录/注册。
- 待办任务的增删改查。
- 待办清单的增删改。
- 简单的定时提醒功能（比如任务的到期时间）。
- 创建团队，在团队中共享任务。

## 实现进度

- [ ] 用户
  - [x] 登录
    - [x] 注册
    - [x] 注销
    - [ ] _设置_
- [ ] todo
    - [x] 增
    - [ ] 查
    - [x] 删
    - [ ] 改名
    - [ ] 改提醒日期
    - [ ] 移动
    - [ ] _分组_
    - [ ] _排序_
    - [ ] todo提醒
      - [ ] 显示提醒日期
      - [ ] _应用内提醒（WebSocket）_
      - [ ] 邮件提醒
    - [ ] 多选
      - [ ] 删
      - [ ] 改名
      - [ ] 改提醒日期
      - [ ] 移动
      - [ ] 分组
- [ ] todo list
    - [x] 增
    - [x] 删
    - [x] 改名
    - [ ] _智能清单_
- [ ] _任务共享与协作_
- [ ] _响应式布局_
- [ ] _数据统计图表_
- [ ] docker + Nginx部署
- [ ] _Github Actions CI/CD_