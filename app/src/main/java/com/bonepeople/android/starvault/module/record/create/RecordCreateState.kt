package com.bonepeople.android.starvault.module.record.create

import com.bonepeople.android.starvault.global.data.VaultTagDefinitionInfo
import com.google.gson.annotations.SerializedName

data class RecordCreateState(
    @SerializedName("title")
    val title: String = "",
    @SerializedName("tags")
    val tags: List<VaultTagDefinitionInfo> = emptyList(),
    @SerializedName("titleError")
    val titleError: String = "",
    @SerializedName("saving")
    val saving: Boolean = false,
) {
    @Suppress("unused")
    object Preview {
        val Normal = RecordCreateState(
            title = "GitHub",
            tags = listOf(
                VaultTagDefinitionInfo(id = "tag-dev", name = "开发"),
                VaultTagDefinitionInfo(id = "tag-work", name = "工作"),
            ),
        )

        val Empty = RecordCreateState()

        val LongName = RecordCreateState(
            title = "超长标签",
            tags = listOf(VaultTagDefinitionInfo(id = "tag-long", name = "这是一个很长很长很长很长很长很长很长很长的标签名称")),
        )

        val TitleError = RecordCreateState(
            title = "   ",
            titleError = "请输入名称",
        )
    }

    companion object {
        val Default = RecordCreateState()
    }
}