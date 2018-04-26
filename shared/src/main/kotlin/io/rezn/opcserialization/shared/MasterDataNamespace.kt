package io.rezn.opcserialization.shared

import org.eclipse.milo.opcua.stack.core.types.OpcUaBinaryDataTypeDictionary
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId


object MasterDataNamespace {

    const val URI = "http://opcfoundation.org/UA/OpenSCS/md"

    const val NAMESPACE_INDEX = 2

    val VocabularyElement_Encoding_DefaultBinary = node( 37)

    val VocabularyElementAttribute_Encoding_DefaultBinary = node(38)
    val VocabularyElementList_Encoding_DefaultBinary = node( 39)
    val VocabularyDataType_Encoding_DefaultBinary = node( 40)
    val VocabularyListDataType_Encoding_DefaultBinary = node( 41)


    val VocabularyElement_NodeId = node(1)
    val VocabularyElementAttribute_NodeId = node(2)
    val VocabularyElementList_NodeId = node(3)
    val VocabularyDataType_NodeId = node(4)
    val VocabularyListDataType_NodeId = node( 5)

    val MasterDataManagerType = node(6)
    val MasterDataManager = node( 10)
    val MasterDataManagerType_GetMasterData = node( 7)

    private fun node(id: Int) = NodeId(NAMESPACE_INDEX, id)

    fun dataTypeDictionary() = OpcUaBinaryDataTypeDictionary(MasterDataNamespace.URI).apply {

        registerStructCodec(
                VocabularyListDataType.Codec.asBinaryCodec(),
                VocabularyListDataType.Codec.name,
                MasterDataNamespace.VocabularyListDataType_Encoding_DefaultBinary)

        registerStructCodec(
                VocabularyDataType.Codec.asBinaryCodec(),
                VocabularyDataType.Codec.name,
                MasterDataNamespace.VocabularyDataType_Encoding_DefaultBinary)

        registerStructCodec(
                VocabularyElementList.Codec.asBinaryCodec(),
                VocabularyElementList.Codec.name,
                MasterDataNamespace.VocabularyElementList_Encoding_DefaultBinary)

        registerStructCodec(
                VocabularyElement.Codec.asBinaryCodec(),
                VocabularyElement.Codec.name,
                MasterDataNamespace.VocabularyElement_Encoding_DefaultBinary)

        registerStructCodec(
                VocabularyAttribute.Codec.asBinaryCodec(),
                VocabularyAttribute.Codec.name,
                MasterDataNamespace.VocabularyElementAttribute_Encoding_DefaultBinary)
    }
}