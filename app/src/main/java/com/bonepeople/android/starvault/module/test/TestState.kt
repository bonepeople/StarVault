package com.bonepeople.android.starvault.module.test

data class TestState(
    val running: Boolean = false,
    val resultList: List<TestResult> = emptyList(),
) {
    companion object {
        val Default = TestState()
    }
}