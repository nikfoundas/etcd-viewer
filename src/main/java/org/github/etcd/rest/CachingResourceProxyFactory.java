package org.github.etcd.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.cxf.jaxrs.client.Client;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

public class CachingResourceProxyFactory implements ResourceProxyFactory {

    private ConcurrentMap<CacheKey, Object> clientCache = new ConcurrentHashMap<>();

    private List<Object> providers;

    public CachingResourceProxyFactory() {

        providers = new ArrayList<>();

        ObjectMapper mapper = new ObjectMapper();

//      mapper.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, false);
//      mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        providers.add(new JacksonJsonProvider(mapper));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T createProxy(String baseAddress, Class<T> serviceType) {
        System.out.println("CachingWebClientProxyFactory.createProxy() " + baseAddress);

        CacheKey key = new CacheKey(baseAddress, serviceType);

        if (clientCache.containsKey(key)) {
            return (T) clientCache.get(key);
        } else {
            System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$ CREATING PROXY FOR: " + baseAddress);
            T proxy = JAXRSClientFactory.create(baseAddress, serviceType, providers);
            clientCache.putIfAbsent(key, proxy);
            return proxy;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> void closeProxy(String baseAddress, Class<T> serviceType) {
        System.out.println("Closing proxies for: " + baseAddress);
        T proxy = (T) clientCache.remove(new CacheKey(baseAddress, serviceType));
        if (proxy != null) {
            Client client = WebClient.client(proxy);
            client.close();
        }
    }

    private static class CacheKey {
        private String address;
        private Class<?> serviceType;
        public CacheKey(String address, Class<?> serviceType) {
            this.address = address;
            this.serviceType = serviceType;
        }
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result
                    + ((address == null) ? 0 : address.hashCode());
            result = prime * result
                    + ((serviceType == null) ? 0 : serviceType.hashCode());
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            CacheKey other = (CacheKey) obj;
            if (address == null) {
                if (other.address != null)
                    return false;
            } else if (!address.equals(other.address))
                return false;
            if (serviceType == null) {
                if (other.serviceType != null)
                    return false;
            } else if (!serviceType.equals(other.serviceType))
                return false;
            return true;
        };
    }

}
