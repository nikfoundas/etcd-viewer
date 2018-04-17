FROM jetty:9.4-jre8

MAINTAINER Nikos Fountas "nikfoundas@gmail.com"


# select the release to download
#ENV VIEWER_VERSION latest
#ENV VIEWER_VERSION tags/v1.1

#Download release package
#RUN curl --silent -X GET https://api.github.com/repos/nikfoundas/etcd-viewer/releases/${VIEWER_VERSION} | sed -n 's|.*\"browser_download_url\": \"\(.*\)\".*|\1|p' > /tmp/etcd-viewer-release-archive
#RUN curl --silent -L `cat /tmp/etcd-viewer-release-archive` > /var/lib/jetty/webapps/ROOT.war

#set maven env
ENV M2_HOME /opt/maven
ENV M2 $M2_HOME/bin
ENV PATH $M2:$PATH
USER root

RUN apt update && \
    apt install openjdk-8-jdk -y && \
    wget -P /opt/ http://www-eu.apache.org/dist/maven/maven-3/3.5.3/binaries/apache-maven-3.5.3-bin.tar.gz && \
    tar -zxvf /opt/apache-maven-3.5.3-bin.tar.gz -C /opt && \
    ln -s /opt/apache-maven-3.5.3 /opt/maven
    # sed '$a export M2_HOME=/opt/maven' /etc/profile && \
    # sed '$a export M2=$M2_HOME/bin' /etc/profile && \
    # sed '$a export PATH=$M2:$PATH' /etc/profile

#build etcd viewer with github code
RUN wget https://github.com/nikfoundas/etcd-viewer/archive/master.zip && \
    unzip master.zip && \
    cd etcd-viewer-master && \
    mvn package && \
    cp /var/lib/jetty/etcd-viewer-master/target/etcd-viewer-1.3-SNAPSHOT.war /var/lib/jetty/webapps/ROOT.war


# To build your own local custom etcd viewer docker image comment the above RUN commands and uncomment the following
# ADD ./target/*.war /var/lib/jetty/webapps/ROOT.war
# USER jetty
RUN chmod 644 /var/lib/jetty/webapps/ROOT.war
RUN chown jetty:jetty /var/lib/jetty/webapps/ROOT.war
