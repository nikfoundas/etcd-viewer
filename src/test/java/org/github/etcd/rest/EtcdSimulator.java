package org.github.etcd.rest;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

public class EtcdSimulator {

    public static void main(String[] args) {
        EtcdSimulator simulator = new EtcdSimulator();
        simulator.start();
    }

    private EtcdResource etcdResource;
    private Server endpointServer;

    public void start() {
        JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
        sf.setResourceClasses(EtcdResource.class);

        ObjectMapper mapper = new ObjectMapper();

        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        sf.setProvider(new JacksonJsonProvider(mapper));

        etcdResource = new EtcdResourceImpl();

        sf.setResourceProvider(EtcdResource.class, new SingletonResourceProvider(etcdResource));

        sf.setAddress("http://localhost:2379/");

        endpointServer = sf.create();

        endpointServer.start();
    }

    public void stop() throws Exception {
        if (endpointServer != null) {
            endpointServer.stop();
            endpointServer.destroy();
        }
        if (etcdResource != null) {
            etcdResource.close();
        }
    }
}
