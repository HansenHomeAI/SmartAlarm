package com.smartalarm

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class NavigationSmokeTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun verifyMainLayoutAndTodoInteraction() {
        composeTestRule.onNodeWithText("To-Do List", substring = false).assertIsDisplayed()
        composeTestRule.onNodeWithText("Alarm setup", substring = true).assertExists()

        val sampleTodo = "Test nightly task"
        composeTestRule.onNodeWithTag("todoInput").performTextInput(sampleTodo)
        composeTestRule.onNodeWithTag("addTodoButton").performClick()
        composeTestRule.onNodeWithText("• $sampleTodo").assertExists()
    }
}
