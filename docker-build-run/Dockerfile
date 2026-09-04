FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app

RUN apt update && apt install -y make

COPY . .

RUN chmod +x mvnw

RUN make package-only

FROM eclipse-temurin:17-jre

RUN apt update \
    && apt install -y --no-install-recommends \
        libxext6 \
        libxrender1 \
        libxtst6 \
        x11-utils \
    && rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/hospital-scheduler-app/target/*-jar-with-dependencies.jar /app/app.jar