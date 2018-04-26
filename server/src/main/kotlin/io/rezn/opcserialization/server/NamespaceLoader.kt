package io.rezn.opcserialization.server

import io.rezn.opcserialization.shared.*
import jdk.nashorn.internal.runtime.ArgumentSetter
import org.eclipse.milo.opcua.sdk.core.Reference
import org.eclipse.milo.opcua.sdk.server.OpcUaServer
import org.eclipse.milo.opcua.sdk.server.model.nodes.objects.FolderNode
import org.eclipse.milo.opcua.sdk.server.nodes.*
import org.eclipse.milo.opcua.sdk.server.util.AnnotationBasedInvocationHandler
import org.eclipse.milo.opcua.stack.core.Identifiers
import org.eclipse.milo.opcua.stack.core.UaException
import org.eclipse.milo.opcua.stack.core.types.OpcUaBinaryDataTypeDictionary
import org.eclipse.milo.opcua.stack.core.types.OpcUaDataTypeManager
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UShort
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass
import org.eclipse.milo.opcua.stack.core.types.structured.Argument


class Loader(val server: OpcUaServer,
             val namespaceIndex: UShort) {

    companion object {
        fun load(server: OpcUaServer, namespaceIndex: UShort) {
            Loader(server, namespaceIndex)
        }
    }

    private val nodeFactory = NodeFactory(server.nodeMap, server.objectTypeManager, server.variableTypeManager)

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

    init {
        val objectsFolder = server.nodeMap.get(Identifiers.ObjectsFolder) as FolderNode

        objectsFolder.addObject(obj).addMethod(method)

        val dictionary = OpcUaBinaryDataTypeDictionary(MasterDataNamespace.URI)

        dataTypes.forEach { addType(it, dictionary) }

        OpcUaDataTypeManager.getInstance().registerTypeDictionary(dictionary)
    }


    fun addType(dataType: DataType<out Any>,
                dictionary: OpcUaBinaryDataTypeDictionary) {

        val dataTypeNode = UaDataTypeNode(
                server.nodeMap,
                dataType.nodeId,
                QualifiedName(namespaceIndex, dataType.name),
                LocalizedText.english(dataType.name),
                LocalizedText.english(dataType.name),
                uint(0),
                uint(0),
                false
        )

        // Inverse ref to Structure
        dataTypeNode.addReference(Reference(
                dataType.nodeId,
                Identifiers.HasSubtype,
                Identifiers.BaseDataType.expanded(),
                NodeClass.DataType,
                false
        ))

        server.uaNamespace.addReference(
                Identifiers.BaseDataType,
                Identifiers.HasSubtype,
                true,
                dataType.nodeId.expanded(),
                NodeClass.DataType)


        server.uaNamespace.addReference(
                dataType.nodeId,
                Identifiers.HasEncoding,
                true,
                dataType.binaryEncoding.expanded(),
                NodeClass.DataType
        )

        dictionary.registerStructCodec(
                dataType.codec.asBinaryCodec(),
                dataType.name,
                dataType.binaryEncoding
        )
    }


    // extension function to add a method to an object
    @Throws(UaException::class)
    fun FolderNode.addObject(obj: Object): UaObjectNode {

        // build the object type
        val objectTypeNode = UaObjectTypeNode.builder(server.nodeMap)
                .setNodeId(obj.typeNode)
                .setBrowseName(QualifiedName(namespaceIndex, obj.typeName))
                .setDisplayName(LocalizedText.english(obj.typeName))
                .setIsAbstract(false)
                .build()


        // register type in the server's manager
        server.objectTypeManager.registerObjectType(
                objectTypeNode.nodeId,
                UaObjectNode::class.java,
                ::UaObjectNode)

        // Add the object as subtype of BaseObjectType.
        server.uaNamespace.addReference(
                Identifiers.BaseObjectType,
                Identifiers.HasSubtype,
                true,
                objectTypeNode.nodeId.expanded(),
                NodeClass.ObjectType
        )

        // Add the inverse SubtypeOf relationship.
        objectTypeNode.addReference(Reference(
                objectTypeNode.nodeId,
                Identifiers.HasSubtype,
                Identifiers.BaseObjectType.expanded(),
                NodeClass.ObjectType,
                false
        ))

        // Add it into the address space.
        server.nodeMap.addNode(objectTypeNode)

        // instantiate the object
        val myObject = nodeFactory.createObject(
                obj.instanceNode,
                QualifiedName(namespaceIndex, obj.instanceName),
                LocalizedText.english(obj.instanceName),
                objectTypeNode.nodeId
        )

        // Add forward and inverse references from the folder.
        this.addReference(Reference(
                this.nodeId,
                Identifiers.Organizes,
                myObject.nodeId.expanded(),
                myObject.nodeClass,
                true))

        // add folder reference to the object
        myObject.addReference(Reference(
                myObject.nodeId,
                Identifiers.Organizes,
                this.nodeId.expanded(),
                this.nodeClass,
                false))

        return myObject
    }

    // extension function to add a method to an object
    fun UaObjectNode.addMethod(method: Method) {

        val methodNode = UaMethodNode.builder(server.getNodeMap())
                .setNodeId(method.nodeId)
                .setBrowseName(QualifiedName(namespaceIndex, method.name))
                .setDisplayName(LocalizedText(null, method.name))
                .setDescription(
                        LocalizedText.english(method.description))
                .build()


        val invocationHandler = AnnotationBasedInvocationHandler.fromAnnotatedObject(
                server.nodeMap, method.target)

        val oldArg = invocationHandler.outputArguments[0]

        val newArg = Argument(
                oldArg.name,
                MasterDataNamespace.VocabularyListDataType_NodeId,
                oldArg.valueRank,
                oldArg.arrayDimensions,
                oldArg.description)


        val newHandler = AnnotationBasedInvocationHandler(server.nodeMap,
                invocationHandler.inputArguments.toMutableList(),
                mutableListOf(newArg),
                method.target)

        methodNode.setProperty(UaMethodNode.OutputArguments, newHandler.outputArguments)
        methodNode.setProperty(UaMethodNode.InputArguments, newHandler.inputArguments)
        methodNode.setInvocationHandler(newHandler)

        server.getNodeMap().addNode(methodNode)

        this.addReference(Reference(
                this.nodeId,
                Identifiers.HasComponent,
                methodNode.getNodeId().expanded(),
                methodNode.getNodeClass(),
                true
        ))

        methodNode.addReference(Reference(
                methodNode.getNodeId(),
                Identifiers.HasComponent,
                this.nodeId.expanded(),
                this.nodeClass,
                false
        ))
    }
}

