package br.com.zup.application.controllers

import br.com.zup.application.dtos.ConsultarChavePixPorClienteIdEPixIdResponseDTO
import br.com.zup.application.dtos.ContaBancariaResponseDTO
import br.com.zup.application.dtos.RegistraChavePixRequestDTO
import br.com.zup.application.dtos.RegistraChavePixResponseDTO
import br.com.zup.application.dtos.RemoverChavePixRequestDTO
import br.com.zup.application.extension.toLocalDateTime
import br.com.zup.edu.ConsultarChavePixPorClienteIdEPixIdRequest
import br.com.zup.edu.KeyManagerServiceGrpc
import br.com.zup.edu.RegistraChavePixRequest
import br.com.zup.edu.RemoverChavePixRequest
import com.google.protobuf.Timestamp
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

@Controller
class KeyManagerController(
    @Inject val keyManagerClient: KeyManagerServiceGrpc.KeyManagerServiceBlockingStub
) {

    @Post("/api/v1/pix/chaves")
    fun registraChavePix(@Body requestBody: RegistraChavePixRequestDTO) : HttpResponse<RegistraChavePixResponseDTO> {

        val registraChavePixRequest = RegistraChavePixRequest.newBuilder()
            .setCodigoCliente(requestBody.codigoCliente)
            .setTipoChave(requestBody.tipoChave)
            .setValorChave(requestBody.valorChave)
            .setTipoConta(requestBody.tipoConta)
            .build()

        val registraChavePixResponse = keyManagerClient.registraChavePix(registraChavePixRequest)

        return HttpResponse.created(
            RegistraChavePixResponseDTO(pixId = registraChavePixResponse.pixId)
        )

    }

    @Delete("/api/v1/pix/chaves/{id}")
    fun removerChavePix(@PathVariable(value = "id") pixId: String,
                        @Body requestBody: RemoverChavePixRequestDTO): HttpResponse<Any>{
        try {
            keyManagerClient.removerChavePix(
                RemoverChavePixRequest.newBuilder()
                    .setPixId(pixId)
                    .setCodigoCliente(requestBody.codigoCliente)
                    .build()
            )

            return HttpResponse.accepted()
        }catch (ex: StatusRuntimeException){
            if (ex.status.code == Status.Code.NOT_FOUND){
                return HttpResponse.notFound()
            }
        }

        return HttpResponse.serverError()
    }

    @Get(value = "/api/v1/clientes/{clienteId}/chaves/{pixId}")
    fun consultarChavePixPorClienteIdEPixId(@PathVariable(value = "clienteId") clienteId: String,
                                            @PathVariable(value = "pixId") pixId: String): HttpResponse<ConsultarChavePixPorClienteIdEPixIdResponseDTO>{

        try {
            val consultaChaveResponse = keyManagerClient.consultarChavePixPorClienteIdEPixId(
                ConsultarChavePixPorClienteIdEPixIdRequest.newBuilder()
                    .setPixId(pixId)
                    .setClienteId(clienteId)
                    .build()
            )

            val reponse = ConsultarChavePixPorClienteIdEPixIdResponseDTO(
                pixId = consultaChaveResponse.pixId,
                clienteId = consultaChaveResponse.clienteId,
                tipoChave = consultaChaveResponse.tipoChave,
                valorChave = consultaChaveResponse.valorChave,
                nomeTitular = consultaChaveResponse.nomeTitular,
                cpfTitular = consultaChaveResponse.cpfTitular,
                contaBancaria = ContaBancariaResponseDTO(
                    nomeInstituicao = consultaChaveResponse.contaBancaria.nomeInstituicao,
                    agencia = consultaChaveResponse.contaBancaria.agencia,
                    numero = consultaChaveResponse.contaBancaria.numero,
                    tipo = consultaChaveResponse.contaBancaria.tipo
                ),
                dataCriacao = consultaChaveResponse.dataCriacao.toLocalDateTime()
            )

            return HttpResponse.ok(reponse)
        }catch (ex: StatusRuntimeException){
            if (ex.status.code == Status.Code.NOT_FOUND){
                return HttpResponse.notFound()
            }
        }

        return HttpResponse.serverError()

    }


    //listarChavePix
}