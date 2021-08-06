package br.com.zup.edu.application.extension

import br.com.zup.edu.RemoverChavePixRequest
import br.com.zup.edu.application.dtos.pix.ConsultaChavePixDTO

fun RemoverChavePixRequest.toDTO(): ConsultaChavePixDTO {
    return ConsultaChavePixDTO(
        pixId = this.pixId,
        clienteId = this.codigoCliente
    )
}