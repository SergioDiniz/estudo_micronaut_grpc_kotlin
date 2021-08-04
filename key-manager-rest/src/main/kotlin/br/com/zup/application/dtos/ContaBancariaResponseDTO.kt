package br.com.zup.application.dtos

import com.fasterxml.jackson.annotation.JsonProperty

data class ContaBancariaResponseDTO (
    @JsonProperty("nomeInstituicao")
    val nomeInstituicao: String,
    @JsonProperty("agencia")
    val agencia: String,
    @JsonProperty("numero")
    val numero: String,
    @JsonProperty("tipo")
    val tipo: String
)