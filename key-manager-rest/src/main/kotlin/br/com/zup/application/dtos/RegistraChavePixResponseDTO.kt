package br.com.zup.application.dtos

import com.fasterxml.jackson.annotation.JsonProperty

data class RegistraChavePixResponseDTO (
      @JsonProperty("pixId")
      val pixId: String
)
