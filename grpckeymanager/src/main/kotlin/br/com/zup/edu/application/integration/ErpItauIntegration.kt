package br.com.zup.edu.application.integration

import br.com.zup.edu.application.dtos.erp.DadosDaContaResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client(value = "\${erp.itau.url}")
interface ERPItauIntegration {
    @Get(value = "/api/v1/clientes/{clientId}/contas{?tipo}")
    fun buscaContasClientePorTipo(@PathVariable clientId: String, @QueryValue tipo: String): HttpResponse<DadosDaContaResponse>
}
