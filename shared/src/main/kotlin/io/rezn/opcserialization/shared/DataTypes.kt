package io.rezn.opcserialization.shared

import org.eclipse.milo.opcua.stack.core.serialization.UaDecoder
import org.eclipse.milo.opcua.stack.core.serialization.UaEncoder
import org.eclipse.milo.opcua.stack.core.serialization.UaStructure
import org.eclipse.milo.opcua.stack.core.serialization.codecs.GenericDataTypeCodec
import org.eclipse.milo.opcua.stack.core.serialization.codecs.SerializationContext
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant
import java.util.*

abstract class BaseCodec<T>(val clazz: Class<T>) : GenericDataTypeCodec<T>() {

    val name = clazz::class.simpleName

    override fun getType(): Class<T> = clazz
}


/**
 * Wrapper for vocabulary array.
 */
data class VocabularyListDataType(val vocabularies: Array<VocabularyDataType>) : UaStructure {

    override fun getXmlEncodingId(): NodeId = MasterDataNamespace.VocabularyListDataType_Encoding_DefaultBinary

    override fun getTypeId(): NodeId = MasterDataNamespace.VocabularyListDataType_NodeId

    override fun getBinaryEncodingId(): NodeId = MasterDataNamespace.VocabularyListDataType_Encoding_DefaultBinary

    companion object Codec : BaseCodec<VocabularyListDataType>(VocabularyListDataType::class.java) {


        override fun encode(contexnt: SerializationContext?, data: VocabularyListDataType, encoder: UaEncoder) {

            encoder.writeStructArray("Vocabularies", data.vocabularies, MasterDataNamespace.VocabularyDataType_Encoding_DefaultBinary)
        }

        override fun decode(context: SerializationContext?, decoder: UaDecoder): VocabularyListDataType {

            val elements = decoder.readStructArray("Vocabularies",
                    MasterDataNamespace.VocabularyDataType_Encoding_DefaultBinary) as Array<VocabularyDataType>

            return VocabularyListDataType(elements)
        }
    }
}


/**
 * Vocabulary types, identified by the URI
 */
data class VocabularyDataType(val uri: String,
                              val vocabularyElements: VocabularyElementList) {

    companion object Codec : BaseCodec<VocabularyDataType>(VocabularyDataType::class.java) {

        override fun encode(contexnt: SerializationContext?, data: VocabularyDataType, encoder: UaEncoder) {

            encoder.writeString("Uri", data.uri)
            encoder.writeStruct("Vocabularies", data.vocabularyElements, MasterDataNamespace.VocabularyElementList_Encoding_DefaultBinary)
        }

        override fun decode(context: SerializationContext?, decoder: UaDecoder): VocabularyDataType {

            val uri = decoder.readString("Uri")
            val elements = decoder.readStruct("VocabularyElements",
                    MasterDataNamespace.VocabularyElementList_Encoding_DefaultBinary) as VocabularyElementList
            return VocabularyDataType(uri, elements)
        }
    }
}

/**
 * Wrapper for vocabulary elements array.
 */
data class VocabularyElementList(val vocabularyElements: Array<VocabularyElement>) {

    companion object Codec : BaseCodec<VocabularyElementList>(VocabularyElementList::class.java) {

        override fun encode(contexnt: SerializationContext?, data: VocabularyElementList, encoder: UaEncoder) {
            encoder.writeStructArray("VocabularyElements", data.vocabularyElements, MasterDataNamespace.VocabularyElement_Encoding_DefaultBinary)
        }

        override fun decode(context: SerializationContext?, decoder: UaDecoder): VocabularyElementList {

            val elements = decoder.readStructArray("VocabularyElements",
                    MasterDataNamespace.VocabularyElement_Encoding_DefaultBinary) as Array<VocabularyElement>
            return VocabularyElementList(elements)
        }

    }
}


/**
 * Vocabulary element, or the master data item.
 * @param uri item identifier
 * @param attributes master data attributes
 * @param children children, of same type as the vocabulary element
 */
data class VocabularyElement(val uri: String,
                             val attributes: Array<VocabularyAttribute>,
                             val children: Array<String>) {

    companion object Codec : BaseCodec<VocabularyElement>(VocabularyElement::class.java) {

        override fun encode(context: SerializationContext?, data: VocabularyElement, encoder: UaEncoder) {

            encoder.writeString("Uri", data.uri)
            encoder.writeStructArray("VocabularyElements", data.attributes, MasterDataNamespace.VocabularyElementAttribute_Encoding_DefaultBinary)
            encoder.writeArray("Children", data.children, encoder::writeString)

        }

        override fun decode(context: SerializationContext?, decoder: UaDecoder): VocabularyElement {

            val uri = decoder.readString("Uri")
            val attributes = decoder.readStructArray("VocabularyElements",
                    MasterDataNamespace.VocabularyElementAttribute_Encoding_DefaultBinary) as Array<VocabularyAttribute>
            val children = decoder.readArray<String>("Children", decoder::readString, String::class.java) as Array<String>

            return VocabularyElement(uri, attributes, children)
        }
    }
}

/**
 * Wrapper for the key-value attributes of each VocabularyElement
 */
data class VocabularyAttribute(val uri: String,
                               val value: Any) : UaStructure {

    override fun getXmlEncodingId(): NodeId = MasterDataNamespace.VocabularyElementAttribute_Encoding_DefaultBinary

    override fun getTypeId(): NodeId = MasterDataNamespace.VocabularyElementAttribute_NodeId

    override fun getBinaryEncodingId(): NodeId = MasterDataNamespace.VocabularyElementAttribute_Encoding_DefaultBinary


    companion object Codec : BaseCodec<VocabularyAttribute>(VocabularyAttribute::class.java) {

        override fun encode(contexnt: SerializationContext?, data: VocabularyAttribute, encoder: UaEncoder) {

            encoder.writeString("Uri", data.uri)
            encoder.writeVariant("Value", Variant(data.value))
        }

        override fun decode(context: SerializationContext?, decoder: UaDecoder): VocabularyAttribute {

            val uri = decoder.readString("Uri")
            val value = decoder.readVariant("Value")
            return VocabularyAttribute(uri, value.value)
        }
    }
}

data class HeaderDataType(val msgId: UUID,
                          val correlationId: UUID,
                          val initiatorId: String,
                          val partnerId: String,
                          val extensionParameters: Array<Variant> = emptyArray()) : UaStructure {

    override fun getXmlEncodingId(): NodeId = MasterDataNamespace.HeaderDataType_Encoding_DefaultBinary

    override fun getTypeId(): NodeId = MasterDataNamespace.HeaderDataType_NodeId

    override fun getBinaryEncodingId(): NodeId = MasterDataNamespace.HeaderDataType_Encoding_DefaultBinary


    companion object Codec : BaseCodec<HeaderDataType>(HeaderDataType::class.java) {

        override fun encode(contexnt: SerializationContext?, data: HeaderDataType, encoder: UaEncoder) {

            encoder.writeGuid("MessageID", data.msgId)
            encoder.writeGuid("CorrelationID", data.correlationId)
            encoder.writeString("InitiatorID", data.initiatorId)
            encoder.writeString("PartnerID", data.partnerId)
            encoder.writeArray("ExtensionParameters", data.extensionParameters, encoder::writeVariant)
        }

        override fun decode(context: SerializationContext?, decoder: UaDecoder): HeaderDataType {

            return HeaderDataType(
                    decoder.readGuid("MessageID"),
                    decoder.readGuid("CorrelationID"),
                    decoder.readString("InitiatorID"),
                    decoder.readString("PartnerID"),
                    decoder.readArray("ExtensionParameters", decoder::readVariant, Variant::class.java))
        }
    }

}

data class GetMasterDataInputDataType(val masterDataQuery: Array<String> = emptyArray()) : UaStructure {

    override fun getXmlEncodingId(): NodeId = MasterDataNamespace.GetMasterDataInputDataType_Encoding_DefaultBinary

    override fun getTypeId(): NodeId = MasterDataNamespace.GetMasterDataInputDataType_NodeId

    override fun getBinaryEncodingId(): NodeId = MasterDataNamespace.GetMasterDataInputDataType_Encoding_DefaultBinary

    companion object Codec : BaseCodec<GetMasterDataInputDataType>(GetMasterDataInputDataType::class.java) {

        override fun encode(contexnt: SerializationContext?, data: GetMasterDataInputDataType, encoder: UaEncoder) {
            encoder.writeArray("MasterDataQuery", data.masterDataQuery, encoder::writeString)
        }

        override fun decode(context: SerializationContext?, decoder: UaDecoder): GetMasterDataInputDataType {

            return GetMasterDataInputDataType(
                    decoder.readArray("MasterDataQuery", decoder::readString, String::class.java))
        }
    }

}