package br.com.zup.edu.application.dtos

import br.com.zup.edu.TipoChave
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import java.time.LocalDateTime

data class CreatePixKeyResponse(
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
