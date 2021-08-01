package br.com.zup.edu.application.dtos

import br.com.zup.edu.RegistraChavePixRequest
import br.com.zup.edu.application.constants.ISPB
import br.com.zup.edu.domain.enums.BCBTipoChave
import br.com.zup.edu.domain.enums.BCBTipoConta
import br.com.zup.edu.domain.enums.BCBTipoPessoa
import com.fasterxml.jackson.annotation.JsonProperty

data class CreatePixKeyRequest(
    @JsonProperty(value = "keyType")
    val tipoChave: BCBTipoChave,
    @JsonProperty(value = "key")
    val chave: String,
    @JsonProperty(value = "bankAccount")
    val contaBancaria: ContaBancariaDTO,
    @JsonProperty(value = "owner")
    val dono: DonoInfoDTO
){
    companion object {
        fun criar(request: RegistraChavePixRequest, dadosConta: DadosDaContaResponse) = CreatePixKeyRequest(
            tipoChave = BCBTipoChave.CPF, //TODO request.tipoChave
            chave = request.valorChave,
            contaBancaria = ContaBancariaDTO(
                ispb = ISPB.ITAU_UNIBANCO_SA,
                agencia = dadosConta.agencia,
                numero = dadosConta.numero,
                tipo = BCBTipoConta.CACC //TODO dadosConta.tipo
            ),
            dono = DonoInfoDTO(
                tipo = BCBTipoPessoa.NATURAL_PERSON, //TODO de onde pegar?
                nome = dadosConta.titular.nome,
                cpf = dadosConta.titular.cpf
            )
        )
    }
}
