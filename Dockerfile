FROM eclipse-temurin:17
WORKDIR /app
COPY . .
RUN ./mvnw package
CMD ["java","-jar","target/trainbrella-0.0.1.jar"]
