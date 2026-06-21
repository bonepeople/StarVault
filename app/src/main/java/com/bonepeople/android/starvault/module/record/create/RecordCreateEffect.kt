package com.bonepeople.android.starvault.module.record.create

sealed interface RecordCreateEffect {
    data class OpenDetail(val recordId: String) : RecordCreateEffect
}