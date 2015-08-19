# etcd-viewer

[![Build Status](https://travis-ci.org/nikfoundas/etcd-viewer.png?branch=master)](https://travis-ci.org/nikfoundas/etcd-viewer)

The `etcd-viewer` is a java web application that allows you to navigate and modify [etcd][etcd] distributed key-value stores.

The application uses the [etcd rest API][etcd-api] to communicate with the underlying key-value store.

## Getting Started

![navigation screen](http://nikfoundas.github.io/etcd-viewer/screenshots/navigation.png)


### Run with docker

The easiest way to get etcd-viewer is to run a [docker][docker] container with the etcd-viewer image:

```docker run -d -p 8080:8080 nikfoundas/etcd-viewer```

The docker container will deploy the etcd-viewer war file to a [jetty][jetty] servlet container.
The docker image is built on top of the jetty:latest image.


### Build etcd-viewer

In order to build `etcd-viewer` you need java-1.7 and [maven][maven] 3.0.5 or later.

* Clone the etcd-viewer repository

```git clone https://github.com/nikfoundas/etcd-viewer.git```

* Build the project with maven

```mvn clean install```

* deploy war file to some servlet container ([jetty][jetty] or [tomcat][tomcat])

* Alternatively you can start the etcd-viewer with maven jetty plugin

```mvn jetty:run```

* or even build a docker image using the provided Dockerfile

```docker build -t some-repo/etcd-viewer .```


### Embed components within your wicket application

If your application is built with [wicket][wicket] you can also use the
components - panels from etcd-viewer to embed the browsing - editing
functionality in your application.

## Features

### Supported etcd API

Etcd viewer supports the following actions via the [etcd rest API][etcd-api]:

* Get version
* Get node self statistics which contains the leader information
* Retrieve machines that participate in the etcd cluster.
* Create directories and key-value pairs with optionally providing
time to live (TTL)
* Retrieve directories and key value nodes
* Update key-value pairs
* Delete directories recursively
* Delete key-value pairs

![add node](http://nikfoundas.github.io/etcd-viewer/screenshots/add-node.png)

![confirm delete](http://nikfoundas.github.io/etcd-viewer/screenshots/confirm-delete.png)

### Multiple etcd key-value stores

You can navigate and modify multiple etcd key-value stores without
having to run the application more than once. The etcd clusters are
stored in memory and they are forgotten after the application is
restarted.

You can add and remove etcd key-value stores at any time. Removing
an etcd registry from the etcd-viewer does not affect the contents
of the registry itself.

![multiple clusters](http://nikfoundas.github.io/etcd-viewer/screenshots/add-registry.png)

### Leader auto detection

In order to add an etcd cluster registry you only need to provide one
alive etcd host. The application discovers the rest of the cluster
hosts and detects the leader node. All reads and writes are then
directed to the leader node to avoid redirections.

### Etcd cluster monitor

Etcd viewer enables you to view the machines that participate in the
etcd cluster along with their status - leader or follower. If some
etcd host is not accessible then it is marked with red to indicate
that it is down. Note that for single node etcd registries no
status is reported by etcd.

![cluster monitor](http://nikfoundas.github.io/etcd-viewer/screenshots/view-cluster.png)

### Responsive css

`etcd-viewer` uses [bootstrap][bootstrap] css framework to enable key-value
storage accessibility even from mobile devices or tablets.

![responsive](http://nikfoundas.github.io/etcd-viewer/screenshots/responsive.png)

## Next steps

* Provide feedback on communication or etcd api errors (partially done)
* Provide log console to record modifications applied
* Extend functionality to view and modify [fleet][fleet] unit information
* Use thread safe CXF jax-rs api (done)
* Support client side basic authentication for etcd with authentication enabled. Check [etcd auth api](https://github.com/coreos/etcd/blob/master/Documentation/auth_api.md). (etcd 2.1.x and later)
* Support user and role management (etcd 2.1.x and later)
* Provide documentation and hooks to import etcd client certificates

## About

`etcd-viewer` uses the following open source libraries and frameworks:

* [Apache wicket 6.19.0][wicket]: Open source Java web framework
* [Apache CXF 3.0.1][cxf]: To handle the etcd rest API
* [FasterXML Jackson 2.5.3][jackson]: To marshal/unmarshal JSON objects to POJOs
* [Google Guice 3.0][guice]: For dependency injection
* [Bootstrap 3.3.4][bootstrap]: Responsive CSS framework
* [jQuery 1.11.2][jquery]: Open source JavaScript library
* [FontAwesome 4.3.0][fontawesome]: Iconic web font

![about](http://nikfoundas.github.io/etcd-viewer/screenshots/about.png)

## License

etcd-viewer is released under the Apache 2.0 license. See the [LICENSE](http://nikfoundas.github.io/etcd-viewer/LICENSE) file for details.

[etcd]: https://github.com/coreos/etcd
[etcd-api]: https://github.com/coreos/etcd/blob/master/Documentation/api.md
[docker]: https://www.docker.com/
[maven]: http://maven.apache.org
[wicket]: http://wicket.apache.org/
[bootstrap]: http://getbootstrap.com/
[fontawesome]: http://fortawesome.github.io/Font-Awesome/
[cxf]: http://cxf.apache.org
[jackson]: https://github.com/FasterXML/jackson
[guice]: https://github.com/google/guice
[jetty]: http://www.eclipse.org/jetty/
[tomcat]: http://tomcat.apache.org
[fleet]: https://github.com/coreos/fleet
[jquery]: https://jquery.com/
