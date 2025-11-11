FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY target/TrainingMicroservice-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

ENV EUREKA_URI=http://eureka-server:8761/eureka
ENV SPRING_PROFILES_ACTIVE=default
ENV ACTIVEMQ_BROKER_URL=tcp://activemq:61616
ENV ACTIVEMQ_USER=admin
ENV ACTIVEMQ_PASSWORD=admin

ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=8081"]
