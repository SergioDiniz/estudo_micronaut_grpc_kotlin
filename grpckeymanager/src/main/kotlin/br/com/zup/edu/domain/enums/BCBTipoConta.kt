package br.com.zup.edu.domain.enums

import br.com.zup.edu.TipoConta

enum class BCBTipoConta(val descricao: String) {
    CACC(TipoConta.CONTA_CORRENTE.name),
    SVGS(TipoConta.CONTA_POUPANCA.name);

    companion object {
        fun comDescricao(descricao: String): BCBTipoConta{
            return BCBTipoConta.values().first { it.descricao == descricao }
        }
    }

}
