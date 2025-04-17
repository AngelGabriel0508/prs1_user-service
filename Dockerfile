# Etapa 1: Construcción del proyecto
FROM maven:3.9.4-eclipse-temurin-17-alpine AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa 2: Imagen final para producción
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copia el JAR generado desde la etapa anterior
COPY --from=builder /app/target/*.jar app.jar

# Expone el puerto del microservicio
EXPOSE 8082

# Comando para ejecutar Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]
