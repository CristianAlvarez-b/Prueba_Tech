# Imagen oficial de Amazon Corretto
FROM amazoncorretto:21.0.4-alpine3.18

WORKDIR /app

# Copiar JAR compilado
COPY target/app-0.0.1-SNAPSHOT.jar app.jar

# Exponer puerto del backend
EXPOSE 8080

# Ejecutar aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
