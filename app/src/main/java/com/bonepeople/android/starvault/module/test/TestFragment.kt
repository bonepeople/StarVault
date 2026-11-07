package com.bonepeople.android.starvault.module.test

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bonepeople.android.starvault.global.base.BaseFragment

class TestFragment : BaseFragment() {
    private val viewModel: TestPageModel by viewModels()
    private val testSuite = TestSuite(
        name = "测试页面",
        description = "当前没有待执行的测试。后续测试可作为测试项添加到此页面。",
    )

    @Composable
    override fun Content() {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle(viewLifecycleOwner.lifecycle)
        ComposeContent(
            testSuite = testSuite,
            uiState = uiState,
            startTest = { viewModel.runTest(testSuite) },
        )
    }

    @Composable
    private fun ComposeContent(
        testSuite: TestSuite,
        uiState: TestState,
        startTest: () -> Unit,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Text(
                text = testSuite.name,
                style = MaterialTheme.typography.headlineSmall,
            )
            if (testSuite.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = testSuite.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = startTest,
                enabled = !uiState.running && testSuite.testCaseList.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (uiState.running) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                }
                Text(
                    text = when {
                        uiState.running -> "测试中"
                        testSuite.testCaseList.isEmpty() -> "暂无测试项目"
                        else -> "开始测试"
                    },
                )
            }
            if (uiState.resultList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                val passedCount = uiState.resultList.count { it.passed }
                Text(
                    text = "通过 $passedCount / ${uiState.resultList.size}",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (passedCount == uiState.resultList.size) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(items = uiState.resultList) { result ->
                    ResultItem(result)
                }
            }
        }
    }

    @Composable
    private fun ResultItem(result: TestResult) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = if (result.passed) "通过" else "失败",
                style = MaterialTheme.typography.labelLarge,
                color = if (result.passed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.name,
                    style = MaterialTheme.typography.bodyLarge,
                )
                if (result.detail.isNotEmpty()) {
                    Text(
                        text = result.detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}