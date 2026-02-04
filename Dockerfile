FROM eclipse-temurin:17-jre-jammy

WORKDIR /app
COPY target/*.jar app.jar

ENV TZ=Asia/Jakarta
EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]