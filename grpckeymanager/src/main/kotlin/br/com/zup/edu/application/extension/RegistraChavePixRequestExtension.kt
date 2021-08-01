package br.com.zup.edu.application.extension

import br.com.zup.edu.RegistraChavePixRequest
import br.com.zup.edu.domain.entites.ChavePix

fun RegistraChavePixRequest.toDomain(): ChavePix {
    return ChavePix(
        clientId = this.codigoCliente,
        tipoChave = this.tipoChave,
        chave = this.valorChave,
        tipoConta = this.tipoConta
    )
}
