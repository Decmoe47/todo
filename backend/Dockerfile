# backend/Dockerfile
FROM azul/zulu-openjdk:24-jre

WORKDIR /app

COPY build/libs/*.jar /app/app.jar

EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -Dfile.encoding=UTF-8"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
