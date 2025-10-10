FROM eclipse-temurin:25-jdk AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src
COPY gradle.lockfile gradle.lockfile

RUN chmod +x gradlew
RUN ./gradlew build -x test -x spotlessJava -x spotlessCheck

FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=builder /app/build/libs/isldevs-standalone.jar app.jar

EXPOSE 8443

ENV SPRING_PROFILES_ACTIVE=prod
ENV ISSUER_URI=https://localhost:8443/api/v1
ENV DB_HOST=db
ENV DB_PORT=5432
ENV DB_NAME=isldevs_db
ENV DB_USERNAME=postgres
ENV DB_PASSWORD=password

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "--enable-preview", "app.jar"]