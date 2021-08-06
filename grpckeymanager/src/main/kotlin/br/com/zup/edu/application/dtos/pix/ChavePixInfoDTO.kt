package br.com.zup.edu.application.dtos.pix

import br.com.zup.edu.TipoChave
import br.com.zup.edu.application.dtos.bcb.PixKeyDetailsResponse
import br.com.zup.edu.application.dtos.erp.DadosDaContaResponse
import br.com.zup.edu.domain.entites.ChavePix
import java.time.LocalDateTime


data class ChavePixInfoDTO(
    val pixId: String? = "",
    val clienteId: String? = "",
    val tipoChave: TipoChave,
    val valorChave: String,
    val titular: TitularDTO,
    val contaBancaria: ContaBancariaDTO,
    val dataCriacao: LocalDateTime,
){
    companion object {
        fun criar(chavePix: ChavePix, dadosConta: DadosDaContaResponse): ChavePixInfoDTO{
            return ChavePixInfoDTO(
                pixId = chavePix.id.toString(),
                clienteId = chavePix.clientId,
                tipoChave = chavePix.tipoChave,
                valorChave = chavePix.chave,
                titular = TitularDTO(
                    nome = dadosConta.titular.nome,
                    cpf = dadosConta.titular.cpf
                ),
                contaBancaria = ContaBancariaDTO(
                    nomeInstituicao = dadosConta.instituicao.nome,
                    agencia = dadosConta.agencia,
                    numero = dadosConta.numero,
                    tipo = dadosConta.tipo
                ),
                dataCriacao = chavePix.criadaEm
            )
        }

        fun criar(chavePixResponse: PixKeyDetailsResponse): ChavePixInfoDTO{
            return ChavePixInfoDTO(
                tipoChave = TipoChave.valueOf(chavePixResponse.tipoChave.descricao),
                valorChave = chavePixResponse.chave,
                titular = TitularDTO(
                    nome = chavePixResponse.dono.nome,
                    cpf = chavePixResponse.dono.cpf
                ),
                contaBancaria = ContaBancariaDTO(
                    nomeInstituicao = chavePixResponse.contaBancaria.ispb, //TODO consultar ISPB da Relação de participantes do STR
                    agencia = chavePixResponse.contaBancaria.agencia,
                    numero = chavePixResponse.contaBancaria.numero,
                    tipo = chavePixResponse.contaBancaria.tipo.name
                ),
                dataCriacao = chavePixResponse.criadoEm
            )
        }
    }
}

data class ContaBancariaDTO(
    val nomeInstituicao: String,
    val agencia: String,
    val numero: String,
    val tipo: String
)

data class TitularDTO(
    val nome: String,
    val cpf: String
)