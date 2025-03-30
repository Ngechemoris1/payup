#!/bin/bash

: ${PORT:=5432} # Default PostgreSQL port
: ${VERSION:=latest} # PostgreSQL version
: ${APP:=postgres} # PostgreSQL image name
: ${REGISTRY:=postgres} # PostgreSQL image registry

TAG=$APP:$VERSION
CONTAINER_NAME=payup

# Stop and remove existing container
docker stop $CONTAINER_NAME
docker rm $CONTAINER_NAME

# Run PostgreSQL container
docker run -d -p $PORT:5432 \
-e POSTGRES_PASSWORD=payup## \ 
-e POSTGRES_USER=root \   
-e POSTGRES_DB=payup \ 
-e TZ=Africa/Nairobi \  
-v $(pwd)/accounts/data:/var/lib/postgresql/data \ # Mount data directory
--name $CONTAINER_NAME $REGISTRY/$TAG

# Stream container logs
docker logs -f $CONTAINER_NAME
