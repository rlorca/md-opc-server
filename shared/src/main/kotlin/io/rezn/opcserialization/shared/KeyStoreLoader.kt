package io.rezn.opcserialization.shared

import java.security.KeyPair
import java.security.PrivateKey
import com.sun.org.apache.xml.internal.security.keys.keyresolver.KeyResolver.getPublicKey
import java.security.PublicKey
import java.security.KeyStore
import java.security.cert.X509Certificate
import com.sun.org.apache.xml.internal.security.keys.keyresolver.KeyResolver.getPublicKey
import java.io.FileInputStream
import java.io.FileOutputStream
import org.eclipse.milo.opcua.sdk.server.util.HostnameUtil
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateBuilder
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateGenerator
import java.io.File
import java.util.regex.Pattern


class KeyStoreLoader {

    companion object {
        private val CLIENT_ALIAS = "client-ai"
        private val SERVER_ALIAS = "1"
        private val PASSWORD = "cidinha".toCharArray()
    }

    private val IP_ADDR_PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$")


    var clientCertificate: X509Certificate? = null
        private set
    var clientKeyPair: KeyPair? = null
        private set
    var serverCertificate: X509Certificate? = null
        private set
    var serverKeyPair: KeyPair? = null
        private set

    @Throws(Exception::class)
    fun load(): KeyStoreLoader {
        val keyStore = KeyStore.getInstance("PKCS12")
        keyStore.load(javaClass.classLoader.getResourceAsStream("acruas.p12"), PASSWORD)

        val clientPrivateKey = keyStore.getKey(CLIENT_ALIAS, PASSWORD)
        if (clientPrivateKey is PrivateKey) {
            clientCertificate = keyStore.getCertificate(CLIENT_ALIAS) as X509Certificate
            val clientPublicKey = clientCertificate!!.getPublicKey()
            clientKeyPair = KeyPair(clientPublicKey, clientPrivateKey)
        }

        val serverPrivateKey = keyStore.getKey(SERVER_ALIAS, PASSWORD)
        if (serverPrivateKey is PrivateKey) {
            serverCertificate = keyStore.getCertificate(SERVER_ALIAS) as X509Certificate
            val serverPublicKey = serverCertificate!!.getPublicKey()
            serverKeyPair = KeyPair(serverPublicKey, serverPrivateKey)
        }

        return this
    }


    @Throws(Exception::class)
    fun load(baseDir: File): KeyStoreLoader {
        val keyStore = KeyStore.getInstance("PKCS12")

        val serverKeyStore = baseDir.toPath().resolve("example-client.pfx").toFile()


        if (!serverKeyStore.exists()) {
            keyStore.load(null, PASSWORD)

            val keyPair = SelfSignedCertificateGenerator.generateRsaKeyPair(2048)

            val builder = SelfSignedCertificateBuilder(keyPair)
                    .setCommonName("Eclipse Milo Example Client")
                    .setOrganization("digitalpetri")
                    .setOrganizationalUnit("dev")
                    .setLocalityName("Folsom")
                    .setStateName("CA")
                    .setCountryCode("US")
                    .setApplicationUri("urn:eclipse:milo:examples:client")
                    .addDnsName("localhost")
                    .addIpAddress("127.0.0.1")

            // Get as many hostnames and IP addresses as we can listed in the certificate.
            for (hostname in HostnameUtil.getHostnames("0.0.0.0")) {
                if (IP_ADDR_PATTERN.matcher(hostname).matches()) {
                    builder.addIpAddress(hostname)
                } else {
                    builder.addDnsName(hostname)
                }
            }

            val certificate = builder.build()

            keyStore.setKeyEntry(CLIENT_ALIAS, keyPair.private, PASSWORD, arrayOf(certificate))
            keyStore.store(FileOutputStream(serverKeyStore), PASSWORD)
        } else {
            keyStore.load(FileInputStream(serverKeyStore), PASSWORD)
        }

        val serverPrivateKey = keyStore.getKey(CLIENT_ALIAS, PASSWORD)
        if (serverPrivateKey is PrivateKey) {
            val clientCertificate = keyStore.getCertificate(CLIENT_ALIAS) as X509Certificate
            val serverPublicKey = clientCertificate.getPublicKey()
            clientKeyPair = KeyPair(serverPublicKey, serverPrivateKey)
        }

        return this
    }
}

