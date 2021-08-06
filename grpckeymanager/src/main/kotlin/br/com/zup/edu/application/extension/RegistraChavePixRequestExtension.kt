package br.com.zup.edu.application.extension

import br.com.zup.edu.RegistraChavePixRequest
import br.com.zup.edu.application.dtos.pix.RegistraChavePixDTO

fun RegistraChavePixRequest.toDTO(): RegistraChavePixDTO {
    return RegistraChavePixDTO(
        clienteId = this.codigoCliente,
        tipoChave = this.tipoChave,
        chave = this.valorChave,
        tipoConta = this.tipoConta
    )
}
