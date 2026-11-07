package com.bonepeople.android.starvault.module.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bonepeople.android.base.util.CoroutineExtension.launchOnDefault
import com.bonepeople.android.starvault.global.util.LogUtil
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class TestPageModel : ViewModel() {
    val uiState: MutableStateFlow<TestState> = MutableStateFlow(TestState.Default)

    fun runTest(testSuite: TestSuite) {
        if (uiState.value.running || testSuite.testCaseList.isEmpty()) return
        uiState.value = TestState(running = true)
        viewModelScope.launchOnDefault {
            LogUtil.test.info("[开始] ${testSuite.name}")
            try {
                testSuite.testCaseList.forEach { testCase ->
                    val result = runTestCase(testCase)
                    uiState.update { it.copy(resultList = it.resultList + result) }
                    val status = if (result.passed) "通过" else "失败"
                    val detail = result.detail.takeIf { it.isNotEmpty() }?.let { "，$it" }.orEmpty()
                    LogUtil.test.info("[$status] ${result.name}$detail")
                }
            } finally {
                uiState.update { it.copy(running = false) }
                val resultList = uiState.value.resultList
                val passedCount = resultList.count { it.passed }
                LogUtil.test.info("[完成] ${testSuite.name}：通过 $passedCount / ${resultList.size}")
            }
        }
    }

    private suspend fun runTestCase(testCase: TestCase): TestResult {
        return try {
            TestResult(
                name = testCase.name,
                passed = true,
                detail = testCase.execute(),
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            TestResult(
                name = testCase.name,
                passed = false,
                detail = "${exception.javaClass.simpleName}: ${exception.message.orEmpty()}",
            )
        }
    }
}