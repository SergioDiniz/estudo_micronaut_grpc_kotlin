package br.com.zup.application.extension

import br.com.zup.application.dtos.ConsultarChavePixPorClienteIdEPixIdResponseDTO
import br.com.zup.application.dtos.ContaBancariaResponseDTO
import br.com.zup.edu.ConsultarChavePixPorClienteIdEPixIdResponse

fun ConsultarChavePixPorClienteIdEPixIdResponse.toDTO() : ConsultarChavePixPorClienteIdEPixIdResponseDTO{
    return ConsultarChavePixPorClienteIdEPixIdResponseDTO(
        pixId = this.pixId,
        clienteId = this.clienteId,
        tipoChave = this.tipoChave,
        valorChave = this.valorChave,
        nomeTitular = this.nomeTitular,
        cpfTitular = this.cpfTitular,
        contaBancaria = ContaBancariaResponseDTO(
            nomeInstituicao = this.contaBancaria.nomeInstituicao,
            agencia = this.contaBancaria.agencia,
            numero = this.contaBancaria.numero,
            tipo = this.contaBancaria.tipo
        ),
        dataCriacao = this.dataCriacao.toLocalDateTime()
    )
}