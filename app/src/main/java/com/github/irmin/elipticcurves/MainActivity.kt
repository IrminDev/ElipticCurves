package com.github.irmin.elipticcurves

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.irmin.elipticcurves.ui.navigation.Routes
import com.github.irmin.elipticcurves.ui.screens.EllipticCurveScreen
import com.github.irmin.elipticcurves.ui.screens.MenuScreen
import com.github.irmin.elipticcurves.ui.screens.PointMultiplicationScreen
import com.github.irmin.elipticcurves.ui.screens.PointSumScreen
import com.github.irmin.elipticcurves.ui.theme.ElipticCurvesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ElipticCurvesTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = Routes.MENU) {
                    composable(Routes.MENU) {
                        MenuScreen(
                            onNavigateToCurvePoints = { navController.navigate(Routes.CURVE_POINTS) },
                            onNavigateToPointSum = { navController.navigate(Routes.POINT_SUM) },
                            onNavigateToMultiplication = { navController.navigate(Routes.POINT_MUL) }
                        )
                    }
                    composable(Routes.CURVE_POINTS) {
                        EllipticCurveScreen(onBack = { navController.popBackStack() })
                    }
                    composable(Routes.POINT_SUM) {
                        PointSumScreen(onBack = { navController.popBackStack() })
                    }
                    composable(Routes.POINT_MUL) {
                        PointMultiplicationScreen(onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}