package com.reactnativesecurekeystore.exception

class KeyInvalidatedException(message: String): CustomException(ErrorCode.INVALIDATED_KEY_ERROR, message)
