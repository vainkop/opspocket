package com.vainkop.opspocket

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class NavigationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun homeScreen_displaysTitle() {
        composeRule.onNodeWithText("OpsPocket").assertIsDisplayed()
    }

    @Test
    fun homeScreen_displaysCastAiButton() {
        composeRule.onNodeWithText("Manage Cast.ai").assertIsDisplayed()
    }

    @Test
    fun homeScreen_displaysAzureButton() {
        composeRule.onNodeWithText("Manage Azure").assertIsDisplayed()
    }

    @Test
    fun homeScreen_castAiButton_navigatesToApiKey() {
        composeRule.onNodeWithText("Manage Cast.ai").performClick()
        composeRule.onNodeWithText("Connect to Cast.ai").assertIsDisplayed()
    }

    @Test
    fun homeScreen_azureButton_navigatesToAzureAuth() {
        composeRule.onNodeWithText("Manage Azure").performClick()
        composeRule.onNodeWithText("Azure Sign-In").assertIsDisplayed()
    }
}
