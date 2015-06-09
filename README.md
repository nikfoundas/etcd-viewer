# etcd-browser

The `etcd-browser` is a java web application that allows you to navigate and modify etcd distributed key-value stores.

The application uses the ETCD v2.0 Rest API to communicate with the underlying key-value store.

## Getting Started


### Run with docker

The easiest way to get etcd-browser is to build a docker image and run the application as a container:

```docker run -d -p 8080:8080 nikfoundas/etcd-browser```

The docker image is built on top of the jetty:latest image by deploying the etcd-browser war file to jetty.


### Build

In order to build `etcd-browser` you need java-1.7 and maven 3.0.5 or later.

* Clone this repository

* mvn clean install

* mvn jetty:run

* deploy war file to some servlet container (jetty or tomcat)

* or even build customized docker image to fit your environment.



### Embed components within your wicket application


## Features

### Supported etcd API

Etcd browser supports the following actions via the rest etcd api:

* Get version
* Get node self statistics which contains the leader information
* Retrieve machines that participate in the etcd consensus.
* Create directories and key-value pairs with optionally providing
time to live (TTL)
* Retrieve directories and key value nodes
* Update key-values
* Delete directories recursively
* Delete key-value pairs


### Multiple etcd key-value stores

You can navigate and modify multiple etcd key-value stores without
having to run the application more than once. The etcd clusters are
stored in memory and they are forgotten after the application is
restarted.

### Leader auto detection

In order to add an etcd cluster registry you only need to provide one
alive etcd host. The application discovers the rest of the cluster
hosts and detects the leader node. All reads and writes are then
directed to the leader node to avoid redirections.

### Etcd cluster monitor

### Responsive html/css

Etcd browser uses twitter bootstrap css framework to enable key-value
storage accessibility even from mobile devices or tablets.

### Connect to an etcd cluster

### Navigate key value entries

### Modify directory structures and values
