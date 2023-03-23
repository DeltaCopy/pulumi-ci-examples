FROM ubuntu:latest

ARG user=jenkins
ARG group=jenkins
ARG REF=/usr/share/jenkins/ref
ARG JENKINS_HOME=/var/jenkins_home
ARG PULUMI_VERSION=latest
ARG uid=1000
ARG gid=1000
ARG http_port=8080
ARG agent_port=50000

ENV JENKINS_SLAVE_AGENT_PORT ${agent_port}
ENV REF $REF
ENV JENKINS_HOME $JENKINS_HOME

ENV LC_ALL C.UTF-8
ENV LANG C.UTF-8

RUN mkdir -p /etc/pki/tls/certs

RUN apt-get update -qq

RUN apt-get install wget gnupg curl default-jdk python3-venv python3 python3-pip  -qq


RUN wget -q -O - https://pkg.jenkins.io/debian/jenkins.io.key | apt-key add -


#RUN sh -c 'echo https://pkg.jenkins.io/debian binary/ > \
#    /etc/apt/sources.list.d/jenkins.list'

RUN sh -c 'echo deb https://pkg.jenkins.io/debian-stable binary/ > \
    /etc/apt/sources.list.d/jenkins.list'

RUN apt-key adv --keyserver keyserver.ubuntu.com --recv-keys 9B7D32F2D50582E6

RUN apt-get update -qq

RUN apt-get install jenkins -qq

ENV PATH=$PATH:/root/.pulumi/bin

RUN curl -fsSL https://get.pulumi.com/ | sh

RUN pip3 install grpcio pipenv pulumi-aws

RUN mkdir -p ${REF}/init.groovy.d ${JENKINS_HOME}

RUN chown -R ${user}:${group} "$JENKINS_HOME" "$REF"

RUN update-ca-certificates && cd /etc/pki/tls/certs/ && ln -s /etc/ssl/certs/ca-certificates.crt ca-bundle.crt

COPY entrypoint.sh /



ENTRYPOINT [ "/entrypoint.sh" ]
