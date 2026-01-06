# Bước 1: Build dự án bằng Maven
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
# Lệnh này sẽ tạo ra file .jar trong thư mục target, bỏ qua test để build nhanh hơn
RUN mvn clean package -DskipTests

# Bước 2: Chạy ứng dụng bằng JDK rút gọn (cho nhẹ)
FROM openjdk:21-jdk-slim
WORKDIR /app
# Copy file .jar vừa build ở trên sang (dùng dấu * để không cần quan tâm tên version)
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]