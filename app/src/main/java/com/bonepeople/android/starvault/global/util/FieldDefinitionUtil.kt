package com.bonepeople.android.starvault.global.util

import java.util.Locale

object FieldDefinitionUtil {
    fun normalizeName(name: String): String {
        return name.trim().lowercase(Locale.ROOT)
    }
}