package com.reactnativesecurekeystore.exception

open class CustomException(val code: ErrorCode, override val message: String) :
    RuntimeException(message)
