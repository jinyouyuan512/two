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
fun RegisterScreen(navController: NavHostController, authViewModel: AuthViewModel = viewModel()) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
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
        Text(text = "注册", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = { Text("昵称") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(12.dp))

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
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = confirm,
            onValueChange = { confirm = it },
            label = { Text("确认密码") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
        )

        SnackbarHost(hostState = snackbarHostState)

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                authViewModel.register(username, password, confirm, displayName) {
                    authViewModel.persistSession(context)
                    navController.navigate("auth/login") {
                        popUpTo("auth/register") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            },
            enabled = !authViewModel.isLoading && username.isNotBlank() && password.isNotBlank() && confirm.isNotBlank() && displayName.isNotBlank(),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (authViewModel.isLoading) "正在注册..." else "注册")
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = { navController.popBackStack() }) {
            Text(text = "已有账号？返回登录")
        }
    }
}
