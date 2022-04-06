package online.yardstrong.netty.utils;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.security.*;

/**
 * (CN: common name; OU: organization unit; O: organization name; L: locality name; S: state name; C: country)
 * 1. 生成服务端私钥和证书仓库
 * keytool -genkey -alias ssl_server -validity 36500 -keyalg RSA -dname "CN=localhost" -keypass 123456 -storepass 123456 -keystore server_key.jks
 * 2. 导出服务端证书
 * keytool -export -alias ssl_server -keystore server_key.jks -storepass 123456 -file server.crt
 * 3. 生成客户端私钥和证书仓库
 * keytool -genkey -alias ssl_client -validity 36500 -keyalg RSA -dname "CN=localhost" -keypass 123456 -storepass 123456 -keystore client_key.jks
 * 4. 导出客户端证书
 * keytool -export -alias ssl_client -keystore client_key.jks -storepass 123456 -file client.crt
 * 5. 将服务端证书导入客户端证书仓库
 * keytool -import -trustcacerts -alias ssl_server -file server.crt -storepass 123456 -keystore client_key.jks
 * 6. 将客户端证书导入服务端证书仓库
 * keytool -import -trustcacerts -alias ssl_client -file client.crt -storepass 123456 -keystore server_key.jks
 * 7. 导出服务端公钥证书
 * keytool -import -alias ssl_server -keystore server_trust.jks -file server.crt -keypass 123456 -storepass 123456
 * 8. 导出客户端公钥证书
 * keytool -import -alias ssl_client -keystore client_trust.jks -file client.crt -keypass 123456 -storepass 123456
 */
public class SSLContextUtil {

    private static final InternalLogger LOG = InternalLoggerFactory.getInstance(SSLContextUtil.class);

    private static final String PROTOCOL = "SSLv3";

    /**
     * Init ssh context
     *
     * @param keystoreJKS   keystore
     * @param truststoreJKS truststore
     * @param keyPassword   key password
     * @param storePassword store password
     * @return ssl context
     */
    public static SSLContext initSSLContext(InputStream keystoreJKS, InputStream truststoreJKS,
                                            String keyPassword, String storePassword) {
        try {
            // keystore
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(keystoreJKS, keyPassword.toCharArray());
            KeyManagerFactory keyManagerfactory =
                    KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerfactory.init(keyStore, keyPassword.toCharArray());

            // truststore
            KeyStore truststore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(truststoreJKS, storePassword.toCharArray());
            TrustManagerFactory trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(truststore);

            // init ssl context
            SSLContext sslContext = SSLContext.getInstance(PROTOCOL);
            sslContext.init(keyManagerfactory.getKeyManagers(),
                    trustManagerFactory.getTrustManagers(), null);
            return sslContext;
        } catch (Exception e) {
            LOG.error("Fail to init ssl context", e);
            throw new RuntimeException(e);
        }
    }
}
