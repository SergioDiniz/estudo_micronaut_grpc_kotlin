package br.com.zup.application.extension

import com.google.protobuf.Timestamp
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun Timestamp.toLocalDateTime(): LocalDateTime {
    return Instant.ofEpochSecond(
        this.seconds,
        this.nanos.toLong()
    )
    .atZone(ZoneId.of("UTC"))
    .toLocalDateTime()
}