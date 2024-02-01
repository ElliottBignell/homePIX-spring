FROM openjdk:17-jdk-slim as builder
WORKDIR application
ADD maven/spring-homepix-2.4.0.BUILD-SNAPSHOT.jar ./
RUN java -Djarmode=layertools -jar spring-homepix-2.4.0.BUILD-SNAPSHOT.jar extract

FROM openjdk:17-jdk-slim
WORKDIR application

# Start with the OpenJDK 17 slim image
FROM openjdk:17-jdk-slim

# Install necessary packages for AWT headless mode
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    fontconfig \
    libfreetype6 \
    && rm -rf /var/lib/apt/lists/*

# Set the JAVA_OPTS environment variable to enforce headless mode
ENV JAVA_OPTS="-Djava.awt.headless=true"

# Your application setup follows here...

COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./

RUN mkdir -p /application/images

EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "org.springframework.boot.loader.JarLauncher"]

