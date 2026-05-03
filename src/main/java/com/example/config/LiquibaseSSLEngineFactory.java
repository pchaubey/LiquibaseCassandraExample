package com.example.config;

import com.datastax.oss.driver.api.core.metadata.EndPoint;
import com.datastax.oss.driver.api.core.ssl.SslEngineFactory;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.net.URISyntaxException;
import java.security.KeyStore;

@Slf4j
public class LiquibaseSSLEngineFactory implements SslEngineFactory {

    private final SSLContext sslContext;

    public LiquibaseSSLEngineFactory() throws Exception {
        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        String trustStorePath = getAbsolutePath(System.getProperty("server.ssl.trust-store"));
        String trustStorePassword = System.getProperty("server.ssl.trust-store-password");
        try (FileInputStream fis = new FileInputStream(trustStorePath)) {
            trustStore.load(fis, trustStorePassword.toCharArray());
        }
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);
    }

    private String getAbsolutePath(String path) {
        String resourcePath = path.startsWith("classpath:") ? path.substring("classpath:".length()) : path;
        var resource = this.getClass().getClassLoader().getResource(resourcePath);
        try {
            return new File(resource.toURI()).getAbsolutePath();
        } catch (URISyntaxException e) {
            return path;
        }
    }

    @Override
    public SSLEngine newSslEngine(EndPoint remoteEndpoint) {
        var sslEngine = sslContext.createSSLEngine();
        sslEngine.setUseClientMode(true);
        return sslEngine;
    }

    @Override
    public void close() {
    }
}