package br.com.zup.edu.application.exceptions

import java.lang.RuntimeException

class ClienteNaoEncontradoException(val mensagem: String) : RuntimeException(mensagem)