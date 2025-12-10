package com.example.healthapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthapp.viewmodel.AuthViewModel
import androidx.compose.ui.platform.LocalContext

@Composable
fun LoginScreen(navController: NavHostController, authViewModel: AuthViewModel = viewModel()) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(authViewModel.errorMessage) {
        val msg = authViewModel.errorMessage
        if (!msg.isNullOrBlank()) snackbarHostState.showSnackbar(msg)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "登录", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("邮箱") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("密码") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
        )

        SnackbarHost(hostState = snackbarHostState)

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                authViewModel.login(username, password) {
                    authViewModel.persistSession(context)
                    navController.navigate("home") {
                        popUpTo("auth/login") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            },
            enabled = !authViewModel.isLoading && username.isNotBlank() && password.isNotBlank(),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (authViewModel.isLoading) "正在登录..." else "登录")
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = { navController.navigate("auth/register") }) {
            Text(text = "没有账号？去注册")
        }
    }
}
