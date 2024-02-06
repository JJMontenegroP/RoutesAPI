# Crea una imagen de Docker para la aplicación Spring Boot
FROM openjdk:21-jdk-bookworm

# Directorio de trabajo
WORKDIR /app

# Copy the JAR file into the container at /app
COPY build/libs/userManager-v1.0.jar /app/app.jar

# Comando para ejecutar la aplicación Spring Boot
CMD ["java", "-jar", "/app/app.jar"]