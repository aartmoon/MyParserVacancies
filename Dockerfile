FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -DskipTests -T 1C

FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar
RUN java -version

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
