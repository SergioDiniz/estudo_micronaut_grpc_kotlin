package br.com.zup.application.dtos

import br.com.zup.edu.TipoChave
import br.com.zup.edu.TipoConta
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class ChavePixResponseDTO(
    @JsonProperty("pixId")
    val pixId: String,
    @JsonProperty("clienteId")
    val clienteId: String,
    @JsonProperty("tipoChave")
    val tipoChave: TipoChave,
    @JsonProperty("valorChave")
    val valorChave: String,
    @JsonProperty("tipoConta")
    val tipoConta: TipoConta,
    @JsonProperty("dataCriacao")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    val dataCriacao: LocalDateTime
)
