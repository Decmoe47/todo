# 使用官方的 OpenJDK 基础镜像
FROM openjdk:21

# 设置工作目录
WORKDIR /app

# 将构建好的 JAR 文件复制到容器中
COPY target/todo-backend.jar app.jar

# 暴露端口（假设 Spring Boot 使用 8080 端口）
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]