FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY build/libs/*.jar /app/blog.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/blog.jar"]
