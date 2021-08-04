package br.com.zup.application.extension

import br.com.zup.application.dtos.ChavePixResponseDTO
import br.com.zup.edu.ChavePixResponse

fun ChavePixResponse.toDTO() : ChavePixResponseDTO{
    return ChavePixResponseDTO(
        pixId = this.pixId,
        clienteId = this.clienteId,
        tipoChave = this.tipoChave,
        valorChave = this.valorChave,
        tipoConta = this.tipoConta,
        dataCriacao = this.dataCriacao.toLocalDateTime()
    )
}