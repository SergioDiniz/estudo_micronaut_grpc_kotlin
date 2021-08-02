package br.com.zup.edu.application.dtos.bcb

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class PixKeyDetailsResponse (
    @JsonProperty(value = "keyType")
    val tipoChave: String,
    @JsonProperty(value = "key")
    val chave: String,
    @JsonProperty(value = "bankAccount")
    val contaBancaria: ContaBancariaDTO,
    @JsonProperty(value = "owner")
    val dono: DonoInfoDTO,
    @JsonProperty(value = "createdAt")
    val criadoEm: LocalDateTime
)