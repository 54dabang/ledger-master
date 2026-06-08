ARG DOCKER_MIRROR=docker.1ms.run
FROM ${DOCKER_MIRROR}/library/nginx:1.27-alpine

COPY conf.d/default.conf /etc/nginx/conf.d/default.conf
COPY html/ /usr/share/nginx/html/
