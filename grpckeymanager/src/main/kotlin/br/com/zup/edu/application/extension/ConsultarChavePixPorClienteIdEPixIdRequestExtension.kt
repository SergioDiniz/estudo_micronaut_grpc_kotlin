package br.com.zup.edu.application.extension

import br.com.zup.edu.ConsultarChavePixPorClienteIdEPixIdRequest
import br.com.zup.edu.application.dtos.pix.ConsultaChavePixDTO

fun ConsultarChavePixPorClienteIdEPixIdRequest.toDTO(): ConsultaChavePixDTO {
    return ConsultaChavePixDTO(
        pixId = this.pixId,
        clienteId = this.clienteId
    )
}