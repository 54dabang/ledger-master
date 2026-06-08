ARG DOCKER_MIRROR=docker.1ms.run
FROM ${DOCKER_MIRROR}/library/mysql:8.0.36

ENV TZ=Asia/Shanghai

COPY conf.d/ /etc/mysql/conf.d/
COPY initdb/ /docker-entrypoint-initdb.d/
