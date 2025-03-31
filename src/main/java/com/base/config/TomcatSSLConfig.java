package com.base.config;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * @author YISivlay
 */
@Configuration
public class TomcatSSLConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> webServerFactoryCustomizer() {
        return factory -> {
            factory.addConnectorCustomizers(connector -> {
                connector.setScheme("https");
                connector.setSecure(true);
                connector.setPort(8443);

                Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();

                SSLHostConfig sslHostConfig = new SSLHostConfig();
                sslHostConfig.setHostName("localhost");
                sslHostConfig.setProtocols("TLSv1.2+TLSv1.3");
                sslHostConfig.setCertificateVerification("false");
                sslHostConfig.setCiphers("TLS_AES_256_GCM_SHA384:TLS_AES_128_GCM_SHA256");
                sslHostConfig.setHonorCipherOrder(true);

                SSLHostConfigCertificate certificate = new SSLHostConfigCertificate(sslHostConfig, SSLHostConfigCertificate.Type.RSA);
                try (InputStream is = getClass().getClassLoader().getResourceAsStream("server.p12")) {
                    KeyStore ks = KeyStore.getInstance("PKCS12");
                    ks.load(is, "isldevs".toCharArray());

                    certificate.setCertificateKeystore(ks);
                    certificate.setCertificateKeyAlias("server");
                    certificate.setCertificateKeystorePassword("isldevs");

                } catch (IOException | CertificateException | KeyStoreException | NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }

                sslHostConfig.addCertificate(certificate);
                protocol.addSslHostConfig(sslHostConfig);
            });
            Connector httpConnector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
            httpConnector.setScheme("http");
            httpConnector.setPort(8080);
            httpConnector.setSecure(false);
            httpConnector.setRedirectPort(8443);
            factory.setContextPath("/api/v1");
            factory.addAdditionalTomcatConnectors(httpConnector);
        };
    }

}