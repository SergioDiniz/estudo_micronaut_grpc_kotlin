package br.com.zup.edu.domain.service.impl

import br.com.zup.edu.TipoConta
import br.com.zup.edu.application.dtos.bcb.CreatePixKeyRequest
import br.com.zup.edu.application.dtos.bcb.DeletePixKeyRequest
import br.com.zup.edu.application.dtos.erp.DadosDaContaResponse
import br.com.zup.edu.application.dtos.pix.ChavePixInfoDTO
import br.com.zup.edu.application.dtos.pix.RegistraChavePixDTO
import br.com.zup.edu.application.dtos.pix.ConsultaChavePixDTO
import br.com.zup.edu.application.exceptions.ChaveNaoPertenceAoClienteException
import br.com.zup.edu.application.exceptions.ChavePixJaCadastradaException
import br.com.zup.edu.application.exceptions.ChavePixNaoCadastradaException
import br.com.zup.edu.application.exceptions.ClienteNaoEncontradoException
import br.com.zup.edu.application.integration.BCBIntegration
import br.com.zup.edu.application.integration.ERPItauIntegration
import br.com.zup.edu.domain.entites.ChavePix
import br.com.zup.edu.domain.enums.BCBTipoChave
import br.com.zup.edu.domain.repositories.ChavePixRepository
import br.com.zup.edu.domain.service.ChavePixService
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Singleton
import javax.validation.Valid

@Validated
@Singleton
class ChavePixServiceImpl(
    private val chavePixRepository: ChavePixRepository,
    private val erpItauIntegration: ERPItauIntegration,
    private val bcbIntegration: BCBIntegration
) : ChavePixService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun registraChavePix(@Valid chavePixDTO: RegistraChavePixDTO): ChavePix {

        logger.info("Validando se chave: ${chavePixDTO.chave} ja estar cadastrada.")
        if(chavePixRepository.existsByChave(chavePixDTO.chave)) throw ChavePixJaCadastradaException()

        logger.info("Registrando chave no BCB")
        val dadosConta = getDadosContaCliente(chavePixDTO.clienteId, chavePixDTO.tipoConta)
        val bcbResponse = bcbIntegration.cadastrarChavePix(CreatePixKeyRequest.criar(chavePixDTO, dadosConta)).body()

        //Atualizando valor da chave com o valor do cbc, que tera seu valor alterado caso seja ALEATORIA
        val chavePix = chavePixDTO.toDomain().copy(chave = bcbResponse.chave)

        logger.info("Salvando chave PIX")
        return chavePixRepository.save(chavePix)

    }

    override fun removerChavePix(consultaChavePixDTO: ConsultaChavePixDTO) {
        val chavePix = chavePixRepository.findById(UUID.fromString(consultaChavePixDTO.pixId))

        logger.info("Validando se PixId ${consultaChavePixDTO.pixId} existe para ser deletado.")
        if(chavePix.isEmpty) throw ChavePixNaoCadastradaException()

        logger.info("Validando se PixId ${consultaChavePixDTO.pixId} pertence ao dono.")
        if(chavePix.get().clientId != consultaChavePixDTO.clienteId) throw ChaveNaoPertenceAoClienteException(
            "O PixId : ${consultaChavePixDTO.pixId} não percente ao cliente informado(${consultaChavePixDTO.clienteId})."
        )

        logger.info("Deletando chave pix do BCB.")
        bcbIntegration.deletarChavePix(
            chave = chavePix.get().chave,
            deletePixKeyRequest = DeletePixKeyRequest(chave = chavePix.get().chave)
        )

        logger.info("Deletando PixId ${consultaChavePixDTO.pixId}.")
        chavePixRepository.delete(chavePix.get())
    }

    override fun listarChavePix(clienteId: String): List<ChavePix> {
        return chavePixRepository.findAllByClientId(clienteId)
    }

    override fun consultarChavePixPorClienteIdEPixId(consultaChavePixDTO: ConsultaChavePixDTO): ChavePixInfoDTO {
        logger.info("Consultando chaves pix por PixId: ${consultaChavePixDTO.pixId} e clienteId ${consultaChavePixDTO.clienteId}.")
        val chavePix = chavePixRepository.findById(UUID.fromString(consultaChavePixDTO.pixId))

        logger.info("Validando se PixId ${consultaChavePixDTO.pixId} existe.")
        if(chavePix.isEmpty) throw ChavePixNaoCadastradaException()

        logger.info("Validando se PixId ${consultaChavePixDTO.pixId} pertence ao dono.")
        if(chavePix.get().clientId != consultaChavePixDTO.clienteId) throw ChaveNaoPertenceAoClienteException(
            "O PixId : ${consultaChavePixDTO.pixId} não percente ao cliente informado(${consultaChavePixDTO.clienteId})."
        )

        logger.info("Consultando dados da conta no ERP")
        val dadosConta = getDadosContaCliente(consultaChavePixDTO.clienteId, chavePix.get().tipoConta)
        return ChavePixInfoDTO.criar(chavePix.get(), dadosConta)
    }

    override fun consultarChavePix(chavePix: String): ChavePixInfoDTO {
        logger.info("Validando chave.")
        BCBTipoChave.isTamanhoValido(chavePix)

        val chavePixInfoDTO = if (chavePixRepository.existsByChave(chavePix)) {
            logger.info("Consultando chaves pix pela chave: ${chavePix}.")
            val chavePix = chavePixRepository.findByChave(chavePix)
            val dadosConta = getDadosContaCliente(chavePix!!.clientId, chavePix.tipoConta)
            ChavePixInfoDTO.criar(chavePix, dadosConta)
        } else {
            logger.info("Consultando chaves pix: ${chavePix} no BCB.")
            val chavePixResponse = bcbIntegration.consultarChavePix(chavePix).body()
            //TODO throw caso chavePixResponse seja null
            ChavePixInfoDTO.criar(chavePixResponse)
        }

        return chavePixInfoDTO
    }


    private fun getDadosContaCliente(codigoCliente: String, tipoConta: TipoConta): DadosDaContaResponse {
        logger.info("Consultando conta do cliente: $codigoCliente no ERP Itau.")
        val erpResponse = erpItauIntegration.buscaContasClientePorTipo(codigoCliente, tipoConta.name)

        if(erpResponse.status != HttpStatus.OK) {
            throw ClienteNaoEncontradoException("Não foi possivel encontrar um cliente valido " +
                    "para o ID $codigoCliente e tipo da conta: $tipoConta.")
        }

        return erpResponse.body()
    }
}