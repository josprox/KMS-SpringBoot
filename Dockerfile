# Stage 1: Build vlmcsd
FROM alpine:latest AS vlmcsd-builder
WORKDIR /root
RUN apk add --no-cache git make build-base && \
    git clone --branch master --single-branch https://github.com/Wind4/vlmcsd.git && \
    cd vlmcsd && \
    make

# Stage 2: Build Spring Boot app
FROM eclipse-temurin:21-jdk-alpine AS app-builder
WORKDIR /app
COPY . .
# Fix line endings for gradlew if on Windows
RUN apk add --no-cache dos2unix && dos2unix gradlew && chmod +x gradlew
RUN ./gradlew bootJar -x test

# Stage 3: Final image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install dependencies
RUN apk add --no-cache tzdata

# Copy vlmcsd binary and database
COPY --from=vlmcsd-builder /root/vlmcsd/bin/vlmcsd /usr/local/bin/vlmcsd
COPY --from=vlmcsd-builder /root/vlmcsd/etc/vlmcsd.kmd /usr/local/bin/vlmcsd.kmd

# Copy Spring Boot jar
COPY --from=app-builder /app/build/libs/*.jar app.jar

# Environment variables
ENV KMS_VLMCSD_PATH=/usr/local/bin/vlmcsd

# Copy entrypoint script
COPY entrypoint.sh .
RUN chmod +x entrypoint.sh

# Dashboard and KMS unified port
EXPOSE 80

ENTRYPOINT ["./entrypoint.sh"]
