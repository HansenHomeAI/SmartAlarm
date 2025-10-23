package com.smartalarm

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class NavigationSmokeTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun verifyNavigationBetweenMajorScreens() {
        composeTestRule.onNodeWithText("SmartAlarm", substring = false).assertIsDisplayed()

        composeTestRule.onNodeWithText("Set Alarm", substring = false).performClick()
        composeTestRule.onNodeWithText("Schedule Alarm", substring = false).assertIsDisplayed()

        composeTestRule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        composeTestRule.onNodeWithText("SmartAlarm", substring = false).assertIsDisplayed()

        composeTestRule.onNodeWithText("Settings", substring = false).performClick()
        composeTestRule.onNodeWithText("Settings", substring = false).assertIsDisplayed()
    }
}
