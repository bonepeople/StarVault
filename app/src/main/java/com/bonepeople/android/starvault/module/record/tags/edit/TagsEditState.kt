package com.bonepeople.android.starvault.module.record.tags.edit

import com.bonepeople.android.starvault.global.data.VaultTagDefinitionInfo
import com.google.gson.annotations.SerializedName

data class TagsEditState(
    @SerializedName("tags")
    val tags: List<VaultTagDefinitionInfo> = emptyList(),
    @SerializedName("pendingUndo")
    val pendingUndo: PendingUndoInfo? = null,
    @SerializedName("recommendedTags")
    val recommendedTags: List<VaultTagDefinitionInfo> = emptyList(),
) {
    @Suppress("unused")
    object Preview {
        val Normal = TagsEditState(
            tags = listOf(
                VaultTagDefinitionInfo(id = "tag-dev", name = "开发"),
                VaultTagDefinitionInfo(id = "tag-work", name = "工作"),
            ),
            recommendedTags = listOf(
                VaultTagDefinitionInfo(id = "tag-home", name = "家庭"),
                VaultTagDefinitionInfo(id = "tag-network", name = "网络"),
            ),
        )

        val Empty = TagsEditState()

        val LongName = TagsEditState(
            tags = listOf(VaultTagDefinitionInfo(id = "tag-long", name = "这是一个很长很长很长很长很长很长很长很长的标签名称")),
        )
    }

    companion object {
        val Default = TagsEditState()
    }
}