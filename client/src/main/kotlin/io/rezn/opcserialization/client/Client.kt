package io.rezn.opcserialization.client

import io.rezn.opcserialization.shared.KeyStoreLoader
import io.rezn.opcserialization.shared.MasterDataNamespace
import io.rezn.opcserialization.shared.VocabularyListDataType
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.eclipse.milo.opcua.sdk.client.OpcUaClient
import org.eclipse.milo.opcua.sdk.client.api.UaClient
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider
import org.eclipse.milo.opcua.stack.client.UaTcpStackClient
import org.eclipse.milo.opcua.stack.core.UaException
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy
import org.eclipse.milo.opcua.stack.core.types.OpcUaDataTypeManager
import org.eclipse.milo.opcua.stack.core.types.builtin.ExtensionObject
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned
import org.eclipse.milo.opcua.stack.core.types.structured.CallMethodRequest
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription
import org.eclipse.milo.opcua.stack.core.util.CryptoRestrictions
import java.io.File
import java.security.KeyPair
import java.security.Security
import java.util.concurrent.CompletableFuture

const val applicationUri = "urn:rezn:md:client"
const val applicationName = "MD client application"

val securityPolicy = SecurityPolicy.None
val identityProvider = AnonymousProvider()

fun main(args: Array<String>) {

    val endpoint = args.getOrElse(0,  { i ->  "opc.tcp://localhost:51510/rezn-opc" })

    print("Connecting to server $endpoint")

    CryptoRestrictions.remove()
    Security.addProvider(BouncyCastleProvider())

    OpcUaDataTypeManager.getInstance().registerTypeDictionary(MasterDataNamespace.dataTypeDictionary())

    val keyStoreLoader = KeyStoreLoader().load(securityTempDir())

    val result = createClient(endpoint, keyStoreLoader.clientKeyPair!!).connect().thenCompose(::query).get()

    print("Got $result")

}

@Throws(Exception::class)
private fun createClient(endpoint: String, clientKeyPair: KeyPair): UaClient = config(endpoint, clientKeyPair).run(::OpcUaClient)

fun query(client: UaClient): CompletableFuture<VocabularyListDataType> {

    val request = CallMethodRequest(MasterDataNamespace.MasterDataManager,
            MasterDataNamespace.MasterDataManagerType_GetMasterData,
            arrayOf(Variant(emptyArray<String>())))

    return client.call(request).thenApply { result ->

        if (result.statusCode.isBad)
            throw UaException(result.statusCode)

        val output = result.outputArguments?.firstOrNull()?.value as? ExtensionObject

        output?.run { decode<VocabularyListDataType>() }
    }
}

private fun config(endpoint: String, clientKeyPair: KeyPair) = OpcUaClientConfig.builder()
        .setApplicationName(LocalizedText.english(applicationName))
        .setApplicationUri(applicationUri)
        //.setCertificate(loader.clientCertificate)
        .setKeyPair(clientKeyPair)
        .setEndpoint(getEndpoint(endpoint))
        .setIdentityProvider(identityProvider)
        .setRequestTimeout(Unsigned.uint(500_000))
        .build()

private fun securityTempDir(): File = File(System.getProperty("java.io.tmpdir"), "security").apply { mkdirs() }

private fun getEndpoint(target: String) : EndpointDescription {

    val endpoints = try {
        UaTcpStackClient.getEndpoints(target).get()
    } catch (e: Exception) {
        UaTcpStackClient.getEndpoints( "$target/discovery").get()
    }

    return endpoints.find { it.securityPolicyUri == securityPolicy.securityPolicyUri }
            ?: throw Exception("no desired endpoints returned")
}


