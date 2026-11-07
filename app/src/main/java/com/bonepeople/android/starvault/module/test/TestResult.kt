package com.bonepeople.android.starvault.module.test

data class TestResult(
    val name: String,
    val passed: Boolean,
    val detail: String = "",
)