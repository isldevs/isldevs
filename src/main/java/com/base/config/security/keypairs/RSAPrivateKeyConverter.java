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
package com.base.config.security.keypairs;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import org.springframework.core.serializer.Deserializer;
import org.springframework.core.serializer.Serializer;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.util.FileCopyUtils;

/**
 * @author YISivlay
 */
public class RSAPrivateKeyConverter implements Serializer<RSAPrivateKey>, Deserializer<RSAPrivateKey> {

    private final TextEncryptor textEncryptor;

    public RSAPrivateKeyConverter(TextEncryptor textEncryptor) {
        this.textEncryptor = textEncryptor;
    }

    @Override
    public RSAPrivateKey deserialize(InputStream inputStream) {
        try (var reader = new BufferedReader(new InputStreamReader(inputStream,
                                                                   StandardCharsets.UTF_8))) {
            var pem = this.textEncryptor.decrypt(FileCopyUtils.copyToString(reader));
            var privateKeyPEM = pem.replace("-----BEGIN PRIVATE KEY-----",
                                            "")
                                   .replace("-----END PRIVATE KEY-----",
                                            "");
            var encoded = Base64.getMimeDecoder()
                                .decode(privateKeyPEM);
            var keyFactory = KeyFactory.getInstance("RSA");
            var keySpec = new PKCS8EncodedKeySpec(encoded);
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (Throwable throwable) {
            throw new IllegalArgumentException("there's been an exception",
                                               throwable);
        }
    }

    @Override
    public void serialize(RSAPrivateKey object,
                          OutputStream outputStream) throws IOException {
        var pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(object.getEncoded());
        var string = "-----BEGIN PRIVATE KEY-----\n" + Base64.getMimeEncoder()
                                                             .encodeToString(pkcs8EncodedKeySpec.getEncoded()) + "\n-----END PRIVATE KEY-----";
        outputStream.write(this.textEncryptor.encrypt(string)
                                             .getBytes(StandardCharsets.UTF_8));
    }

    public Key convertFromString(String keyStr) throws IOException {
        var byteArrayInputStream = new ByteArrayInputStream(keyStr.getBytes(StandardCharsets.UTF_8));
        return deserialize(byteArrayInputStream);
    }

}
