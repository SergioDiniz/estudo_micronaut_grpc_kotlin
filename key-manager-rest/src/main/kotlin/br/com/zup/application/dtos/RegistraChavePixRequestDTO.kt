package br.com.zup.application.dtos

import br.com.zup.edu.TipoChave
import br.com.zup.edu.TipoConta
import com.fasterxml.jackson.annotation.JsonProperty

data class RegistraChavePixRequestDTO(
    @JsonProperty("codigoCliente")
    val codigoCliente: String,
    @JsonProperty("tipoChave")
    val tipoChave: TipoChave,
    @JsonProperty("valorChave")
    val valorChave: String,
    @JsonProperty("tipoConta")
    val tipoConta: TipoConta
)
