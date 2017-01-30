package com.distelli.europa.guice;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import javax.inject.Provider;

import com.distelli.europa.models.SslSettings;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.util.Base64;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.TrustManagerFactorySpi;
import javax.net.ssl.X509TrustManager;
import javax.xml.bind.DatatypeConverter;
import sun.security.util.DerInputStream;
import sun.security.util.DerValue;
import javax.inject.Inject;

public class SslContextFactoryProvider implements Provider<SslContextFactory> {
    @Inject
    private Provider<SslSettings> _sslSettings;

    @Override
    public SslContextFactory get() {
        try {
            return getThrows();
        } catch ( RuntimeException ex ) {
            throw ex;
        } catch ( Exception ex ) {
            throw new RuntimeException(ex);
        }
    }

    public SslContextFactory getThrows() throws Exception {
        SslContextFactory ctxFactory = new SslContextFactory();
        ctxFactory.setValidatePeerCerts(false);

        SslSettings settings = _sslSettings.get();
        if ( null == settings ) return ctxFactory;

        char[] passwd = generatePassword();
        ctxFactory.setTrustStore(getTrustStore(settings.getAuthorityCertificate()));
        ctxFactory.setKeyStore(getKeyStore(passwd, settings.getServerPrivateKey(), settings.getServerCertificate()));
        ctxFactory.setKeyStorePassword(new String(passwd));

        return ctxFactory;
    }

    private static KeyStore getTrustStore(String authorityCertificate) throws Exception {
        X509Certificate caCert = toX509Certificate(authorityCertificate);
        if ( null == caCert ) return null;
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(null);
        trustStore.setCertificateEntry("ca-cert", caCert);
        return trustStore;
    }

    private static char[] generatePassword() {
        // Use random password for KeyStore:
        SecureRandom rand = new SecureRandom();
        char[] passwd = new char[8];
        for ( int i=0; i < passwd.length; i++ ) {
            passwd[i] = (char)rand.nextInt(Character.MAX_VALUE);
        }
        return passwd;
    }

    private static KeyStore getKeyStore(char[] passwd, String serverPrivateKey, String serverCertificate) throws Exception {
        PrivateKey key = pemToPrivateKey(serverPrivateKey);
        X509Certificate cert = toX509Certificate(serverCertificate);
        if ( null == key || null == cert ) return null;
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null);
        keyStore.setCertificateEntry("cert-alias", cert);
        keyStore.setKeyEntry("key-alias", key, passwd, new Certificate[] {cert});
        return keyStore;
    }

    private static PrivateKey pemToPrivateKey(String keyPem) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException  {
        if ( null == keyPem ) return null;
        // PKCS#8 format
        final String PEM_PRIVATE_START = "-----BEGIN PRIVATE KEY-----";
        final String PEM_PRIVATE_END = "-----END PRIVATE KEY-----";

        // PKCS#1 format
        final String PEM_RSA_PRIVATE_START = "-----BEGIN RSA PRIVATE KEY-----";
        final String PEM_RSA_PRIVATE_END = "-----END RSA PRIVATE KEY-----";

        if (keyPem.indexOf(PEM_PRIVATE_START) != -1) { // PKCS#8 format
            keyPem = keyPem.replace(PEM_PRIVATE_START, "").replace(PEM_PRIVATE_END, "");
            keyPem = keyPem.replaceAll("\\s", "");

            byte[] pkcs8EncodedKey = Base64.getDecoder().decode(keyPem);

            KeyFactory factory = KeyFactory.getInstance("RSA");
            return factory.generatePrivate(new PKCS8EncodedKeySpec(pkcs8EncodedKey));

        } else if (keyPem.indexOf(PEM_RSA_PRIVATE_START) != -1) {  // PKCS#1 format

            keyPem = keyPem.replace(PEM_RSA_PRIVATE_START, "").replace(PEM_RSA_PRIVATE_END, "");
            keyPem = keyPem.replaceAll("\\s", "");

            DerInputStream derReader = new DerInputStream(Base64.getDecoder().decode(keyPem));

            DerValue[] seq = derReader.getSequence(0);

            if (seq.length < 9) {
                throw new InvalidKeySpecException("Could not parse a PKCS1 private key.");
            }

            // skip version seq[0];
            BigInteger modulus = seq[1].getBigInteger();
            BigInteger publicExp = seq[2].getBigInteger();
            BigInteger privateExp = seq[3].getBigInteger();
            BigInteger prime1 = seq[4].getBigInteger();
            BigInteger prime2 = seq[5].getBigInteger();
            BigInteger exp1 = seq[6].getBigInteger();
            BigInteger exp2 = seq[7].getBigInteger();
            BigInteger crtCoef = seq[8].getBigInteger();

            RSAPrivateCrtKeySpec keySpec = new RSAPrivateCrtKeySpec(modulus, publicExp, privateExp, prime1, prime2, exp1, exp2, crtCoef);

            KeyFactory factory = KeyFactory.getInstance("RSA");

            return factory.generatePrivate(keySpec);
        }

        throw new InvalidKeySpecException("Unsupported PEM format. Expected " + PEM_PRIVATE_START + " or " + PEM_PRIVATE_END);
    }

    private static byte[] parseDERFromPEM(String data, String beginDelimiter, String endDelimiter) throws InvalidKeySpecException {
        String[] tokens = data.split(beginDelimiter);
        if ( tokens.length < 2 ) {
            throw new InvalidKeySpecException("Unsupported PEM format. Expected " + beginDelimiter);
        }
        tokens = tokens[1].split(endDelimiter);
        return DatatypeConverter.parseBase64Binary(tokens[0]);
    }

    private static X509Certificate toX509Certificate(String certPem) throws CertificateException, InvalidKeySpecException {
        if ( null == certPem ) return null;
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        return (X509Certificate)certFactory.generateCertificate(
            new ByteArrayInputStream(
                parseDERFromPEM(certPem, "-----BEGIN CERTIFICATE-----", "-----END CERTIFICATE-----")));
    }
}
