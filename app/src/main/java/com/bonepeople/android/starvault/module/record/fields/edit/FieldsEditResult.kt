package com.bonepeople.android.starvault.module.record.fields.edit

import com.bonepeople.android.starvault.global.data.VaultFieldInfo
import com.google.gson.annotations.SerializedName

data class FieldsEditResult(
    @SerializedName("type")
    val type: String = "",
    @SerializedName("savedField")
    val savedField: VaultFieldInfo = VaultFieldInfo(),
    @SerializedName("deletedFieldId")
    val deletedFieldId: String = "",
) {
    companion object {
        const val TYPE_SAVED = "saved"
        const val TYPE_DELETED = "deleted"
    }
}