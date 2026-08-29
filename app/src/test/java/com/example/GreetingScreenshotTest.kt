package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.FoodState
import com.example.ui.components.FoodStateBadge
import com.example.ui.theme.MealyTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun mealy_badge_screenshot() {
        composeTestRule.setContent {
            MealyTheme {
                FoodStateBadge(state = FoodState.EXPIRING)
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/mealy_badge.png")
    }
}
