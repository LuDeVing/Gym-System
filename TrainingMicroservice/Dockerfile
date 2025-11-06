FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app
COPY target/TrainingMicroservice-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENV EUREKA_URI=http://eureka-server:8761/eureka

ENTRYPOINT ["java", "-jar", "app.jar"]
