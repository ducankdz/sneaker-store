# Stage 1: Build Spring Boot application
FROM maven:3-openjdk-17 AS build
WORKDIR /app

# Copy source code vào container
COPY . .

# Build project (tạo file .jar trong thư mục target)
RUN mvn clean package -DskipTests

# Stage 2: Run ứng dụng
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy file .jar từ stage build
COPY --from=build /app/target/*.jar app.jar

# Mở cổng 8080
EXPOSE 8080

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=${PORT}"]

