package com.bonepeople.android.starvault.global.storage

import java.io.IOException

sealed class VaultStorageException(
    val stage: VaultOperationStage,
    message: String,
    cause: Throwable? = null,
) : IOException(message, cause) {
    class InvalidFile(stage: VaultOperationStage, cause: Throwable? = null) :
        VaultStorageException(stage, "Vault file is invalid", cause)

    class AuthenticationFailed(stage: VaultOperationStage, cause: Throwable? = null) :
        VaultStorageException(stage, "Vault authentication failed", cause)

    class Unsupported(stage: VaultOperationStage, cause: Throwable? = null) :
        VaultStorageException(stage, "Vault format is unsupported", cause)

    class ResourceLimitExceeded(
        stage: VaultOperationStage,
        val limit: VaultResourceLimit,
        cause: Throwable? = null,
    ) : VaultStorageException(stage, "Vault resource limit exceeded: $limit", cause)

    class IoFailure(
        stage: VaultOperationStage,
        cause: Throwable? = null,
        val recoveryInfo: VaultRecoveryInfo? = null,
    ) :
        VaultStorageException(stage, "Vault storage I/O failed", cause)
}