package br.com.zup.edu.domain.service

import br.com.zup.edu.application.dtos.pix.ChavePixInfoDTO
import br.com.zup.edu.application.dtos.pix.RegistraChavePixDTO
import br.com.zup.edu.application.dtos.pix.ConsultaChavePixDTO
import br.com.zup.edu.domain.entites.ChavePix

interface ChavePixService {
    fun registraChavePix(chavePixDTO: RegistraChavePixDTO): ChavePix
    fun removerChavePix(consultaChavePixDTO: ConsultaChavePixDTO)
    fun listarChavePix(clienteId: String): List<ChavePix>
    fun consultarChavePixPorClienteIdEPixId(consultaChavePixDTO: ConsultaChavePixDTO): ChavePixInfoDTO
    fun consultarChavePix(chavePix: String): ChavePixInfoDTO
}