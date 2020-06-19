package com.github.tomakehurst.wiremock.http.ssl;

import com.github.tomakehurst.wiremock.testsupport.TestNotifier;
import org.junit.Test;

import javax.net.ssl.ExtendedSSLSession;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedKeyManager;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

import static com.github.tomakehurst.wiremock.testsupport.TestFiles.KEY_STORE_WITH_CA_PATH;
import static java.util.Collections.singletonList;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class CertificateGeneratingX509ExtendedKeyManagerChooseEngineServerAliasTest {

    @Test
    public void generatesAndReturnsNewAliasForWorkingPrivateKey() throws Exception {

        KeyStore keyStore = readKeyStore(KEY_STORE_WITH_CA_PATH, "password");
        String hostname = "example.com";

        // given
        CertificateGeneratingX509ExtendedKeyManager generatingKeyManager = keyManagerFor(keyStore, "password".toCharArray());

        // expect
        assertNull(generatingKeyManager.getCertificateChain(hostname));
        assertNull(generatingKeyManager.getPrivateKey(hostname));

        // when
        SSLEngine sslEngineMock = getSslEngineWithSessionFor(hostname);
        String keyAlias = generatingKeyManager.chooseEngineServerAlias("RSA", null, sslEngineMock);

        // then
        assertEquals(hostname, keyAlias);

        // and
        assertEquals(2, generatingKeyManager.getCertificateChain(keyAlias).length);
        RSAPrivateCrtKey privateKey = (RSAPrivateCrtKey) generatingKeyManager.getPrivateKey(keyAlias);

        PublicKey myPublicKey = getPublicKey(privateKey);
        assertEquals(myPublicKey, generatingKeyManager.getCertificateChain(keyAlias)[0].getPublicKey());
    }

    @Test
    public void returnsSameGeneratedPrivateKeyOnSubsequentCalls() throws Exception {

        KeyStore keyStore = readKeyStore(KEY_STORE_WITH_CA_PATH, "password");
        String hostname = "example.com";

        // given
        CertificateGeneratingX509ExtendedKeyManager generatingKeyManager = keyManagerFor(keyStore, "password".toCharArray());

        // when
        SSLEngine sslEngineMock = getSslEngineWithSessionFor(hostname);
        String keyAlias = generatingKeyManager.chooseEngineServerAlias("RSA", null, sslEngineMock);

        // and
        X509Certificate[] certificateChain = generatingKeyManager.getCertificateChain(keyAlias);
        PrivateKey privateKey = generatingKeyManager.getPrivateKey(keyAlias);

        // when
        String sameKeyAlias = generatingKeyManager.chooseEngineServerAlias("RSA", null, sslEngineMock);

        // then
        assertEquals(keyAlias, sameKeyAlias);

        // and same keys returned
        assertEquals(privateKey, generatingKeyManager.getPrivateKey(sameKeyAlias));
        assertArrayEquals(certificateChain, generatingKeyManager.getCertificateChain(sameKeyAlias));
    }

    private PublicKey getPublicKey(RSAPrivateCrtKey privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(privateKey.getModulus(), privateKey.getPublicExponent());

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(publicKeySpec);
    }

    private SSLEngine getSslEngineWithSessionFor(String hostname) {
        SSLEngine sslEngineMock = mock(SSLEngine.class);
        ExtendedSSLSession extendedSslSessionMock = mock(ExtendedSSLSession.class);
        given(sslEngineMock.getHandshakeSession()).willReturn(extendedSslSessionMock);
        SNIServerName hostName = new SNIHostName(hostname);
        given(extendedSslSessionMock.getRequestedServerNames()).willReturn(singletonList(hostName));
        return sslEngineMock;
    }

    private CertificateGeneratingX509ExtendedKeyManager keyManagerFor(KeyStore keyStore, char[] keyStorePassword) throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keyStorePassword);
        X509ExtendedKeyManager keyManager = findExtendedKeyManager(keyManagerFactory.getKeyManagers());
        X509KeyStore x509KeyStore = new X509KeyStore(keyStore, keyStorePassword);

        return new CertificateGeneratingX509ExtendedKeyManager(
                keyManager,
                new DynamicKeyStore(x509KeyStore),
                new SunHostNameMatcher(),
                new TestNotifier()
        );
    }

    private X509ExtendedKeyManager findExtendedKeyManager(KeyManager[] keyManagers) {
        for (KeyManager manager : keyManagers) {
            if (manager instanceof X509ExtendedKeyManager) {
                return (X509ExtendedKeyManager) manager;
            }
        }
        throw new AssertionError("Can't run this test of the SSL provider does not create X509ExtendedKeyManager instances");
    }

    private static KeyStore readKeyStore(String path, String password) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore trustStore  = KeyStore.getInstance(KeyStore.getDefaultType());
        FileInputStream instream = new FileInputStream(path);
        try {
            trustStore.load(instream, password.toCharArray());
        } finally {
            instream.close();
        }
        return trustStore;
    }
}
