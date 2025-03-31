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

## 进度

- [ ]  用户
    - [x]  登录
    - [x]  注册
    - [x]  注销
    - [ ]  设置
- [ ]  todo
    - [x]  增
    - [ ]  查
    - [ ]  删（设计成可批量的api）
    - [ ]  改（设计成可批量的api）
    - [ ]  排序
    - [ ]  移动
    - [ ]  todo提醒
        - [ ]  应用内提醒（WebSocket）
        - [ ]  邮件提醒
    - [ ]  分组
- [ ]  todo list
    - [x]  增
    - [ ]  查
    - [ ]  删（设计成可批量的api）
    - [ ]  改名
    - [ ]  智能清单
- [ ]  *任务共享与协作*
- [ ]  *响应式布局*
- [ ]  *数据统计图表*
- [ ]  docker + Nginx部署