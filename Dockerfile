FROM jetty:latest

MAINTAINER Nikos Fountas "nikfoundas@gmail.com"

ADD ./target/*.war /var/lib/jetty/webapps/ROOT.war