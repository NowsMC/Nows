FROM eclipse-temurin:25-jdk

RUN apt-get update \
    && apt-get install -y --no-install-recommends git openssh-client ca-certificates bash \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace

ENV GRADLE_USER_HOME=/workspace/.gradle-docker

COPY gradlew gradlew.bat settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle ./gradle

RUN chmod +x ./gradlew

COPY . .

CMD ["./gradlew", "publishLayout"]
