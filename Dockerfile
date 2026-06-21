# Etapa de construcción
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B
COPY src ./src
RUN ./mvnw package -DskipTests -B

# Etapa de ejecución
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S planillas && adduser -S planillas -G planillas
COPY --from=build /app/target/*.jar app.jar
RUN mkdir -p /app/uploads && chown planillas:planillas /app/uploads
USER planillas
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
