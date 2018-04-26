package io.rezn.opcserialization.server

import io.rezn.opcserialization.shared.MasterDataNamespace
import org.eclipse.milo.opcua.sdk.core.Reference
import org.eclipse.milo.opcua.sdk.server.OpcUaServer
import org.eclipse.milo.opcua.sdk.server.api.*
import org.eclipse.milo.opcua.sdk.server.api.Namespace
import org.eclipse.milo.opcua.sdk.server.nodes.*
import org.eclipse.milo.opcua.stack.core.UaException
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UShort
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId
import org.eclipse.milo.opcua.stack.core.types.structured.WriteValue
import java.util.concurrent.CompletableFuture
import org.eclipse.milo.opcua.stack.core.StatusCodes
import org.eclipse.milo.opcua.stack.core.util.FutureUtils
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue
import org.eclipse.milo.opcua.sdk.server.api.MethodInvocationHandler
import java.util.*


class NamespaceHandler(val server: OpcUaServer,
                       private val namespaceIndex: UShort) : Namespace {

    override fun getNamespaceUri(): String = MasterDataNamespace.URI

    override fun getNamespaceIndex(): UShort = namespaceIndex

    override fun getInvocationHandler(methodId: NodeId?): Optional<MethodInvocationHandler> {

        return server.nodeMap.getNode(methodId).flatMap {

            val node = it as? UaMethodNode

            node?.let { node.invocationHandler } ?: Optional.empty()
        }
    }

    override fun browse(context: AccessContext?, nodeId: NodeId?) =

            server.nodeMap[nodeId]
                    ?.let { CompletableFuture.completedFuture(it.references.toMutableList()) }
                    ?: FutureUtils.failedFuture<MutableList<Reference>>(UaException(StatusCodes.Bad_NodeIdUnknown))


    override fun read(context: AttributeManager.ReadContext, p1: Double?, timestamps: TimestampsToReturn?, readValueIds: MutableList<ReadValueId>) {

        val ids = readValueIds.map { id ->
            server.nodeMap[id.nodeId]?.let { node ->

                node.readAttribute(
                        AttributeContext(context),
                        id.getAttributeId(),
                        timestamps,
                        id.getIndexRange(),
                        id.getDataEncoding())

            } ?: DataValue(StatusCodes.Bad_NodeIdUnknown)
        }

        context.complete(ids)
    }

    override fun onDataItemsDeleted(p0: MutableList<DataItem>?) = TODO("not implemented")

    override fun onMonitoringModeChanged(p0: MutableList<MonitoredItem>?) = TODO("not implemented")

    override fun write(p0: AttributeManager.WriteContext?, p1: MutableList<WriteValue>?) = TODO("not implemented")

    override fun onDataItemsCreated(p0: MutableList<DataItem>?) = TODO("not implemented")

    override fun onDataItemsModified(p0: MutableList<DataItem>?) = TODO("not implemented")
}



