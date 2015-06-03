/**
 *
 */
package org.github.etcd.rest;

public interface ResourceProxyFactory {
    <T> T createProxy(String baseAddress, Class<T> serviceType);

    <T> void closeProxy(String baseAddress, Class<T> serviceType);
}
