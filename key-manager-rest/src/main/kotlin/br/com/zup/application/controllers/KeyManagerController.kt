package br.com.zup.application.controllers

import br.com.zup.application.dtos.RegistraChavePixRequestDTO
import br.com.zup.application.dtos.RegistraChavePixResponseDTO
import br.com.zup.edu.KeyManagerServiceGrpc
import br.com.zup.edu.RegistraChavePixRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
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

    //removerChavePix

    //consultarChavePixPorClienteIdEPixId

    //listarChavePix
}