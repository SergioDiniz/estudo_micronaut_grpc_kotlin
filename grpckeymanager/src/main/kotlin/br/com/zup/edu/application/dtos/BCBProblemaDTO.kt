package br.com.zup.edu.application.dtos

import com.fasterxml.jackson.annotation.JsonProperty

data class BCBProblemaDTO(
    @JsonProperty(value = "type")
    val tipo: String,
    @JsonProperty(value = "status")
    val status: String,
    @JsonProperty(value = "title")
    val titulo: String,
    @JsonProperty(value = "detail")
    val detalhes: String,
    @JsonProperty(value = "violations")
    val violacoes: List<ViolacaoDTO>? = null
)

data class ViolacaoDTO(
    @JsonProperty(value = "field")
    val campo: String,
    @JsonProperty(value = "message")
    val mensagem: String
)
