package br.com.zup.application.controllers

import br.com.zup.application.dtos.ChavePixResponseDTO
import br.com.zup.application.dtos.ConsultarChavePixPorClienteIdEPixIdResponseDTO
import br.com.zup.application.dtos.ContaBancariaResponseDTO
import br.com.zup.application.dtos.RegistraChavePixRequestDTO
import br.com.zup.application.dtos.RegistraChavePixResponseDTO
import br.com.zup.application.extension.toDTO
import br.com.zup.application.extension.toLocalDateTime
import br.com.zup.edu.ConsultarChavePixPorClienteIdEPixIdRequest
import br.com.zup.edu.KeyManagerServiceGrpc
import br.com.zup.edu.ListarChavePixRequest
import br.com.zup.edu.RegistraChavePixRequest
import br.com.zup.edu.RemoverChavePixRequest
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import javax.inject.Inject

@Controller
class KeyManagerController(
    @Inject val keyManagerClient: KeyManagerServiceGrpc.KeyManagerServiceBlockingStub
) {

    @Post("/api/v1/clientes/{clienteId}/chaves")
    fun registraChavePix(@PathVariable(value = "clienteId") clienteId: String,
                         @Body requestBody: RegistraChavePixRequestDTO) : HttpResponse<RegistraChavePixResponseDTO> {

        try{
            val registraChavePixRequest = RegistraChavePixRequest.newBuilder()
                .setCodigoCliente(clienteId)
                .setTipoChave(requestBody.tipoChave)
                .setValorChave(requestBody.valorChave)
                .setTipoConta(requestBody.tipoConta)
                .build()

            val registraChavePixResponse = keyManagerClient.registraChavePix(registraChavePixRequest)

            return HttpResponse.created(
                RegistraChavePixResponseDTO(pixId = registraChavePixResponse.pixId)
            )
        } catch (ex: StatusRuntimeException){
            if (ex.status.code == Status.Code.ALREADY_EXISTS){
                return HttpResponse.unprocessableEntity()
            }
        }

        return HttpResponse.serverError()
    }

    @Delete("/api/v1/clientes/{clienteId}/chaves/{pixId}")
    fun removerChavePix(@PathVariable(value = "clienteId") clienteId: String,
                        @PathVariable(value = "pixId") pixId: String): HttpResponse<Any>{
        try {
            keyManagerClient.removerChavePix(
                RemoverChavePixRequest.newBuilder()
                    .setPixId(pixId)
                    .setCodigoCliente(clienteId)
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
            return HttpResponse.ok(consultaChaveResponse.toDTO())
        }catch (ex: StatusRuntimeException){
            if (ex.status.code == Status.Code.NOT_FOUND){
                return HttpResponse.notFound()
            }
        }

        return HttpResponse.serverError()
    }


    @Get(value = "/api/v1/clientes/{clienteId}/chaves")
    fun listarChavePix(@PathVariable(value = "clienteId") clienteId: String) : HttpResponse<List<ChavePixResponseDTO>>{
        try {
            val listarChavePixResponse = keyManagerClient.listarChavePix(
                ListarChavePixRequest.newBuilder()
                    .setClienteId(clienteId)
                    .build()
            )

            val response = mutableListOf<ChavePixResponseDTO>()
            listarChavePixResponse.chavesPixList.map { chave ->
                response.add(chave.toDTO())
            }

            return HttpResponse.ok(response)
        } catch (ex: Throwable){
            return HttpResponse.serverError()
        }
    }
}