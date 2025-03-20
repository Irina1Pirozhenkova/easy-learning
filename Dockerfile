# Stage 1: сборка приложения с помощью Maven
FROM maven:3-openjdk-17 AS builder
WORKDIR /app
# Копируем файлы pom.xml и src, чтобы затем выполнить сборку
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
# Сборка приложения, пропуская тесты
RUN mvn clean package -DskipTests

# Stage 2: запуск приложения на легковесном образе OpenJDK
FROM openjdk:17-jdk-slim
WORKDIR /app
# Копируем скомпилированный jar из стадии сборки
COPY --from=builder /app/target/easy_learning-0.0.1-SNAPSHOT.jar app.jar

# Загружаем скрипт wait-for-it и делаем его исполняемым
ADD https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh

EXPOSE 8080
# Ожидаем, пока база MySQL станет доступной, затем запускаем приложение
ENTRYPOINT ["/wait-for-it.sh", "mysql:3306", "--", "java", "-jar", "app.jar"]

