package br.com.zup.edu.domain.enums

import br.com.zup.edu.application.exceptions.CampoInvalidoException

private const val TAMANHO_MAXIMO_CHAVE = 77
private const val MENSAGEM_DE_ERRO = "A chave do tipo %s deve respeitar o padrão: %s."

enum class BCBTipoChave(val descricao: String) {
    CPF("CPF") {
        override fun isValida(chave: String): Boolean {
            if(isTamanhoValido(chave) && chave.matches("^[0-9]{11}\$".toRegex())) return true
            throw CampoInvalidoException(MENSAGEM_DE_ERRO.format(CPF.descricao, "12345678901"))
        }
    },
    RANDOM("ALEATORIA") {
        override fun isValida(chave: String): Boolean {
            if(chave.isNullOrEmpty()) return true
            throw CampoInvalidoException(MENSAGEM_DE_ERRO.format(RANDOM.descricao, "STRING VAZIA"))
        }
    },
    EMAIL("EMAIL") {
        override fun isValida(chave: String): Boolean {
            val pattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
            if (isTamanhoValido(chave) && chave.matches(pattern.toRegex())) return true
            throw CampoInvalidoException(MENSAGEM_DE_ERRO.format(EMAIL.descricao, "exemplo@email.com"))
        }
    },
    PHONE("CELULAR") {
        override fun isValida(chave: String): Boolean {
            if(isTamanhoValido(chave) && chave.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())) return true
            throw CampoInvalidoException(MENSAGEM_DE_ERRO.format(PHONE.descricao, "+5585988714077"))
        }
    };

    abstract fun isValida(chave: String): Boolean



    companion object {
        fun comDescricao(descricao: String): BCBTipoChave{
            return BCBTipoChave.values().first { it.descricao == descricao }
        }

        fun isTamanhoValido(chave: String): Boolean{
            if(chave.isNullOrBlank()) throw CampoInvalidoException("O campo chave não pode ser nulo ou vazio.")
            if(chave.length > TAMANHO_MAXIMO_CHAVE)
                throw CampoInvalidoException("Tamanho maximo de $TAMANHO_MAXIMO_CHAVE caracteres excedido para o campo chave.")
            return true
        }
    }

}

