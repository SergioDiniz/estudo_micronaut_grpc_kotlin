package br.com.zup.application.factories

import br.com.zup.edu.HelloWorldServiceGrpc
import br.com.zup.edu.KeyManagerServiceGrpc
import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import javax.inject.Singleton

@Factory
class GrpcClientFactory {

    @Singleton
    fun helloWorldClientStub(@GrpcChannel(value = "hello-world") channel: ManagedChannel) : HelloWorldServiceGrpc.HelloWorldServiceBlockingStub {
        return HelloWorldServiceGrpc.newBlockingStub(channel)
    }

    @Singleton
    fun keyManagerClientStub(@GrpcChannel(value = "key-manager") channel: ManagedChannel) : KeyManagerServiceGrpc.KeyManagerServiceBlockingStub {
        return KeyManagerServiceGrpc.newBlockingStub(channel)
    }

}