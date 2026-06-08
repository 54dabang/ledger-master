ARG DOCKER_MIRROR=docker.1ms.run
FROM ${DOCKER_MIRROR}/library/eclipse-temurin:11-jdk-jammy

ENV TZ=Asia/Shanghai \
    SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-Xms512m -Xmx1024m"

WORKDIR /app

RUN groupadd -r ledger \
    && useradd -r -g ledger -d /home/ledger -s /usr/sbin/nologin ledger \
    && mkdir -p /home/ledger/uploadPath /home/ledger/html/download /home/ledger/jar/logs \
    && chown -R ledger:ledger /home/ledger /app

COPY --chown=ledger:ledger ledger-admin.jar /app/ledger-admin.jar

USER ledger
EXPOSE 7070

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app/ledger-admin.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE}"]
