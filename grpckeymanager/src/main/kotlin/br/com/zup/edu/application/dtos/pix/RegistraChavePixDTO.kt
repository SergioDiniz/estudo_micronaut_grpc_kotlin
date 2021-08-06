package br.com.zup.edu.application.dtos.pix

import br.com.zup.edu.TipoChave
import br.com.zup.edu.TipoConta
import br.com.zup.edu.domain.entites.ChavePix
import br.com.zup.edu.domain.enums.BCBTipoChave
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
data class RegistraChavePixDTO(
    @field:NotNull
    @field:NotBlank
    val clienteId: String,
    @field:NotNull
    @field:NotBlank
    val tipoChave: TipoChave,
    @field:Size(max = 77)
    val chave: String,
    @field:NotNull
    @field:NotBlank
    val tipoConta: TipoConta
) {
    init {
        //Valida valor da chave
        BCBTipoChave.comDescricao(tipoChave.name).isValida(chave)
    }

    fun toDomain(): ChavePix {
        return ChavePix(
            clientId = this.clienteId,
            tipoChave = this.tipoChave,
            chave = this.chave,
            tipoConta = this.tipoConta
        )
    }

}
