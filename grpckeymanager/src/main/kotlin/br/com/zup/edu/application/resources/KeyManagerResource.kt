package br.com.zup.edu.application.resources

import br.com.zup.edu.ChavePixResponse
import br.com.zup.edu.ConsultarChavePixPorClienteIdEPixIdRequest
import br.com.zup.edu.ConsultarChavePixPorClienteIdEPixIdResponse
import br.com.zup.edu.ConsultarChavePixRequest
import br.com.zup.edu.ContaBancariaResponse
import br.com.zup.edu.KeyManagerServiceGrpc
import br.com.zup.edu.ListarChavePixRequest
import br.com.zup.edu.ListarChavePixResponse
import br.com.zup.edu.RegistraChavePixRequest
import br.com.zup.edu.RegistraChavePixResponse
import br.com.zup.edu.RemoverChavePixRequest
import br.com.zup.edu.TipoChave
import br.com.zup.edu.TipoConta
import br.com.zup.edu.application.dtos.bcb.CreatePixKeyRequest
import br.com.zup.edu.application.dtos.erp.DadosDaContaResponse
import br.com.zup.edu.application.dtos.bcb.DeletePixKeyRequest
import br.com.zup.edu.application.dtos.bcb.PixKeyDetailsResponse
import br.com.zup.edu.application.exceptions.ChaveNaoPertenceAoClienteException
import br.com.zup.edu.application.exceptions.ChavePixJaCadastradaException
import br.com.zup.edu.application.exceptions.ChavePixNaoCadastradaException
import br.com.zup.edu.application.exceptions.ClienteNaoEncontradoException
import br.com.zup.edu.application.extension.toDomain
import br.com.zup.edu.application.extension.toGoogleTimestamp
import br.com.zup.edu.application.integration.BCBIntegration
import br.com.zup.edu.domain.repositories.ChavePixRepository
import br.com.zup.edu.application.integration.ERPItauIntegration
import br.com.zup.edu.domain.entites.ChavePix
import br.com.zup.edu.domain.enums.BCBTipoChave
import com.google.protobuf.Empty
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.http.HttpStatus
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Singleton

@Singleton
class KeyManagerResource(
    private val chavePixRepository: ChavePixRepository,
    private val erpItauIntegration: ERPItauIntegration,
    private val bcbIntegration: BCBIntegration
) : KeyManagerServiceGrpc.KeyManagerServiceImplBase() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun registraChavePix(request: RegistraChavePixRequest, responseObserver: StreamObserver<RegistraChavePixResponse>){
        runCatching {
            logger.info("Validando valor da chave.")
            BCBTipoChave.comDescricao(request.tipoChave.name).isValida(request.valorChave)

            logger.info("Validando se chave: ${request.valorChave} ja estar cadastrada.")
            if(chavePixRepository.existsByChave(request.valorChave)) throw ChavePixJaCadastradaException()

            logger.info("Registrando chave no BCB")
            val dadosConta = getDadosContaCliente(request.codigoCliente, request.tipoConta)
            val bcbResponse = bcbIntegration.cadastrarChavePix(CreatePixKeyRequest.criar(request, dadosConta)).body()

            //Validando se a chave é ALEATORIA para usar o valor do bcbResponse
            val chavePix = if(request.tipoChave != TipoChave.ALEATORIA) request.toDomain()
                           else request.toDomain().copy(chave = bcbResponse.chave)

            logger.info("Salvando chave PIX")
            chavePixRepository.save(chavePix)

            responseObserver.onNext(
                RegistraChavePixResponse.newBuilder()
                    .setPixId(chavePix.id.toString())
                    .build()
            )
            responseObserver.onCompleted()
        }.onFailure { ex ->
            logger.error("Um erro ocorreu durante o processamento da requisição de registraChavePix", ex)
            if(ex is ChavePixJaCadastradaException) responseObserver.onError(Status.ALREADY_EXISTS.asRuntimeException())
            else responseObserver.onError(Status.INTERNAL.withDescription(ex.message).asRuntimeException())
        }

    }

    override fun removerChavePix(request: RemoverChavePixRequest, responseObserver: StreamObserver<Empty>){
        runCatching {
            val chavePix = chavePixRepository.findById(UUID.fromString(request.pixId))

            logger.info("Validando se PixId ${request.pixId} existe para ser deletado.")
            if(chavePix.isEmpty) throw ChavePixNaoCadastradaException()

            logger.info("Validando se PixId ${request.pixId} pertence ao dono.")
            if(chavePix.get().clientId != request.codigoCliente) throw ChaveNaoPertenceAoClienteException(
                "O PixId : ${request.pixId} não percente ao cliente informado(${request.codigoCliente})."
            )

            logger.info("Deletando chave pix do BCB.")
            bcbIntegration.deletarChavePix(
                chave = chavePix.get().chave,
                deletePixKeyRequest = DeletePixKeyRequest(chave = chavePix.get().chave)
            )

            logger.info("Deletando PixId ${request.pixId}.")
            chavePixRepository.delete(chavePix.get())

            responseObserver.onNext(Empty.newBuilder().build())
            responseObserver.onCompleted()
        }.onFailure { ex ->
            logger.error("Um erro ocorreu durante o processamento da requisição de removerChavePix", ex)
            if(ex is ChavePixNaoCadastradaException) responseObserver.onError(Status.NOT_FOUND.asRuntimeException())
            else responseObserver.onError(Status.INTERNAL.withDescription(ex.message).asRuntimeException())
        }
    }

    override fun listarChavePix(request: ListarChavePixRequest, responseObserver: StreamObserver<ListarChavePixResponse>){
        runCatching {
            val builder = ListarChavePixResponse.newBuilder()

            logger.info("Consultando todos as chaves pix para o cliente ${request.clienteId}")
            chavePixRepository.findAllByClientId(request.clienteId).map { chave ->
                //Interagindo na lista retornada do banco e criando novos ChavePixResponse
                //E adicionando no builder, que será o "corpo do nosso response"
                builder.addChavesPix(mapToChavePixResponse(chave))
            }

            responseObserver.onNext(builder.build())
            responseObserver.onCompleted()
        }.onFailure { ex ->
            logger.error("Um erro ocorreu durante o processamento da requisição de listarChavePix", ex)
            responseObserver.onError(Status.INTERNAL.withDescription(ex.message).asRuntimeException())
        }
    }

    override fun consultarChavePixPorClienteIdEPixId(request: ConsultarChavePixPorClienteIdEPixIdRequest,
                                                     responseObserver: StreamObserver<ConsultarChavePixPorClienteIdEPixIdResponse>){
        runCatching {
            logger.info("Consultando chaves pix por PixId: ${request.pixId} e clienteId ${request.clienteId}.")
            val chavePix = chavePixRepository.findById(UUID.fromString(request.pixId))

            logger.info("Validando se PixId ${request.pixId} existe.")
            if(chavePix.isEmpty) throw ChavePixNaoCadastradaException()

            logger.info("Validando se PixId ${request.pixId} pertence ao dono.")
            if(chavePix.get().clientId != request.clienteId) throw ChaveNaoPertenceAoClienteException(
                "O PixId : ${request.pixId} não percente ao cliente informado(${request.clienteId})."
            )

            logger.info("Consultando dados da conta no ERP")
            val dadosConta = getDadosContaCliente(request.clienteId, chavePix.get().tipoConta)

            responseObserver.onNext(mapToConsultarChavePixPorClienteIdEPixIdResponse(chavePix.get(), dadosConta))
            responseObserver.onCompleted()
        }.onFailure { ex ->
            logger.error("Um erro ocorreu durante o processamento da requisição de consultarChavePixPorClienteIdEPixId", ex)
            when(ex){
                is ChavePixNaoCadastradaException -> responseObserver.onError(Status.NOT_FOUND.asRuntimeException())
                is ClienteNaoEncontradoException -> responseObserver.onError(Status.NOT_FOUND.withDescription(ex.message).asRuntimeException())
                else -> responseObserver.onError(Status.INTERNAL.withDescription(ex.message).asRuntimeException())
            }
        }
    }

    override fun consultarChavePix(request: ConsultarChavePixRequest, responseObserver: StreamObserver<ConsultarChavePixPorClienteIdEPixIdResponse>){
        runCatching {
            logger.info("Validando chave.")
            BCBTipoChave.isTamanhoValido(request.chavePix)

            val response = if (chavePixRepository.existsByChave(request.chavePix)) {
                logger.info("Consultando chaves pix pela chave: ${request.chavePix}.")
                val chavePix = chavePixRepository.findByChave(request.chavePix)
                val dadosConta = getDadosContaCliente(chavePix!!.clientId, chavePix.tipoConta)
                mapToConsultarChavePixPorClienteIdEPixIdResponse(chavePix, dadosConta)
            } else {
                logger.info("Consultando chaves pix: ${request.chavePix} no BCB.")
                val chavePixResponse = bcbIntegration.consultarChavePix(request.chavePix).body()
                mapToConsultarChavePixPorClienteIdEPixIdResponse(chavePixResponse)
            }

            responseObserver.onNext(response)
            responseObserver.onCompleted()
        }.onFailure { ex ->
            logger.error("Um erro ocorreu durante o processamento da requisição de consultarChavePix", ex)
            when (ex) {
                is ClienteNaoEncontradoException -> responseObserver.onError(
                    Status.NOT_FOUND.withDescription(ex.message).asRuntimeException()
                )
                else -> responseObserver.onError(Status.INTERNAL.withDescription(ex.message).asRuntimeException())
            }
        }
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

    private fun mapToChavePixResponse(chave: ChavePix) = ChavePixResponse.newBuilder()
        .setPixId(chave.id.toString())
        .setClienteId(chave.clientId)
        .setTipoChave(chave.tipoChave)
        .setValorChave(chave.chave)
        .setTipoConta(chave.tipoConta)
        .setDataCriacao(chave.criadaEm.toGoogleTimestamp())
        .build()

    private fun mapToConsultarChavePixPorClienteIdEPixIdResponse(
        chavePix: ChavePix,
        dadosConta: DadosDaContaResponse
    ) = ConsultarChavePixPorClienteIdEPixIdResponse.newBuilder()
        .setPixId(chavePix.id.toString())
        .setClienteId(chavePix.clientId)
        .setTipoChave(chavePix.tipoChave)
        .setValorChave(chavePix.chave)
        .setNomeTitular(dadosConta.titular.nome)
        .setCpfTitular(dadosConta.titular.cpf)
        .setContaBancaria(
            ContaBancariaResponse.newBuilder()
                .setNomeInstituicao(dadosConta.instituicao.nome)
                .setAgencia(dadosConta.agencia)
                .setNumero(dadosConta.numero)
                .setTipo(dadosConta.tipo)
                .build()
        )
        .setDataCriacao(chavePix.criadaEm.toGoogleTimestamp())
        .build()

    private fun mapToConsultarChavePixPorClienteIdEPixIdResponse(chavePixResponse: PixKeyDetailsResponse) =
        ConsultarChavePixPorClienteIdEPixIdResponse.newBuilder()
//            .setPixId().setClienteId() //os 2 campos são opcionais para a segunda abordagem
            .setTipoChave(TipoChave.valueOf(chavePixResponse.tipoChave.descricao))
            .setValorChave(chavePixResponse.chave)
            .setNomeTitular(chavePixResponse.dono.nome)
            .setCpfTitular(chavePixResponse.dono.cpf)
            .setContaBancaria(
                ContaBancariaResponse.newBuilder()
                    .setNomeInstituicao(chavePixResponse.contaBancaria.ispb) //TODO consultar ISPB da Relação de participantes do STR
                    .setAgencia(chavePixResponse.contaBancaria.agencia)
                    .setNumero(chavePixResponse.contaBancaria.numero)
                    .setTipo(chavePixResponse.contaBancaria.tipo.name)
                    .build()
            )
            .setDataCriacao(chavePixResponse.criadoEm.toGoogleTimestamp())
            .build()

}
