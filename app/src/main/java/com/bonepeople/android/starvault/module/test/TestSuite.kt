package com.bonepeople.android.starvault.module.test

data class TestSuite(
    val name: String,
    val description: String = "",
    val testCaseList: List<TestCase> = emptyList(),
)