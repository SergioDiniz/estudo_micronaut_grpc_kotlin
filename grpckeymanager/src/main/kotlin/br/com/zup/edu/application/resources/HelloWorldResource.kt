package br.com.zup.edu.application.resources


import br.com.zup.edu.HelloRequest
import br.com.zup.edu.HelloResponse
import br.com.zup.edu.HelloWorldServiceGrpc
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
class HelloWorldResource : HelloWorldServiceGrpc.HelloWorldServiceImplBase() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun helloWorld(request: HelloRequest?, responseObserver: StreamObserver<HelloResponse>?){
        logger.info("Say Hello World!")

        val response = HelloResponse.newBuilder()
            .setMessage("Hello World ${request?.name}")
            .build()

        responseObserver?.onNext(response)
        responseObserver?.onCompleted()
    }

}
