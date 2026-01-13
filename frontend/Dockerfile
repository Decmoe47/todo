# syntax=docker/dockerfile:1.7

# -------- build stage --------
FROM node:22-bookworm-slim AS build

ENV VITE_API_URL=/api
ENV NODE_ENV=production

WORKDIR /app

# 拷贝依赖描述文件
COPY package.json package-lock.json* pnpm-lock.yaml* yarn.lock* ./

# 安装依赖，使用缓存加速
RUN --mount=type=cache,target=/root/.npm \
    npm ci

# 再拷贝源码并构建
COPY . .

# 构建 Vue 项目
RUN npm run build

# -------- runtime stage --------
FROM nginx:1.27-alpine
WORKDIR /usr/share/nginx/html

# 清空默认静态文件
RUN rm -rf ./*

# 复制构建产物（Vite 默认 dist；如果你是 Vue CLI 可能是 dist 也一样）
COPY --from=build /app/dist ./

# 复制自定义 nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
