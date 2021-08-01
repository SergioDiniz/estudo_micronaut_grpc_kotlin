package br.com.zup.edu.application.dtos

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class DeletePixKeyResponse(
    @JsonProperty(value = "key")
    val chave: String,
    @JsonProperty(value = "participant")
    val ispb: String,
    @JsonProperty(value = "deletedAt")
    val deletadoEm: LocalDateTime
)
