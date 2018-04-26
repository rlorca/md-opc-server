package io.rezn.opcserialization.server

import org.eclipse.milo.opcua.stack.core.serialization.codecs.GenericDataTypeCodec
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId


data class Method(val nodeId: NodeId,
                  val name: String,
                  val description: String,
                  val target: Any)

data class Object(val typeNode: NodeId,
                  val typeName: String,
                  val instanceNode: NodeId,
                  val instanceName: String)

data class DataType<T : Any?>(val name: String,
                              val nodeId: NodeId,
                              val binaryEncoding: NodeId,
                              val codec: GenericDataTypeCodec<T>)