# 构建阶段
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 运行阶段
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/smartmix-backend-*.jar /app/smartmix-backend.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/smartmix-backend.jar"]
