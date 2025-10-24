package com.smartalarm

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
        // Wait for the app to load and verify key components exist
        composeTestRule.waitForIdle()
        
        // Verify the todo input field is available and functional
        composeTestRule.onNodeWithTag("todoInput").performTextInput("Test nightly task")
        composeTestRule.onNodeWithTag("addTodoButton").performClick()
        
        // Verify the todo was added by checking for the text pattern
        composeTestRule.onNodeWithText("• Test nightly task").assertIsDisplayed()
    }
}
