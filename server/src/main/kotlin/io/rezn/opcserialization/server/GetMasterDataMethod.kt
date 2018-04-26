package io.rezn.opcserialization.server

import io.rezn.opcserialization.shared.*
import org.eclipse.milo.opcua.sdk.server.annotations.UaInputArgument
import org.eclipse.milo.opcua.sdk.server.annotations.UaMethod
import org.eclipse.milo.opcua.sdk.server.annotations.UaOutputArgument
import org.eclipse.milo.opcua.sdk.server.util.AnnotationBasedInvocationHandler
import org.eclipse.milo.opcua.stack.core.types.builtin.ExtensionObject

class GetMasterDataMethod {

    @UaMethod
    fun invoke(context:
               AnnotationBasedInvocationHandler.InvocationContext,

               @UaInputArgument(
                       name = "query",
                       description = "Query parameters.")
               query: Array<String>,

               @UaOutputArgument(
                       name = "result",
                       description = "Master data query result")
               result: AnnotationBasedInvocationHandler.Out<VocabularyListDataType>) {




        result.set(dummyMasterData())
    }

    fun dummyMasterData(): VocabularyListDataType {

        return VocabularyListDataType(arrayOf(
                VocabularyDataType("urn:epcglobal:epcis:vtype:BusinessLocation",
                VocabularyElementList(arrayOf(
                    VocabularyElement("urn:epc:id:sgln:714141.073460.0",
                            arrayOf(VocabularyAttribute("urn:epcglobal:epcis:mda:address", "avenida das nacoes unidas, 12901"),
                                    VocabularyAttribute("urn:epcglobal:epcis:mda:city", "Sao Paulo"),
                                    VocabularyAttribute("urn:epcglobal:epcis:mda:country", "Brasil")),
                            arrayOf("urn:epc:id:sgln:714141.073460.1"))
                ))),

                VocabularyDataType("urn:epcglobal:epcis:vtype:Partner",
                        VocabularyElementList(arrayOf(
                                VocabularyElement("0810283",
                                        arrayOf(VocabularyAttribute("urn:epcglobal:partner:contactperson:email", "test@example.org"),
                                                VocabularyAttribute("urn:epcglobal:partner:contactperson:businessAddress", "urn:epc:id:sgln:714141.073460.0")),
                                        emptyArray()))))))
    }
}

