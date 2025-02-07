package org.springframework.samples.homepix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import java.io.File;

@Component
public class SslConfig implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(SslConfig.class);
    private final Environment env;

    public SslConfig(Environment env) {
        this.env = env;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            String keystorePath = env.getProperty("server.ssl.key-store");
            String keystorePassword = env.getProperty("server.ssl.key-store-password");

            if (keystorePath == null || keystorePassword == null) {
                logger.error("❌ SSL keystore configuration is missing!");
                return;
            }

            File keystoreFile = new File(keystorePath.replace("file:", ""));
            if (!keystoreFile.exists()) {
                logger.error("❌ Keystore file NOT FOUND at: {}", keystoreFile.getAbsolutePath());
                return;
            }

            logger.info("🔍 SSL Keystore Path: {}", keystoreFile.getAbsolutePath());
            logger.info("📏 Keystore File Size: {} bytes", keystoreFile.length());

            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(new FileInputStream(keystoreFile), keystorePassword.toCharArray());

            logger.info("🔑 Loaded {} entries from keystore.", keyStore.size());

            Enumeration<String> aliasesEnum = keyStore.aliases();
            while (aliasesEnum.hasMoreElements()) {
                String alias = aliasesEnum.nextElement();
                X509Certificate x509 = (X509Certificate) keyStore.getCertificate(alias);
                logger.info("🔒 Certificate for alias '{}':", alias);
                logger.info("   ➤ Subject: {}", x509.getSubjectDN());
                logger.info("   ➤ Issuer: {}", x509.getIssuerDN());
                logger.info("   ➤ Expiry Date: {}", x509.getNotAfter());
            }

        } catch (Exception e) {
            logger.error("❌ Error loading SSL configuration!", e);
        }
    }
}
