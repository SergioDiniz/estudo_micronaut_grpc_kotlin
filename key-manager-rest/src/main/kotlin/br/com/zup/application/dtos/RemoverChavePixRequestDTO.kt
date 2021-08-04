package br.com.zup.application.dtos

import com.fasterxml.jackson.annotation.JsonProperty

data class RemoverChavePixRequestDTO(
    @JsonProperty("codigoCliente")
    val codigoCliente: String
)
