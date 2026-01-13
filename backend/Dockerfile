# syntax=docker/dockerfile:1.7

# -------- build stage --------
FROM azul/zulu-openjdk:24-jdk AS build
WORKDIR /app

# 拷贝构建描述文件，最大化缓存
COPY gradlew build.gradle.kts settings.gradle.kts gradle.properties* ./
COPY gradle ./gradle
RUN chmod +x ./gradlew

# 把依赖拉下来，形成缓存层
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon dependencies || true

# 再拷贝源码
COPY src ./src

# 构建
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon clean bootJar

# -------- runtime stage --------
FROM azul/zulu-openjdk:24-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -Dfile.encoding=UTF-8"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
