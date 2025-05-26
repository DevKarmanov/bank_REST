FROM maven:latest AS build

WORKDIR /app

COPY . /app

RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-alpine

WORKDIR /app

COPY --from=build /app/target/Bank_REST-0.0.1-SNAPSHOT.jar /app/bank_rest.jar

CMD ["java", "-jar", "bank_rest.jar"]

EXPOSE 8083
