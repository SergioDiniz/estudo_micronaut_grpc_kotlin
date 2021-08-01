package br.com.zup.edu.application.dtos.erp

import java.util.*

data class DadosDaContaResponse(
    val tipo: String,
    val instituicao: InstituicaoResponse,
    val agencia: String,
    val numero: String,
    val titular: TitularResponse
)

data class InstituicaoResponse(
    val nome: String,
    val ispb: String
)

data class TitularResponse(
    val id: UUID,
    val nome: String,
    val cpf: String
)
