package com.example.menuannam

import android.database.sqlite.SQLiteConstraintException
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import com.example.menuannam.EMAIL
import com.example.menuannam.TOKEN
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun LogInScreen(
    changeMessage: (String) -> Unit = {},
    networkService: NetworkService,
    navigateToTokenScreen: (String) -> Unit = {},
    navigateToHome: () -> Unit = {}
) {

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val appContext = context.applicationContext

    var token by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        changeMessage("Please, introduce your email to generate a token.")
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        // Token field at the top (head)
        TextField(
            value = token,
            onValueChange = { token = it },
            label = { Text("Token") },
            placeholder = { Text("Enter token") },
            modifier = Modifier.semantics { contentDescription = "Token Input" }.fillMaxWidth()
        )

        // Email field below
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            placeholder = { Text("Enter your email") },
            modifier = Modifier.semantics { contentDescription = "Email Input" }.fillMaxWidth()
        )

        // Generate Token Button
        Button(
            onClick = {
                scope.launch {
                    // The withContext function is your primary tool for seamlessly moving between Dispatchers.IO,
                    // Dispatchers.Default, and Dispatchers.Main within a single coroutine, ensuring background
                    // tasks don't freeze the UI.
                    // Start on Main (Implicitly): Composable functions generally run on the Main thread.
                    // Switch to IO: Use withContext(Dispatchers.IO) { ... } to perform heavy lifting (database, network)
                    // without blocking the UI.
                    // Switch back to Main: After the withContext(Dispatchers.IO) block finishes,
                    // the coroutine automatically resumes on the original Main dispatcher
                    // where you can update your UI state and trigger recomposition.
                    try {
                        val result = withContext(Dispatchers.IO) {
                            networkService.generateToken(email = UserCredential(email))
                        }

                        // If the value of the key "code" in the response's body is 200, then an email
                        // containing the generated token has been successfully sent to the value of the key
                        // "email" in the request's body. Otherwise, the token has not been successfully sent.
                        // UI updates must be on the main thread
                        if (result.code == 200) {
                            changeMessage("Token has been sent to your email. Please check your inbox and enter it above.")
                        } else {
                            changeMessage("Failed to send token: ${result.message}")
                        }
                    } catch (e: Exception) {
                        changeMessage("Unexpected error: ${e.message}")
                    }
                }
            },
            enabled = email.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "GenerateToken"
                }
        ) {
            Text("Generate Token")
        }

        // Login Button - saves email and token to DataStore and navigates to home
        Button(
            onClick = {
                scope.launch {
                    withContext(Dispatchers.IO) {
                        // Prefer ApplicationContext: When you need a Context for operations that do not interact with the UI
                        // (e.g., file operations, database access, accessing resources like strings or drawables),
                        // use the application context.
                        // The application context lives for the lifetime of your app and is safe to use on any thread.
                        appContext.dataStore.edit { preferences ->
                            preferences[EMAIL] = email
                            preferences[TOKEN] = token
                        }
                    }
                    changeMessage("Logged in successfully!")
                    navigateToHome()
                }
            },
            enabled = email.isNotBlank() && token.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "Login"
                }
        ) {
            Text("Login")
        }


    }
}