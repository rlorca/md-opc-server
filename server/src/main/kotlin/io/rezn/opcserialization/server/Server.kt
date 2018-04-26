package io.rezn.opcserialization.server

import com.google.common.collect.ImmutableList
import io.rezn.opcserialization.shared.KeyStoreLoader
import io.rezn.opcserialization.shared.MasterDataNamespace
import org.eclipse.milo.opcua.sdk.server.OpcUaServer
import org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig
import org.eclipse.milo.opcua.sdk.server.identity.UsernameIdentityValidator
import org.eclipse.milo.opcua.sdk.server.util.HostnameUtil
import org.eclipse.milo.opcua.stack.core.application.DefaultCertificateManager
import org.eclipse.milo.opcua.stack.core.application.DefaultCertificateValidator
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText
import org.eclipse.milo.opcua.stack.core.types.structured.BuildInfo
import org.eclipse.milo.opcua.stack.core.util.CryptoRestrictions
import java.io.File
import java.util.*
import java.util.concurrent.CompletableFuture

const val serverUri = "http://rezn.io/opc-ua"
const val serverPort = 51510
const val productName = "Rezn Master Data server"
const val manufacturerName = "Rezn.io"
const val serverName = "rezn-opc"
const val serverAppName = "Rezn Master Data Example Server"


fun main(args: Array<String>) {

    // get endpoint from environment, otherwise use the default values
    val endpoindAddress = System.getenv("ENDPOINT")?.let { listOf(it) } ?: defaultEndpoints()

    val server = Server(serverUri, endpoindAddress, serverPort)
    server.start()
}

fun defaultEndpoints(): List<String> = mutableListOf(HostnameUtil.getHostname())
        .apply { addAll(HostnameUtil.getHostnames("0.0.0.0")) }

class Server(val uri: String, val endpoints: List<String>, val port: Int) {

    fun start() {
        CryptoRestrictions.remove()

        val loader = KeyStoreLoader().load()

        val certificateManager = DefaultCertificateManager(loader.serverKeyPair, loader.serverCertificate)

        val securityTempDir = File(System.getProperty("java.io.tmpdir"), "security")

        val certificateValidator = DefaultCertificateValidator(securityTempDir)

        val serverConfig = buildConfig(certificateManager, certificateValidator, identityValidator())

        val server = OpcUaServer(serverConfig)

        initializeNamespace(server)

        server.startup().get()

        val future = CompletableFuture<Void>()

        Runtime.getRuntime().addShutdownHook(Thread { future.complete(null) })

        future.get()
    }

    private fun identityValidator(): UsernameIdentityValidator {
        return UsernameIdentityValidator(true) { authChallenge ->
            val username = authChallenge.username
            val password = authChallenge.password

            val userOk = "user" == username && "password1" == password
            val adminOk = "admin" == username && "password2" == password

            userOk || adminOk
        }
    }

    private fun buildConfig(certificateManager: DefaultCertificateManager,
                            certificateValidator: DefaultCertificateValidator,
                            identityValidator: UsernameIdentityValidator) =

         OpcUaServerConfig.builder()
                .setApplicationUri(uri)
                .setApplicationName(LocalizedText.english(serverAppName))
                .setBindAddresses(listOf("0.0.0.0"))
                .setBindPort(port)
                .setEndpointAddresses(endpoints)
                .setBuildInfo(
                        BuildInfo(
                                uri,
                                manufacturerName,
                                productName,
                                OpcUaServer.SDK_VERSION,
                                "",
                                DateTime.now()))
                .setCertificateManager(certificateManager)
                .setCertificateValidator(certificateValidator)
                .setIdentityValidator(identityValidator)
                .setProductUri(uri)
                .setServerName(serverName)
                .setSecurityPolicies(
                        EnumSet.of(
                                SecurityPolicy.None))
                .setUserTokenPolicies(
                        ImmutableList.of(
                                OpcUaServerConfig.USER_TOKEN_POLICY_ANONYMOUS,
                                OpcUaServerConfig.USER_TOKEN_POLICY_USERNAME))
                .build()


    private fun initializeNamespace(server: OpcUaServer) {

        server.namespaceManager.registerAndAdd(MasterDataNamespace.URI) { idx ->

            // populate the namespace
            Loader.load(server, idx)

            // serve
            NamespaceHandler(server, idx)
        }
    }
}