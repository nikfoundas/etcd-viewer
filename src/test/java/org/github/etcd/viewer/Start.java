package org.github.etcd.viewer;

import org.apache.wicket.util.time.Duration;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;

public class Start {

    private static final String WICKET_CFG = "wicket.configuration"; // or "deployment"

    private enum CfgType {
        development, deployment
    }

    public static void main(String[] args) throws Exception {
        int timeout = (int) Duration.ONE_HOUR.getMilliseconds();

        System.setProperty(WICKET_CFG, CfgType.development.toString());

        Server server = new Server();

        ServerConnector connector = new ServerConnector(server);

        // Set some timeout options to make debugging easier.
        connector.setIdleTimeout(timeout);
        connector.setSoLingerTime(-1);
        connector.setPort(8080);
        server.addConnector(connector);

        Resource keystore = Resource.newClassPathResource("/keystore");
        if (keystore != null && keystore.exists()) {
            // if a keystore for a SSL certificate is available, start a SSL
            // connector on port 8443.
            // By default, the quickstart comes with a Apache Wicket Quickstart
            // Certificate that expires about half way september 2021. Do not
            // use this certificate anywhere important as the passwords are
            // available in the source.

            SslContextFactory sslContextFactory = new SslContextFactory();
            sslContextFactory.setKeyStoreResource(keystore);
            sslContextFactory.setKeyStorePassword("wicket");
            sslContextFactory.setTrustStoreResource(keystore);
            sslContextFactory.setKeyManagerPassword("wicket");

            // HTTP Configuration
            HttpConfiguration httpConfig = new HttpConfiguration();
            httpConfig.setSecureScheme("https");
            httpConfig.setSecurePort(8443);
            httpConfig.setOutputBufferSize(32768);
            httpConfig.setRequestHeaderSize(8192);
            httpConfig.setResponseHeaderSize(8192);
            httpConfig.setSendServerVersion(true);
            httpConfig.setSendDateHeader(false);

            // SSL HTTP Configuration
            HttpConfiguration httpsConfig = new HttpConfiguration(httpConfig);
            httpsConfig.addCustomizer(new SecureRequestCustomizer());

            // SSL Connector
            ServerConnector sslConnector = new ServerConnector(server,
                    new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
                    new HttpConnectionFactory(httpsConfig));
            sslConnector.setPort(8443);

            server.addConnector(sslConnector);

            System.out.println("SSL access to the quickstart has been enabled on port 8443");
            System.out.println("You can access the application using SSL on https://localhost:8443");
            System.out.println();
        }

        WebAppContext bb = new WebAppContext();
        bb.setServer(server);
        bb.setContextPath("/");
        bb.setWar("src/main/webapp");

        // START JMX SERVER
        // MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        // MBeanContainer mBeanContainer = new MBeanContainer(mBeanServer);
        // server.getContainer().addEventListener(mBeanContainer);
        // mBeanContainer.start();

        server.setHandler(bb);

        try {
            System.out.println(">>> STARTING EMBEDDED JETTY SERVER, PRESS ANY KEY TO STOP");
            server.start();
            System.in.read();
            System.out.println(">>> STOPPING EMBEDDED JETTY SERVER");
            server.stop();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
