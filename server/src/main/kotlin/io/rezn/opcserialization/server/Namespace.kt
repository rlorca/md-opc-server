package io.rezn.opcserialization.server

import io.rezn.opcserialization.shared.*

val obj = Object(MasterDataNamespace.MasterDataManagerType,
        "MasterDataManagerType",
        MasterDataNamespace.MasterDataManager,
        "MasterDataManager")


val method = Method(MasterDataNamespace.MasterDataManagerType_GetMasterData,
        "GetMasterData",
        "Returns the the master data values for the query.",
        GetMasterDataMethod())

val dataTypes = arrayOf(
        DataType("VocabularyListDataType",
                MasterDataNamespace.VocabularyListDataType_NodeId,
                MasterDataNamespace.VocabularyListDataType_Encoding_DefaultBinary,
                VocabularyListDataType.Codec),

        DataType("VocabularyElementListDataType",
                MasterDataNamespace.VocabularyElementList_NodeId,
                MasterDataNamespace.VocabularyElementList_Encoding_DefaultBinary,
                VocabularyElementList.Codec),

        DataType("VocabularyElement",
                MasterDataNamespace.VocabularyElement_NodeId,
                MasterDataNamespace.VocabularyElement_Encoding_DefaultBinary,
                VocabularyElement.Codec),

        DataType("VocabularyAttribute",
                MasterDataNamespace.VocabularyElementAttribute_NodeId,
                MasterDataNamespace.VocabularyElementAttribute_Encoding_DefaultBinary,
                VocabularyAttribute.Codec),

        DataType(
                "VocabularyDataType",
                MasterDataNamespace.VocabularyDataType_NodeId,
                MasterDataNamespace.VocabularyDataType_Encoding_DefaultBinary,
                VocabularyDataType.Codec)
)