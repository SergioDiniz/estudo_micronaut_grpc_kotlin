package br.com.zup.edu.application.resources

import br.com.zup.edu.ChavePixResponse
import br.com.zup.edu.KeyManagerServiceGrpc
import br.com.zup.edu.ListarChavePixRequest
import br.com.zup.edu.ListarChavePixResponse
import br.com.zup.edu.RegistraChavePixRequest
import br.com.zup.edu.RegistraChavePixResponse
import br.com.zup.edu.RemoverChavePixRequest
import br.com.zup.edu.TipoConta
import br.com.zup.edu.application.dtos.DadosDaContaResponse
import br.com.zup.edu.application.exceptions.ChaveNaoPertenceAoClienteException
import br.com.zup.edu.application.exceptions.ChavePixJaCadastradaException
import br.com.zup.edu.application.exceptions.ChavePixNaoCadastradaException
import br.com.zup.edu.application.exceptions.ClienteNaoEncontradoException
import br.com.zup.edu.application.extension.toDomain
import br.com.zup.edu.application.extension.toGoogleTimestamp
import br.com.zup.edu.domain.repositories.ChavePixRepository
import br.com.zup.edu.application.integration.ErpItauIntegration
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
    private val erpItauIntegration: ErpItauIntegration
) : KeyManagerServiceGrpc.KeyManagerServiceImplBase() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun registraChavePix(request: RegistraChavePixRequest, responseObserver: StreamObserver<RegistraChavePixResponse>){
        runCatching {
            logger.info("Validando se chave: ${request.valorChave} ja estar cadastrada.")
            if(chavePixRepository.existsByChave(request.valorChave)) throw ChavePixJaCadastradaException()

            val dadosConta = getDadosContaCliente(request.codigoCliente, request.tipoConta)
            logger.info("Conta localizada com sucesso no ERP Itau: $dadosConta")

            val chavePix = chavePixRepository.save(request.toDomain())

            val response = RegistraChavePixResponse.newBuilder()
                .setPixId(chavePix.id.toString())
                .build()

            responseObserver.onNext(response)
            responseObserver.onCompleted()
        }.onFailure { ex ->
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

            logger.info("Deletando PixId ${request.pixId}.")
            chavePixRepository.delete(chavePix.get())

            responseObserver.onNext(Empty.newBuilder().build())
            responseObserver.onCompleted()
        }.onFailure { ex ->
            if(ex is ChavePixNaoCadastradaException) responseObserver.onError(Status.NOT_FOUND.asRuntimeException())
            else responseObserver.onError(Status.INTERNAL.withDescription(ex.message).asRuntimeException())
        }
    }

    override fun listarChavePix(request: ListarChavePixRequest, responseObserver: StreamObserver<ListarChavePixResponse>){
        runCatching {
            val builder = ListarChavePixResponse.newBuilder()

            logger.info("Consultando todos as chaves pix para o cliente ${request.clienteId}")
            chavePixRepository.findAllByClientId(request.clienteId).map { chave ->
                builder.addChavesPix(
                    ChavePixResponse.newBuilder()
                        .setPixId(chave.id.toString())
                        .setClienteId(chave.clientId)
                        .setTipoChave(chave.tipoChave)
                        .setValorChave(chave.chave)
                        .setTipoConta(chave.tipoConta)
                        .setDataCriacao(chave.criadaEm.toGoogleTimestamp())
                        .build()
                )
            }
            
            responseObserver.onNext(builder.build())
            responseObserver.onCompleted()
        }.onFailure { ex ->
            responseObserver.onError(Status.INTERNAL.withDescription(ex.message).asRuntimeException())
        }
    }


    private fun getDadosContaCliente(codigoCliente: String, tipoConta: TipoConta): DadosDaContaResponse{
        logger.info("Consultando conta do cliente: $codigoCliente no ERP Itau.")
        val erpResponse = erpItauIntegration.buscaContasClientePorTipo(codigoCliente, tipoConta.name)

        if(erpResponse.status != HttpStatus.OK) {
            throw ClienteNaoEncontradoException("Não foi possivel encontrar um cliente valido " +
                    "para o ID $codigoCliente e tipo da conta: $tipoConta.")
        }

        return erpResponse.body()
    }

}
