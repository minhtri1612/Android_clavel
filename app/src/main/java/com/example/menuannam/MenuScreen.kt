package com.example.menuannam

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuAnNam(
    onStudy: () -> Unit,
    onAdd: () -> Unit,
    onSearch: () -> Unit,
    changeMessage: (String) -> Unit = {},
    onLogIn: () -> Unit
){
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val appContext = context.applicationContext

    LaunchedEffect(Unit) {
        // Then, use the DataStore.data property to expose the appropriate stored value using a Flow.
        // In coroutines, a flow is a type that can emit multiple values sequentially,
        // as opposed to suspend functions that return only a single value.
        // For example, you can use a flow to receive live updates from a database.
        // Flows are built on top of coroutines and can provide multiple values.
        // A flow is conceptually a stream of data that can be computed asynchronously.
        // The emitted values must be of the same type. For example, a Flow<Int>
        // is a flow that emits integer values.
        // In Kotlin with Jetpack DataStore, the Flow<Preferences> returned by dataStore.data
        // emits every time any single preference within the DataStore file changes.
        // The flow emits the entire Preferences object, containing all current key-value pairs, with each change.
        // In Kotlin Flow, the first() terminal operator is used to collect only the initial value emitted
        // by a flow and then automatically cancel the flow's execution.
        // This is particularly useful in Jetpack Compose and other Android development scenarios
        // where you only need a single, immediate result from a potentially long-running data stream.
        withContext(Dispatchers.IO) {
            val preferencesFlow: Flow<Preferences> = appContext.dataStore.data
            val preferences = preferencesFlow.first()
            val emailValue = preferences[EMAIL] ?: ""
            withContext(Dispatchers.Main) {
                changeMessage(emailValue)
            }
        }
    }

    Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),

            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally)
        {
            Button(onClick = {onStudy()}, modifier = Modifier.semantics{ contentDescription= "navigationToStudyScreen" }.size(width = 100.dp, height = 80.dp)) {
                Text("Study")
            }
            Button(onClick = {onAdd()}, modifier = Modifier.semantics{ contentDescription= "navigationToAddScreen" }.size(width = 100.dp, height = 80.dp)) {
                Text("Add")
            }
            Button(onClick = {onSearch()}, modifier = Modifier.semantics{ contentDescription= "navigationToSearchScreen" }.size(width = 100.dp, height = 80.dp)) {
                Text("Search")
            }
            Button(onClick = {onLogIn()}, modifier = Modifier.semantics{ contentDescription= "navigationToLogInScreen" }.size(width = 100.dp, height = 80.dp)) {
                Text("Log In")
            }
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "ExecuteLogout" },
                onClick = {
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            appContext.dataStore.edit { preferences ->
                                preferences.remove(EMAIL)
                                preferences.remove(TOKEN)
                            }
                            val preferencesFlow: Flow<Preferences> = appContext.dataStore.data
                            val preferences = preferencesFlow.first()
                            val emailValue = preferences[EMAIL] ?: ""
                            withContext(Dispatchers.Main) {
                                changeMessage(emailValue)
                            }
                        }
                    }
                }
            ) {
                Text(
                    "Log out",
                    modifier = Modifier.semantics { contentDescription = "Logout" }
                )
            }
        }
    }
