FROM jetty:9.2.12-jre7

MAINTAINER Nikos Fountas "nikfoundas@gmail.com"

# select the release to download
ENV VIEWER_VERSION latest
#ENV VIEWER_VERSION tags/v1.1

RUN curl --silent -X GET https://api.github.com/repos/nikfoundas/etcd-viewer/releases/${VIEWER_VERSION} | sed -n 's|.*\"browser_download_url\": \"\(.*\)\".*|\1|p' > /tmp/etcd-viewer-release-archive
RUN curl --silent -L `cat /tmp/etcd-viewer-release-archive` > /var/lib/jetty/webapps/ROOT.war

# To build your own local custom etcd viewer docker image comment the above RUN commands and uncomment the following
# ADD ./target/*.war /var/lib/jetty/webapps/ROOT.war

RUN chmod 644 /var/lib/jetty/webapps/ROOT.war
RUN chown jetty:jetty /var/lib/jetty/webapps/ROOT.war
