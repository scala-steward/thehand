FROM ubuntu:18.04

LABEL maintainer "Jeison Cardoso"
LABEL image_type "The hand a svn data explorer"

ARG DOC_ROOT
ENV DOC_ROOT /var/www/handserver
ARG SCALA_VERSION
ENV SCALA_VERSION ${SCALA_VERSION:-2.13.1}
ARG SBT_VERSION
ENV SBT_VERSION ${SBT_VERSION:-1.3.0}

RUN apt-get update \
    && apt-get upgrade -y \
    && apt-get install -y \
    curl

RUN \
  curl -L -o sbt-$SBT_VERSION.deb https://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
  dpkg -i sbt-$SBT_VERSION.deb && \
  rm sbt-$SBT_VERSION.deb && \
  apt-get update && \
  apt-get install -y \
  sbt \
  openjdk-11-jre \
  git \
  && rm -rf /var/lib/apt/lists/*

# # Add and use user sbtuser
# RUN groupadd --gid 1001 sbtuser && useradd --gid 1001 --uid 1001 sbtuser --shell /bin/bash
# RUN chown -R sbtuser:sbtuser /opt
# RUN mkdir /home/sbtuser && chown -R sbtuser:sbtuser /home/sbtuser
# RUN mkdir /logs && chown -R sbtuser:sbtuser /logs
# USER sbtuser

WORKDIR ${DOC_ROOT}

RUN git clone https://github.com/0um/thehand.git

ADD prodution_config/application.conf /var/www/handserver/conf/aplication.conf

EXPOSE 9000

CMD sbt run
# CMD sbt test