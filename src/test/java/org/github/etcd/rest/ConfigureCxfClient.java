package org.github.etcd.rest;

import org.apache.cxf.jaxrs.client.Client;
import org.apache.cxf.jaxrs.client.ClientConfiguration;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transports.http.configuration.ConnectionType;
import org.github.etcd.service.impl.CachingResourceProxyFactory;
import org.github.etcd.service.rest.EtcdApiResource;
import org.github.etcd.service.rest.EtcdResponse;

public class ConfigureCxfClient {

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
//      WebClient client = WebClient.create("http://localhost:4001/");

      CachingResourceProxyFactory factory = new CachingResourceProxyFactory();

      EtcdApiResource proxy = factory.createProxy("http://localhost:4001/", EtcdApiResource.class);

      Client client = WebClient.client(proxy);

      ClientConfiguration config = WebClient.getConfig(client);

      config.getHttpConduit().getClient().setConnection(ConnectionType.KEEP_ALIVE);
//      config.getHttpConduit().getClient().setMaxRetransmits(value)


//      javax.ws.rs.client.Client cli2 = ClientBuilder.newClient();
//      cli2.property("http.connection.timeout", 10000);
//      cli2.property("http.receive.timeout", 10000);
//
//      JAXRSClientFactory.fromClient(WebClient.client(cli2), EtcdResource.class);
//      cli2.property(name, value)

      for (int i=0; i<1000; i++) {
          EtcdResponse resp = proxy.getNode("/");
//          client.close();
//          cli.reset();
          System.out.println("Version #"+i+": " + resp);
      }

      Thread.sleep(30000);
    }

}
