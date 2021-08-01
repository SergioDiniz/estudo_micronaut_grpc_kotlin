package br.com.zup.edu.domain.entites

import br.com.zup.edu.TipoChave
import br.com.zup.edu.TipoConta
import org.hibernate.annotations.GenericGenerator
import java.time.LocalDateTime
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table


@Entity
@Table( name = "chave_pix")
data class ChavePix(
    @Column(nullable = false)
    val clientId: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipoChave: TipoChave,

    @Column(nullable = false, length = 77, unique = true)
    var chave: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipoConta: TipoConta,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", columnDefinition = "UUID", updatable = false, nullable = false)
    val id: UUID? = null

    @Column(nullable = false)
    val criadaEm: LocalDateTime = LocalDateTime.now()
}
