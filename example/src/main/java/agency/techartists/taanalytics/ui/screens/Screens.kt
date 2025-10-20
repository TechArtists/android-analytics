/*
 * Copyright (c) 2025 Tech Artists Agency SRL
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package agency.techartists.taanalytics.ui.screens

import agency.techartists.taanalytics.CallManager
import agency.techartists.taanalytics.CustomViews
import agency.techartists.taanalytics.Route
import agency.techartists.taanalytics.compose.trackViewShow
import agency.techartists.taanalytics.core.TAAnalytics
import agency.techartists.taanalytics.core.TAPermissionType
import agency.techartists.taanalytics.core.trackPermissionButtonTap
import agency.techartists.taanalytics.core.trackPermissionScreenShow
import agency.techartists.taanalytics.data.Contact
import agency.techartists.taanalytics.data.ContactsRepository
import agency.techartists.taanalytics.models.EventAnalyticsModel
import agency.techartists.taanalytics.ui.components.AnalyticsButton
import agency.techartists.taanalytics.ui.components.ContactImage
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

/**
 * Create Account Screen
 */
@Composable
fun CreateAccountScreen(
    analytics: TAAnalytics,
    onAccountCreated: () -> Unit,
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Create An Account",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 40.dp)
        )

        // App Icon
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "App Icon",
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.height(50.dp))

        // Username field
        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                showError = it.isNotEmpty() && it.length < 5
                if (it.isNotEmpty()) {
                    analytics.track(event = EventAnalyticsModel("username_changed"))
                }
            },
            label = { Text("User") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (showError) {
            Text(
                text = "The username should be at least 5 characters long",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Password field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(50.dp))

        Button(
            onClick = {
                if (username.length < 5) {
                    showError = true
                    return@Button
                }
                onAccountCreated()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Create Account", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Text("Already have an account? ")
            TextButton(onClick = { /* Handle sign in */ }) {
                Text("Sign In")
            }
        }
    }
}

/**
 * Contacts Permission Screen
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ContactsPermissionScreen(
    analytics: TAAnalytics,
    onPermissionGranted: () -> Unit,
) {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(
        android.Manifest.permission.READ_CONTACTS
    ) { isGranted ->
        analytics.trackPermissionButtonTap(allowed = isGranted, TAPermissionType.CONTACTS)
        if (isGranted) {
            onPermissionGranted()
        }
    }

    LaunchedEffect(permissionState.status.isGranted) {
        if (permissionState.status.isGranted) {
            onPermissionGranted()
        }
    }

    val viewModel = if (permissionState.status.isGranted) {
        CustomViews.CONTACTS_WITH_PERMISSION
    } else {
        CustomViews.CONTACTS_PERMISSION_NOT_DETERMINED
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .trackViewShow(analytics, viewModel)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "TA Analytics Demo App",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 40.dp)
        )

        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "App Icon",
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(50.dp))

        if (!permissionState.status.isGranted) {
            Text(
                text = "Please allow contacts permission for the app to work 🙏.",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            AnalyticsButton(
                text = "Request Contact Permission",
                analytics = analytics,
                view = viewModel,
                modifier = Modifier.fillMaxWidth()
            ) {
                analytics.trackPermissionScreenShow(TAPermissionType.CONTACTS)
                permissionState.launchPermissionRequest()
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
            ) {
                Text("Open Settings")
            }
        }
    }
}

/**
 * Contacts List Screen
 */
@Composable
fun ContactsListScreen(
    analytics: TAAnalytics,
    navController: NavController,
) {
    val context = LocalContext.current
    val repository = remember { ContactsRepository(context) }
    var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            contacts = repository.getContacts()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .trackViewShow(analytics, CustomViews.CONTACTS_WITH_PERMISSION)
    ) {
        if (contacts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(contacts) { contact ->
                    ContactListItem(
                        contact = contact,
                        onClick = {
                            analytics.track(
                                event = EventAnalyticsModel("contact_tapped"),
                                params = null
                            )
                            navController.navigate(Route.ContactDetail(contact.id).route)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ContactListItem(
    contact: Contact,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ContactImage(contact = contact, size = 56.dp)

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = contact.displayName,
                    style = MaterialTheme.typography.titleMedium
                )
                contact.phoneNumber?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Contact Detail Screen
 */
@Composable
fun ContactDetailScreen(
    analytics: TAAnalytics,
    contactId: String,
) {
    val context = LocalContext.current
    val repository = remember { ContactsRepository(context) }
    var contact by remember { mutableStateOf<Contact?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(contactId) {
        scope.launch {
            contact = repository.getContactById(contactId)
        }
    }

    val viewModel = CustomViews.CONTACT.copy(type = contactId)

    contact?.let { contactData ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .trackViewShow(analytics, viewModel)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ContactImage(contact = contactData, size = 150.dp)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "${contactData.givenName ?: ""} ${contactData.familyName ?: ""}".trim()
                    .ifEmpty { contactData.displayName },
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            contactData.email?.let { email ->
                InfoRow(label = "Email:", value = email)
                Spacer(modifier = Modifier.height(16.dp))
            }

            contactData.phoneNumber?.let { phone ->
                CallButton(
                    label = "Main Number",
                    phoneNumber = phone,
                    analytics = analytics,
                    context = context,
                    viewModel = viewModel
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Outdated number button to demonstrate error tracking
            CallButton(
                label = "Outdated Number",
                phoneNumber = "123",
                analytics = analytics,
                context = context,
                viewModel = viewModel
            )
        }
    } ?: Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.titleMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun CallButton(
    label: String,
    phoneNumber: String,
    analytics: TAAnalytics,
    context: android.content.Context,
    viewModel: agency.techartists.taanalytics.models.ViewAnalyticsModel,
) {
    val callManager = remember { CallManager(analytics, context) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "$label:", style = MaterialTheme.typography.titleMedium)
        AnalyticsButton(
            text = phoneNumber,
            analytics = analytics,
            view = viewModel,
            buttonName = "call"
        ) {
            callManager.callIfAtLeastFourDigits(phoneNumber)
        }
    }
}
