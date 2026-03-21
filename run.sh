#!/bin/bash

MAX_HEALTHCHECK_RETRIES=5
readonly MAX_HEALTHCHECK_RETRIES

RESET_DATABASE=true # Set true if you want to have a clean database when deploying the app
readonly RESET_DATABASE

echo "Deploy yp-blog-backend started..."

echo "Step 1: Building war archive..."
chmod +x gradlew
./gradlew clean war
if [ $? -ne 0 ]; then
    echo "Gradle build failed"
    exit 1
fi

VOLUME_FLAG=""
if [ "$RESET_DATABASE" = true ]; then
    echo "Step 2: Cleaning up previous deployment (WIPING VOLUMES)..."
    VOLUME_FLAG="-v"
else
    echo "Step 2: Cleaning up previous deployment (PRESERVING VOLUMES)..."
fi

docker-compose down $VOLUME_FLAG

echo "Step 3: Building and starting containers..."
docker-compose up --build -d
if [ $? -ne 0 ]; then
    echo "Docker compose failed, cleaning up containers..."
    docker-compose down $VOLUME_FLAG
    exit 1
fi

echo "Step 4: Waiting for database to become healthy..."

RETRY_COUNT=0
until [ "$(docker inspect --format='{{json .State.Health.Status}}' yp-blog-db)" == "\"healthy\"" ]; do

    if [ "$RETRY_COUNT" -gt "$MAX_HEALTHCHECK_RETRIES" ]; then
        docker-compose down $VOLUME_FLAG
        echo "Max healthcheck retries exceeded"
        exit 1
    fi

    RETRY_COUNT=$((RETRY_COUNT + 1))

    printf "."
    sleep 2

done

echo "Step 5: Removing intermediate build layers and dangling images..."
docker image prune -f

echo -e "Deployment Complete!"
echo "----------------------------------------"
echo "App URL:      http://localhost:8080/"
echo "----------------------------------------"
