FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -q
COPY src/ src/
ENTRYPOINT ["./mvnw", "spring-boot:run"]
