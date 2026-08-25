package com.example.devappaccesibilidad

import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.devappaccesibilidad.ui.screens.PantallaInicio
import com.example.devappaccesibilidad.ui.screens.PantallaLogin
import com.example.devappaccesibilidad.ui.screens.PantallaRecuperarContrasena
import com.example.devappaccesibilidad.ui.screens.PantallaRegistro
import com.example.devappaccesibilidad.ui.theme.DevAppAccesibilidadTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DevAppAccesibilidadTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "login"
                    ) {
                        composable("login") {
                            PantallaLogin(
                                alIniciarSesion = { email ->
                                    navController.navigate("home/$email") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                alIrARegistro = { navController.navigate("registro") },
                                alIrARecuperarContrasena = { navController.navigate("recuperar") }
                            )
                        }
                        composable("registro") {
                            PantallaRegistro(
                                alVolver = { navController.popBackStack() }
                            )
                        }
                        composable("recuperar") {
                            PantallaRecuperarContrasena(
                                alVolver = { navController.popBackStack() }
                            )
                        }
                        composable("home/{email}") { backStackEntry ->
                            val email = backStackEntry.arguments?.getString("email") ?: ""
                            PantallaInicio(
                                email = email,
                                alCerrarSesion = {
                                    navController.navigate("login") {
                                        popUpTo("home/{email}") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}