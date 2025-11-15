/*
 * Copyright 2025 iSLDevs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.base.config.security;

import org.apache.catalina.connector.Connector;
import org.apache.catalina.session.StandardManager;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * Configuration class for customizing Tomcat with SSL and HTTP connectors.
 *
 * @author YISivlay
 */
@Configuration
public class TomcatSSLConfig {

    private static final String KEYSTORE_PATH = "server.p12";
    private static final String KEYSTORE_PASSWORD = "isldevs";
    private static final String KEY_ALIAS = "server";
    private static final String KEYSTORE_TYPE = "PKCS12";
    private static final String TRUSTSTORE_PATH = "truststore.jks";
    private static final String TRUSTSTORE_PASSWORD = "password";
    private static final String TRUSTSTORE_TYPE = "JKS";
    private static final int HTTPS_PORT = 8443;
    private static final int HTTP_PORT = 8080;
    private static final String CONTEXT_PATH = "/api/v1";

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> webServerFactoryCustomizer() {
        return factory -> {
            factory.addConnectorCustomizers(connector -> {
                connector.setScheme("https");
                connector.setSecure(true);
                connector.setPort(HTTPS_PORT);

                var protocol = (Http11NioProtocol) connector.getProtocolHandler();

                var sslHostConfig = new SSLHostConfig();
                sslHostConfig.setHostName("_default_");
                sslHostConfig.setProtocols("TLSv1.2+TLSv1.3");
                sslHostConfig.setCertificateVerification("none");
                sslHostConfig.setCiphers("TLS_AES_256_GCM_SHA384:TLS_AES_128_GCM_SHA256");
                sslHostConfig.setHonorCipherOrder(true);

                var truststoreUrl = getClass().getClassLoader()
                        .getResource(TRUSTSTORE_PATH);
                if (truststoreUrl == null) {
                    throw new RuntimeException("Truststore not found at classpath path: " + TRUSTSTORE_PATH);
                }
                sslHostConfig.setTruststoreFile(truststoreUrl.getPath());
                sslHostConfig.setTruststorePassword(TRUSTSTORE_PASSWORD);
                sslHostConfig.setTruststoreType(TRUSTSTORE_TYPE);

                var certificate = new SSLHostConfigCertificate(sslHostConfig, SSLHostConfigCertificate.Type.RSA);
                try (var ksis = getClass().getClassLoader()
                        .getResourceAsStream(KEYSTORE_PATH); var tsIs = getClass().getClassLoader()
                                .getResourceAsStream(TRUSTSTORE_PATH)) {
                    if (ksis == null) {
                        throw new RuntimeException("Keystore not found at classpath path: " + KEYSTORE_PATH);
                    }
                    if (tsIs == null) {
                        throw new RuntimeException("Truststore not found at classpath path: " + TRUSTSTORE_PATH);
                    }
                    var ks = KeyStore.getInstance(KEYSTORE_TYPE);
                    ks.load(ksis, KEYSTORE_PASSWORD.toCharArray());

                    certificate.setCertificateKeystore(ks);
                    certificate.setCertificateKeyAlias(KEY_ALIAS);
                    certificate.setCertificateKeystorePassword(KEYSTORE_PASSWORD);

                } catch (IOException | CertificateException | KeyStoreException | NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }

                sslHostConfig.addCertificate(certificate);
                protocol.setSSLEnabled(true);
                protocol.addSslHostConfig(sslHostConfig);

            });

            factory.setContextPath(CONTEXT_PATH);

            var httpConnector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
            httpConnector.setScheme("http");
            httpConnector.setPort(HTTP_PORT);
            httpConnector.setSecure(false);
            httpConnector.setRedirectPort(HTTPS_PORT);
            factory.addAdditionalTomcatConnectors(httpConnector);
        };
    }

    @Bean
    public TomcatContextCustomizer tomcatContextCustomizer() {
        return context -> {
            StandardManager manager = new StandardManager();
            manager.setPathname(null);
            context.setManager(manager);
        };
    }
}
