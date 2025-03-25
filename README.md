# Tomcat SSL Configuration Guide

## Overview
This document explains how to configure SSL/TLS for the embedded Tomcat server in the iSLDevs application using Spring Boot 3.4.3.

## Prerequisites
- Java 17+
- Spring Boot 3.4.3
- Tomcat 10.1.x (embedded with Spring Boot 3.4.3)
- Valid SSL certificate (or self-signed for development)

## Certificate Generation
For development environments, generate a self-signed certificate:

```bash
keytool -genkeypair -alias yourserver -keyalg RSA -keysize 4096 \
  -validity 365 -storetype PKCS12 -keystore src/main/resources/yourserver.p12 \
  -storepass changeit -keypass changeit \
  -dname "CN=localhost" -ext "SAN=DNS:localhost,IP:127.0.0.1"
```
Check certificate details
```bash
keytool -list -v -keystore src/main/resources/yourserver.p12 -storepass changeit
```

Check supported protocols (should only show TLS 1.2/1.3)
```bash
nmap --script ssl-enum-ciphers -p 8443 localhost
```

## Java Class Configuration
Java configuration class:
- src/main/java/com/base/config/TomcatSSLConfig.java
