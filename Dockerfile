# Crea una imagen de Docker para la aplicación Spring Boot
FROM openjdk:23 as builder

# Directorio de trabajo
WORKDIR /app

# Copy the JAR file into the container at /app
COPY ../build/libs/userManager-v1.0.jar /app/app.jar

# Comando para ejecutar la aplicación Spring Boot
CMD ["java", "-jar", "/app/app.jar"]

ENV PORT=3002
ENV DB_USER=postgres
ENV DB_PASSWORD=postgres
ENV DB_HOST=postgres_routes
ENV DB_PORT=5432
ENV DB_NAME=routes
ENV USER_ME_ENDPOINT=http://localhost:3000/users/me

EXPOSE $PORT