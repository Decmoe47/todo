# 使用官方的 Node.js 基础镜像
FROM node:22 AS build-stage

# 设置工作目录
WORKDIR /app

# 复制 package.json 和 package-lock.json
COPY package*.json ./

# 安装依赖
RUN npm install

# 复制项目文件
COPY . .

# 构建 Vue 项目
RUN npm run build

# 使用 Nginx 作为静态文件服务器
FROM nginx:alpine

# 将构建结果复制到 Nginx 的默认静态文件目录
COPY --from=build-stage /app/dist /usr/share/nginx/html

# 复制自定义的 nginx.conf 到 Nginx 配置目录（覆盖默认配置）
COPY nginx.conf /etc/nginx/nginx.conf

# 暴露端口（Nginx 默认使用 80 端口）
EXPOSE 80