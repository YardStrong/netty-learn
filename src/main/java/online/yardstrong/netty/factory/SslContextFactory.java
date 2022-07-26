package online.yardstrong.netty.factory;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

public class SslContextFactory {

    private static final InternalLogger log = InternalLoggerFactory.getInstance(SslContextFactory.class);
    private static final String PROTOCOL = "TLS";

    public static SSLContext getClientContext(InputStream keyJKS, InputStream trustJKS,
                                              String keyPassword, String storePassword, boolean mutualAuth) {
        try {
            if (mutualAuth && keyJKS == null) {
                throw new RuntimeException("Null key store for client key manager factory");
            }
            if (trustJKS == null) {
                throw new RuntimeException("Null trust store for client key manager factory");
            }

            // 双向认证时需要加载自己的证书，单向认证时不开启
            KeyManagerFactory keyManagerFactory = null;
            if (mutualAuth) {
                KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
                ks.load(keyJKS, storePassword.toCharArray());
                keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                keyManagerFactory.init(ks, keyPassword.toCharArray());
            }

            //信任库
            TrustManagerFactory trustManagerFactory = null;
            KeyStore tks = KeyStore.getInstance(KeyStore.getDefaultType());
            tks.load(trustJKS, storePassword.toCharArray());
            trustManagerFactory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(tks);

            SSLContext sslContext = SSLContext.getInstance(PROTOCOL);

            if (mutualAuth) {
                // 双向认证
                sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
            } else {
                // 单向认证
                sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            }
            return sslContext;
        } catch (Exception e) {
            log.error("Fail to init ssl context", e);
            throw new RuntimeException(e);
        }
    }


    public static SSLContext getServerContext(InputStream keyJKS, InputStream trustJKS,
                                              String keyPassword, String storePassword, boolean mutualAuth) {
        try {
            if (keyJKS == null) {
                throw new RuntimeException("Null key store for server key manager factory");
            }

            if (mutualAuth && trustJKS == null) {
                throw new RuntimeException("Null trust store for server key manager factory");
            }


            //密钥管理器
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(keyJKS, storePassword.toCharArray());
            KeyManagerFactory keyManagerFactory =
                    KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(ks, keyPassword.toCharArray());

            //信任库 caPath is String，双向认证再开启这一段，单向认证不加载
            TrustManagerFactory trustManagerFactory = null;
            if (mutualAuth) {
                KeyStore tks = KeyStore.getInstance(KeyStore.getDefaultType());
                tks.load(trustJKS, storePassword.toCharArray());
                trustManagerFactory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(tks);
            }

            // 获取安全套接字协议（TLS协议）的对象
            SSLContext sslContext = SSLContext.getInstance(PROTOCOL);
            if (mutualAuth) {
                // 双向认证
                sslContext.init(keyManagerFactory.getKeyManagers(),
                        trustManagerFactory.getTrustManagers(), null);
            } else {
                // 单向认证
                sslContext.init(keyManagerFactory.getKeyManagers(),
                        null, null);
            }
            return sslContext;
        } catch (Exception e) {
            log.error("Fail to init ssl context", e);
            throw new RuntimeException(e);
        }
    }
}
