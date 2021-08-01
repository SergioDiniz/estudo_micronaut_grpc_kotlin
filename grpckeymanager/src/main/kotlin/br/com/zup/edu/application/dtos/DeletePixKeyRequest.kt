package br.com.zup.edu.application.dtos

import br.com.zup.edu.application.constants.ISPB
import com.fasterxml.jackson.annotation.JsonProperty

data class DeletePixKeyRequest(
    @JsonProperty(value = "key")
    val chave: String,
    @JsonProperty(value = "participant")
    val ispb: String? = ISPB.ITAU_UNIBANCO_SA
)
