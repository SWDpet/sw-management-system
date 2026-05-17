package com.swmanager.system.config;

import java.io.File;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;
import org.apache.tomcat.util.net.SSLHostConfigCertificate.Type;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * local 프로파일 전용 HTTPS 추가 커넥터.
 *
 * 동작:
 *   - 기존 HTTP 포트 (server.port, 기본 8080) 유지
 *   - dev.https.port (기본 8443) 에 self-signed 인증서로 HTTPS 커넥터 추가
 *
 * 필요 이유: 갤럭시탭/모바일에서 getUserMedia (QR 인라인 카메라) 사용 시
 *   브라우저가 secure context 만 허용 → 사설망 IP 는 반드시 HTTPS.
 *
 * 활성 조건: spring.profiles.active=local AND dev.https.enabled=true (기본 true)
 */
@Configuration
@Profile("local")
public class TomcatHttpsConfig {

    @Value("${dev.https.enabled:true}")
    private boolean enabled;

    @Value("${dev.https.port:8443}")
    private int httpsPort;

    @Value("${dev.https.keystore:keystore-dev.p12}")
    private String keystorePath;

    @Value("${dev.https.keystore-password:changeit}")
    private String keystorePassword;

    @Value("${dev.https.key-alias:swmanager}")
    private String keyAlias;

    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        if (enabled) {
            Connector https = buildHttpsConnector();
            if (https != null) tomcat.addAdditionalTomcatConnectors(https);
        }
        return tomcat;
    }

    private Connector buildHttpsConnector() {
        File ks = new File(keystorePath);
        if (!ks.isAbsolute()) ks = ks.getAbsoluteFile();
        if (!ks.exists()) {
            System.err.println("[TomcatHttpsConfig] keystore not found: " + ks.getAbsolutePath()
                    + " — HTTPS connector disabled. (Run keytool to generate keystore-dev.p12)");
            return null;
        }
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setScheme("https");
        connector.setSecure(true);
        connector.setPort(httpsPort);
        Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
        protocol.setSSLEnabled(true);

        SSLHostConfig sslHost = new SSLHostConfig();
        SSLHostConfigCertificate cert = new SSLHostConfigCertificate(sslHost, Type.UNDEFINED);
        cert.setCertificateKeystoreFile(ks.getAbsolutePath());
        cert.setCertificateKeystorePassword(keystorePassword);
        cert.setCertificateKeystoreType("PKCS12");
        cert.setCertificateKeyAlias(keyAlias);
        sslHost.addCertificate(cert);
        protocol.addSslHostConfig(sslHost);
        return connector;
    }
}
