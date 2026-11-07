package com.bonepeople.android.starvault.global.storage

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import java.io.IOException

class VaultRecentMetadataStorage(context: Context) {
    private val preferences: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFERENCE_FILE_NAME,
        Context.MODE_PRIVATE,
    )

    @Synchronized
    fun load(): VaultRecentMetadataInfo? {
        return try {
            if (preferences.getInt(KEY_VERSION, 0) != METADATA_VERSION) return null
            val uri = preferences.getString(KEY_URI, null) ?: return null
            val vaultId = preferences.getString(KEY_VAULT_ID, null) ?: return null
            val displayName = preferences.getString(KEY_DISPLAY_NAME, null) ?: return null
            val revision = preferences.getLong(KEY_REVISION, INVALID_REVISION)
            VaultRecentMetadataInfo(
                uri = Uri.parse(uri),
                vaultId = vaultId,
                displayName = displayName,
                revision = revision,
            )
        } catch (_: ClassCastException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    @Synchronized
    @Throws(IOException::class)
    fun save(metadata: VaultRecentMetadataInfo) {
        val saved = preferences.edit()
            .clear()
            .putInt(KEY_VERSION, METADATA_VERSION)
            .putString(KEY_URI, metadata.uri.toString())
            .putString(KEY_VAULT_ID, metadata.vaultId)
            .putString(KEY_DISPLAY_NAME, metadata.displayName)
            .putLong(KEY_REVISION, metadata.revision)
            .commit()
        if (!saved) throw IOException("Recent vault metadata could not be saved")
    }

    @Synchronized
    @Throws(IOException::class)
    fun clear() {
        if (!preferences.edit().clear().commit()) {
            throw IOException("Recent vault metadata could not be cleared")
        }
    }

    companion object {
        const val PREFERENCE_FILE_NAME = "vault_recent_metadata"
        private const val METADATA_VERSION = 1
        private const val INVALID_REVISION = -1L
        private const val KEY_VERSION = "version"
        private const val KEY_URI = "uri"
        private const val KEY_VAULT_ID = "vault_id"
        private const val KEY_DISPLAY_NAME = "display_name"
        private const val KEY_REVISION = "revision"
    }
}