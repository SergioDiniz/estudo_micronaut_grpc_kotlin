package br.com.zup.edu.domain.repositories

import br.com.zup.edu.domain.entites.ChavePix
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository : JpaRepository<ChavePix, UUID> {
    fun existsByChave(chave: String?): Boolean
    fun findByChave(chave: String?): ChavePix?
    fun findAllByClientId(clientId: String): List<ChavePix>
}
