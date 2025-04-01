/**
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

                var protocol = (Http11NioProtocol) connector.getProtocolHandler();

                SSLHostConfig sslHostConfig = new SSLHostConfig();
                sslHostConfig.setHostName("localhost");
                sslHostConfig.setProtocols("TLSv1.2+TLSv1.3");
                sslHostConfig.setCertificateVerification("false");
                sslHostConfig.setCiphers("TLS_AES_256_GCM_SHA384:TLS_AES_128_GCM_SHA256");
                sslHostConfig.setHonorCipherOrder(true);

                var certificate = new SSLHostConfigCertificate(sslHostConfig, SSLHostConfigCertificate.Type.RSA);
                try (var is = getClass().getClassLoader().getResourceAsStream("server.p12")) {
                    var ks = KeyStore.getInstance("PKCS12");
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
            var httpConnector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
            httpConnector.setScheme("http");
            httpConnector.setPort(8080);
            httpConnector.setSecure(false);
            httpConnector.setRedirectPort(8443);
            factory.setContextPath("/api/v1");
            factory.addAdditionalTomcatConnectors(httpConnector);
        };
    }

}