FROM node:24-bookworm-slim AS node

FROM eclipse-temurin:25-jdk

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        bash \
        ca-certificates \
        curl \
        dirmngr \
        git \
        gnupg \
        openssh-client \
        procps \
        unzip \
        zip \
    && rm -rf /var/lib/apt/lists/*

COPY --from=node /usr/local/bin/node /usr/local/bin/node
COPY --from=node /usr/local/bin/npm /usr/local/bin/npm
COPY --from=node /usr/local/bin/npx /usr/local/bin/npx
COPY --from=node /usr/local/bin/corepack /usr/local/bin/corepack
COPY --from=node /usr/local/lib/node_modules /usr/local/lib/node_modules

WORKDIR /workspace

ENV GRADLE_USER_HOME=/workspace/.gradle-docker
ENV npm_config_cache=/workspace/.npm
ENV CI=true
ENV NPM_CONFIG_FUND=false
ENV NPM_CONFIG_AUDIT=false

COPY gradlew gradlew.bat settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle ./gradle
COPY docker/nows-entrypoint.sh /usr/local/bin/nows-entrypoint

RUN chmod +x ./gradlew /usr/local/bin/nows-entrypoint

COPY . .

ENTRYPOINT ["nows-entrypoint"]
CMD ["./gradlew", "--no-parallel", "--max-workers=1", "dist"]
