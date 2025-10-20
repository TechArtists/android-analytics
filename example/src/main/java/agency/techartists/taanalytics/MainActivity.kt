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

package agency.techartists.taanalytics

import agency.techartists.taanalytics.ui.screens.ContactDetailScreen
import agency.techartists.taanalytics.ui.screens.ContactsListScreen
import agency.techartists.taanalytics.ui.screens.ContactsPermissionScreen
import agency.techartists.taanalytics.ui.screens.CreateAccountScreen
import agency.techartists.taanalytics.ui.theme.TAAnalyticsTheme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as AnalyticsExampleApp
        val analytics = app.analytics

        enableEdgeToEdge()
        setContent {
            TAAnalyticsTheme {
                MainScreen(analytics = analytics)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(analytics: agency.techartists.taanalytics.core.TAAnalytics) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (currentRoute != Route.CreateAccount.route) {
                TopAppBar(
                    title = {
                        Text(
                            when (currentRoute) {
                                Route.ContactsPermission.route -> "Contacts"
                                Route.ContactsList.route -> "Contacts"
                                else -> if (currentRoute?.startsWith("contact_detail") == true) {
                                    "Contact Detail"
                                } else {
                                    "TAAnalytics Demo"
                                }
                            }
                        )
                    },
                    navigationIcon = {
                        if (currentRoute == Route.ContactDetail.routeWithArg) {
                            IconButton(onClick = { navController.navigateUp() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.CreateAccount.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Route.CreateAccount.route) {
                CreateAccountScreen(
                    analytics = analytics,
                    onAccountCreated = {
                        navController.navigate(Route.ContactsPermission.route) {
                            popUpTo(Route.CreateAccount.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Route.ContactsPermission.route) {
                ContactsPermissionScreen(
                    analytics = analytics,
                    onPermissionGranted = {
                        navController.navigate(Route.ContactsList.route) {
                            popUpTo(Route.ContactsPermission.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Route.ContactsList.route) {
                ContactsListScreen(
                    analytics = analytics,
                    navController = navController
                )
            }

            composable(
                route = Route.ContactDetail.routeWithArg,
                arguments = listOf(
                    navArgument(Route.ContactDetail.arg) { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val contactId = backStackEntry.arguments?.getString(Route.ContactDetail.arg) ?: ""
                ContactDetailScreen(
                    analytics = analytics,
                    contactId = contactId
                )
            }
        }
    }
}
