package com.bonepeople.android.starvault.module.test

class TestCase(
    val name: String,
    val execute: suspend () -> String = { "" },
)