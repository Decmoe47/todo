# TODO

## Demo

http://3.112.70.190:80/

Register your own account or use the following accout to try: 
- email: lingsq47@outlook.com
- password: 123456

## Technology stack

- Frontend: Vue 3, Element Plus, Vue Router, Pinia
- Backend: Spring Boot 3, Spring Security, Spring Data JPA, Spring Cache 
- Databse: MySQL, Redis
- Infra: AWS EC2, Docker, Nginx

## Function description

- [x] User login/registration.
- [x] Add, delete, modify, and check to-do tasks.
- [x] Add, delete, and modify to-do lists.
- [ ] Simple timer reminder function (such as task expiration date).
- [ ] Create a team and share tasks within the team.

## Implementation progress

- [ ] user
  - [x] login
  - [x] register
  - [x] logout
  - [ ] settings
  - [x] permission control
- [ ] todo
  - [x] add
  - [x] delete
  - [x] rename
  - [x] change reminder date
  - [x] move
  - [ ] search
  - [ ] group
  - [ ] sort
  - [ ] todo reminder
    - [ ] show reminder date
    - [ ] _in-app reminder (WebSocket)_
    - [ ] email reminder
    - [ ] can select only the date but not the time
  - [ ] multiple select
    - [ ] delete
    - [ ] rename
    - [ ] change reminder date
    - [ ] move
    - [ ] group
- [ ] todo list
  - [x] add
  - [x] delete
  - [x] rename
  - [ ] _smart lists_
- [ ] _task sharing and collaboration_
- [ ] _responsive layout for mobile_
- [ ] _statistical charts_
- [x] Docker + Nginx deployment
- [x] deploy to AWS
- [ ] Github Actions CI/CD