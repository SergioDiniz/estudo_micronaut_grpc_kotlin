package br.com.zup.edu.application.dtos.bcb

import br.com.zup.edu.domain.enums.BCBTipoConta
import com.fasterxml.jackson.annotation.JsonProperty

data class ContaBancariaDTO(
    @JsonProperty(value = "participant")
    val ispb: String,
    @JsonProperty(value = "branch")
    val agencia: String,
    @JsonProperty(value = "accountNumber")
    val numero: String,
    @JsonProperty(value = "accountType")
    val tipo: BCBTipoConta
)
