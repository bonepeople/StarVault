package com.bonepeople.android.starvault.global.storage

import java.io.IOException

sealed class VaultCodecException(message: String, cause: Throwable? = null) : IOException(message, cause) {
    class InvalidFile(message: String, cause: Throwable? = null) : VaultCodecException(message, cause)

    class Unsupported(message: String) : VaultCodecException(message)

    class ResourceLimitExceeded(val limit: VaultResourceLimit) : VaultCodecException("Vault resource limit exceeded: $limit")
}