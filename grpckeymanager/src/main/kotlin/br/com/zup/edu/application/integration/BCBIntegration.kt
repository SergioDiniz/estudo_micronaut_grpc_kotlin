package br.com.zup.edu.application.integration

import br.com.zup.edu.application.dtos.CreatePixKeyRequest
import br.com.zup.edu.application.dtos.CreatePixKeyResponse
import br.com.zup.edu.application.dtos.DeletePixKeyRequest
import br.com.zup.edu.application.dtos.DeletePixKeyResponse
import io.micronaut.http.HttpHeaders.ACCEPT
import io.micronaut.http.HttpHeaders.CONTENT_TYPE
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.Headers
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client

@Client(value = "\${bcb.external.url}")
@Headers(
    Header(name = ACCEPT, value = "application/xml;charset=UTF-8"),
    Header(name = CONTENT_TYPE, value = "application/xml;charset=UTF-8")
)
interface BCBIntegration {
    @Post(value = "/api/v1/pix/keys" )
    fun cadastrarChavePix(@Body createPixKeyRequest: CreatePixKeyRequest): HttpResponse<CreatePixKeyResponse>

    @Delete(value = "/api/v1/pix/keys/{key}" )
    fun deletarChavePix(@PathVariable(value = "key") chave: String, @Body deletePixKeyRequest: DeletePixKeyRequest): HttpResponse<DeletePixKeyResponse>
}