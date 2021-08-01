package br.com.zup.edu.application.dtos

import br.com.zup.edu.domain.enums.BCBTipoPessoa
import com.fasterxml.jackson.annotation.JsonProperty

data class DonoInfoDTO(
    @JsonProperty(value = "type")
    val tipo: BCBTipoPessoa,
    @JsonProperty(value = "name")
    val nome: String,
    @JsonProperty(value = "taxIdNumber")
    val cpf: String
)
