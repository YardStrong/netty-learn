FROM registry01.yardstrong.online/docker.io/openjdk:8-jre-alpine

ADD target/netty-connector-packages /app

WORKDIR /app

ENTRYPOINT ["java","-cp", "/app/libs/*", "online.yardstrong.netty.CustomNettyServer"]