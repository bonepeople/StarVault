package com.bonepeople.android.starvault.global.storage

import java.io.IOException

sealed class VaultCodecException(message: String, cause: Throwable? = null) : IOException(message, cause) {
    class InvalidFile(message: String, cause: Throwable? = null) : VaultCodecException(message, cause)

    class AuthenticationFailed(cause: Throwable? = null) : VaultCodecException("Vault authentication failed", cause)

    class Unsupported(message: String, cause: Throwable? = null) : VaultCodecException(message, cause)

    class ResourceLimitExceeded(val limit: VaultResourceLimit) : VaultCodecException("Vault resource limit exceeded: $limit")
}