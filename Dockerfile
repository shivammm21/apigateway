# Build stage
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app
COPY . .
RUN ./mvnw -q -DskipTests package

# Run stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
ENV JAVA_OPTS=""
ENV REDIS_HOST=redis
COPY --from=build /app/target/apigateway-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8767
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
