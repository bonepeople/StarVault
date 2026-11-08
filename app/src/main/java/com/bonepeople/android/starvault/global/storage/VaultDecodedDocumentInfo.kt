package com.bonepeople.android.starvault.global.storage

internal data class VaultDecodedDocumentInfo(
    val header: VaultFileHeaderInfo,
    val payload: VaultPayloadInfo,
)