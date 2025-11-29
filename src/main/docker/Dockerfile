# Stage 1: Build and install dependencies
FROM eclipse-temurin:17-jdk as builder
WORKDIR /application
ADD maven/spring-homepix-2.4.0.BUILD-SNAPSHOT.jar ./
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    wget tar \
    fontconfig \
    libfreetype6 \
    && rm -rf /var/lib/apt/lists/*
RUN java -Djarmode=layertools -jar spring-homepix-2.4.0.BUILD-SNAPSHOT.jar extract

# Download WebP binaries from Google
RUN wget https://storage.googleapis.com/downloads.webmproject.org/releases/webp/libwebp-1.3.2-linux-x86-64.tar.gz -O webp.tar.gz \
    && tar -xzf webp.tar.gz \
    && cp libwebp-1.3.2-linux-x86-64/bin/cwebp /usr/local/bin/cwebp

# Verify cwebp is installed
RUN /usr/local/bin/cwebp -version

# Stage 2: Copy libraries and application into the distroless image
FROM gcr.io/distroless/java17-debian11
WORKDIR /application

# Copy WebP binary from the builder stage
COPY --from=builder /usr/local/bin/cwebp /usr/local/bin/cwebp

# Copy required system libraries from builder stage
COPY --from=builder /usr/lib/x86_64-linux-gnu/libfreetype.so.6 /usr/lib/x86_64-linux-gnu/
COPY --from=builder /usr/lib/x86_64-linux-gnu/libfontconfig.so.1 /usr/lib/x86_64-linux-gnu/

# Copy Spring Boot application layers
COPY --from=builder /application/dependencies/ ./
COPY --from=builder /application/spring-boot-loader/ ./
COPY --from=builder /application/snapshot-dependencies/ ./
COPY --from=builder /application/application/ ./

# Expose the application port and set entrypoint
EXPOSE 8080
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "org.springframework.boot.loader.JarLauncher"]
