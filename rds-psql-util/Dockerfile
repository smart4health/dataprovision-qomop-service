FROM ubuntu:20.04
ARG AWS_CLI_VERSION=2.0.30
RUN apt-get update && apt-get upgrade -y && apt-get install -y \
    postgresql-client-12 \
    curl \
    unzip \
    jq
RUN curl -s "https://awscli.amazonaws.com/awscli-exe-linux-x86_64-$AWS_CLI_VERSION.zip" -o "awscliv2.zip" \
    && unzip awscliv2.zip \
    && ./aws/install \
    && rm awscliv2.zip
COPY drone-plugin.sh /
COPY load-conn.sh /
ENTRYPOINT ["bash", "/drone-plugin.sh"]