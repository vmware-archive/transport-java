FROM skyscraper-docker-local.artifactory.eng.vmware.com/vmc-base-photon:1.3

ARG commitId
ARG build

LABEL commitId=$commitId build=$build

RUN mkdir -p /opt/app
WORKDIR /opt/app
COPY dist/sample-ui /opt/app/dist

# Install the dependencies for angular app
RUN tdnf -y install nginx tar

# forward request and error logs to docker log collector
RUN ln -sf /dev/stdout /var/log/nginx/access.log \
	&& ln -sf /dev/stderr /var/log/nginx/error.log

EXPOSE 80
ADD ./nginx.conf  /etc/nginx/
RUN ls -la /opt/app/dist

STOPSIGNAL SIGTERM

CMD ["-g", "daemon off;"]

ENTRYPOINT ["nginx"]

