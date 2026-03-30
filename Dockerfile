FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
# COPY build/libs/*.jar /app/blog.jar  –– use this if you want image to be constructed from source build with Gradle
COPY target/*.jar /app/blog.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/blog.jar"]
