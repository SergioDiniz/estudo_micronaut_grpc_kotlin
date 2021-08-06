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
import br.com.zup.edu.application.dtos.erp.DadosDaContaResponse
import br.com.zup.edu.application.dtos.bcb.PixKeyDetailsResponse
import br.com.zup.edu.application.dtos.pix.ChavePixInfoDTO
import br.com.zup.edu.application.exceptions.ChaveNaoPertenceAoClienteException
import br.com.zup.edu.application.exceptions.ChavePixJaCadastradaException
import br.com.zup.edu.application.exceptions.ChavePixNaoCadastradaException
import br.com.zup.edu.application.exceptions.ClienteNaoEncontradoException
import br.com.zup.edu.application.extension.toDTO
import br.com.zup.edu.application.extension.toGoogleTimestamp
import br.com.zup.edu.application.integration.BCBIntegration
import br.com.zup.edu.domain.repositories.ChavePixRepository
import br.com.zup.edu.application.integration.ERPItauIntegration
import br.com.zup.edu.domain.entites.ChavePix
import br.com.zup.edu.domain.enums.BCBTipoChave
import br.com.zup.edu.domain.service.ChavePixService
import com.google.protobuf.Empty
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.http.HttpStatus
import org.slf4j.LoggerFactory
import java.lang.IllegalArgumentException
import java.util.*
import javax.inject.Singleton

@Singleton
class KeyManagerResource(
    private val service: ChavePixService
) : KeyManagerServiceGrpc.KeyManagerServiceImplBase() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun registraChavePix(request: RegistraChavePixRequest, responseObserver: StreamObserver<RegistraChavePixResponse>){
        runCatching {
            val chavePix = service.registraChavePix(request.toDTO())
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
            service.removerChavePix(request.toDTO())
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
            if(request.clienteId.isNullOrBlank())
                throw IllegalArgumentException("Campo clienteId não pode ser nulo ou vazio.")

            val builder = ListarChavePixResponse.newBuilder()

            logger.info("Consultando todos as chaves pix para o cliente ${request.clienteId}")
            service.listarChavePix(request.clienteId).forEach() { chave ->
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
            val chaveInfo = service.consultarChavePixPorClienteIdEPixId(request.toDTO())
            responseObserver.onNext(mapToConsultarChavePixPorClienteIdEPixIdResponse(chaveInfo))
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
            val chaveInfo = service.consultarChavePix(request.chavePix)
            responseObserver.onNext(mapToConsultarChavePixPorClienteIdEPixIdResponse(chaveInfo))
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


    private fun mapToChavePixResponse(chave: ChavePix) = ChavePixResponse.newBuilder()
        .setPixId(chave.id.toString())
        .setClienteId(chave.clientId)
        .setTipoChave(chave.tipoChave)
        .setValorChave(chave.chave)
        .setTipoConta(chave.tipoConta)
        .setDataCriacao(chave.criadaEm.toGoogleTimestamp())
        .build()

    private fun mapToConsultarChavePixPorClienteIdEPixIdResponse(
        chaveInfo: ChavePixInfoDTO
    ) = ConsultarChavePixPorClienteIdEPixIdResponse.newBuilder()
        .setPixId(chaveInfo.pixId)
        .setClienteId(chaveInfo.clienteId)
        .setTipoChave(chaveInfo.tipoChave)
        .setValorChave(chaveInfo.valorChave)
        .setNomeTitular(chaveInfo.titular.nome)
        .setCpfTitular(chaveInfo.titular.cpf)
        .setContaBancaria(
            ContaBancariaResponse.newBuilder()
                .setNomeInstituicao(chaveInfo.contaBancaria.nomeInstituicao)
                .setAgencia(chaveInfo.contaBancaria.agencia)
                .setNumero(chaveInfo.contaBancaria.numero)
                .setTipo(chaveInfo.contaBancaria.tipo)
                .build()
        )
        .setDataCriacao(chaveInfo.dataCriacao.toGoogleTimestamp())
        .build()

}
