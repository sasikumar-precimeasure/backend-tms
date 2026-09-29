# Multi-stage build: compile with the Gradle wrapper, run on a minimal JRE.
# Optional packaging path for clients who prefer containers - `java -jar`
# against the plain executable jar (see README.md) works identically
# without Docker at all, which is the default path for an on-premises
# install with no container runtime.

FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app
COPY gradlew build.gradle settings.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew --version
COPY src ./src
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN useradd --system --create-home tms
COPY --from=build /app/build/libs/*.jar app.jar
USER tms
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
