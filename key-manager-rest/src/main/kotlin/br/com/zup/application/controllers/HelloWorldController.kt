package br.com.zup.application.controllers

import br.com.zup.application.dtos.HelloWorldResponseDTO
import br.com.zup.edu.HelloRequest
import br.com.zup.edu.HelloWorldServiceGrpc
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import java.util.*
import javax.inject.Inject

@Controller
class HelloWorldController(
    @Inject val grpcClient: HelloWorldServiceGrpc.HelloWorldServiceBlockingStub
) {

    @Get("/api/helloworld{?nome}")
    fun sayHelloWorld(@QueryValue(value = "nome") nome: Optional<String>) : HelloWorldResponseDTO{
        val helloRequest = HelloRequest.newBuilder()
            .setName(nome.get())
            .build()

        val helloResponse = grpcClient.helloWorld(helloRequest)

        return HelloWorldResponseDTO(
            messagem = helloResponse.message
        )
    }

}