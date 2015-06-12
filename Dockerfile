FROM jetty:latest

MAINTAINER Nikos Fountas "nikfoundas@gmail.com"

# ADD ./target/*.war /var/lib/jetty/webapps/ROOT.war

ADD https://github.com/nikfoundas/etcd-viewer/releases/download/v1.0/etcd-viewer-1.0.war /var/lib/jetty/webapps/ROOT.war

RUN chmod 644 /var/lib/jetty/webapps/ROOT.war
RUN chown jetty:jetty /var/lib/jetty/webapps/ROOT.war
