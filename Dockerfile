## Dockerfile for Timi
## Inspired by Dockerfile for docker-gitlab by sameersbn

FROM openjdk:8
MAINTAINER bill.ota@billo.systems

ENV TIMI_VERSION=1.0.0 \
    LEIN_VERSION=2.7.1 \
    TIMI_INSTALL_DIR="/opt/timi/timi" \
    TIMI_DATA_DIR="/opt/timi/data"

WORKDIR ${TIMI_INSTALL_DIR}
RUN cd ${TIMI_INSTALL_DIR}

RUN curl -sL https://deb.nodesource.com/setup_6.x | bash -

RUN apt update && apt install -y curl nodejs

## Install leiningen

# Allow installation as root
ENV LEIN_ROOT=true

# Install leiningen and move to /usr/bin
RUN wget -q -O /usr/bin/lein https://raw.githubusercontent.com/technomancy/leiningen/${LEIN_VERSION}/bin/lein
RUN chmod u=rxw,g=rx,o=rx /usr/bin/lein

## Copy required assets
COPY project.clj ${TIMI_INSTALL_DIR}/project.clj
COPY src ${TIMI_INSTALL_DIR}/src
COPY resources ${TIMI_INSTALL_DIR}/resources
COPY config ${TIMI_INSTALL_DIR}/config
COPY dev ${TIMI_INSTALL_DIR}/dev
COPY bin ${TIMI_INSTALL_DIR}/bin
COPY package.json ${TIMI_INSTALL_DIR}/package.json
COPY Gruntfile.js ${TIMI_INSTALL_DIR}/

## Install client dependencies
RUN npm install && ./node_modules/.bin/grunt copy

## Build assets
RUN lein cljsbuild once

## Create entrypoint through which commands can be executed
COPY docker/entrypoint.sh /sbin/entrypoint.sh
RUN chmod +x /sbin/entrypoint.sh

EXPOSE 3000/tcp

VOLUME ["${TIMI_DATA_DIR}"]
ENTRYPOINT ["/sbin/entrypoint.sh"]

CMD ["app:start"]
