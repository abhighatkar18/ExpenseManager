
package com.example.expensemanager.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensemanager.ui.theme.ExmanColors

@Composable
private fun BrandHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .background(
                    brush = Brush.linearGradient(
                        listOf(
                            ExmanColors.Emerald,
                            ExmanColors.DeepEmerald
                        )
                    ),
                    shape = RoundedCornerShape(22.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(Modifier.height(14.dp))

        Text(
            text = "exman.",
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "YOUR MONEY, BEAUTIFULLY MANAGED",
            fontSize = 10.sp,
            letterSpacing = 1.6.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AuthContainer(
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = 24.dp,
                    vertical = 24.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

@Composable
private fun ExmanField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    password: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    var show by rememberSaveable {
        mutableStateOf(false)
    }

    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        modifier = modifier.fillMaxWidth(),

        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        },

        trailingIcon = if (password) {
            {
                IconButton(
                    onClick = { show = !show }
                ) {
                    Icon(
                        imageVector = if (show) {
                            Icons.Default.VisibilityOff
                        } else {
                            Icons.Default.Visibility
                        },
                        contentDescription = if (show) {
                            "Hide password"
                        } else {
                            "Show password"
                        }
                    )
                }
            }
        } else null,

        visualTransformation = if (password && !show) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },

        keyboardOptions = KeyboardOptions(
            keyboardType = if (password) {
                KeyboardType.Password
            } else {
                keyboardType
            }
        ),

        shape = RoundedCornerShape(16.dp),

        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        )
    )
}

@Composable
private fun SubmitButton(
    text: String,
    busy: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = !busy,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (busy) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun Feedback(viewModel: AuthViewModel) {
    viewModel.error?.let {
        Text(
            text = it,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth()
        )
    }

    viewModel.notice?.let {
        Text(
            text = it,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun LoginScreen(
    onSignUp: () -> Unit,
    onAuthenticated: () -> Unit,
    vm: AuthViewModel = viewModel()
) {
    var email by rememberSaveable {
        mutableStateOf("")
    }

    var password by rememberSaveable {
        mutableStateOf("")
    }

    var resetting by rememberSaveable {
        mutableStateOf(false)
    }

    AuthContainer {
        Spacer(Modifier.height(20.dp))

        BrandHeader()

        Spacer(Modifier.height(38.dp))

        Column(Modifier.fillMaxWidth()) {

            if (resetting) {
                IconButton(
                    onClick = {
                        resetting = false
                        vm.clearFeedback()
                    }
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to sign in"
                    )
                }
            }

            Text(
                text = if (resetting) {
                    "Reset password"
                } else {
                    "Welcome back!"
                },
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = if (resetting) {
                    "We'll email a password-reset link to you."
                } else {
                    "Sign in to manage your finances."
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(25.dp))

            ExmanField(
                value = email,
                onChange = {
                    email = it
                    vm.clearFeedback()
                },
                label = "Email address",
                icon = Icons.Default.Mail,
                keyboardType = KeyboardType.Email
            )

            if (!resetting) {

                Spacer(Modifier.height(14.dp))

                ExmanField(
                    value = password,
                    onChange = {
                        password = it
                        vm.clearFeedback()
                    },
                    label = "Password",
                    icon = Icons.Default.Lock,
                    password = true
                )

                Spacer(Modifier.height(9.dp))

                TextButton(
                    onClick = {
                        resetting = true
                        vm.clearFeedback()
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Forgot password?")
                }
            }

            Spacer(Modifier.height(12.dp))

            Feedback(vm)

            Spacer(Modifier.height(14.dp))

            SubmitButton(
                text = if (resetting) {
                    "Send reset link"
                } else {
                    "Sign in"
                },
                busy = vm.busy,
                onClick = {
                    if (resetting) {
                        vm.reset(email)
                    } else {
                        vm.login(
                            email,
                            password,
                            onAuthenticated
                        )
                    }
                }
            )
        }

        Spacer(Modifier.height(26.dp))

        if (!resetting) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "New to Exman? ",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Create account",
                    modifier = Modifier.clickable(
                        enabled = !vm.busy
                    ) {
                        onSignUp()
                    },
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
fun SignUpScreen(
    onLogin: () -> Unit,
    onAuthenticated: () -> Unit,
    vm: AuthViewModel = viewModel()
) {
    var name by rememberSaveable {
        mutableStateOf("")
    }

    var email by rememberSaveable {
        mutableStateOf("")
    }

    var password by rememberSaveable {
        mutableStateOf("")
    }

    var confirm by rememberSaveable {
        mutableStateOf("")
    }

    AuthContainer {
        BrandHeader()

        Spacer(Modifier.height(26.dp))

        Column(Modifier.fillMaxWidth()) {

            Text(
                "Create your account",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(6.dp))

            Text(
                "Start building healthier money habits.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(20.dp))

            ExmanField(
                value = name,
                onChange = {
                    name = it
                    vm.clearFeedback()
                },
                label = "Full name",
                icon = Icons.Default.Person
            )

            Spacer(Modifier.height(12.dp))

            ExmanField(
                value = email,
                onChange = {
                    email = it
                    vm.clearFeedback()
                },
                label = "Email address",
                icon = Icons.Default.Mail,
                keyboardType = KeyboardType.Email
            )

            Spacer(Modifier.height(12.dp))

            ExmanField(
                value = password,
                onChange = {
                    password = it
                    vm.clearFeedback()
                },
                label = "Password",
                icon = Icons.Default.Lock,
                password = true
            )

            Spacer(Modifier.height(12.dp))

            ExmanField(
                value = confirm,
                onChange = {
                    confirm = it
                    vm.clearFeedback()
                },
                label = "Confirm password",
                icon = Icons.Default.Lock,
                password = true
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Use at least 6 characters.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(15.dp))

            Feedback(vm)

            Spacer(Modifier.height(14.dp))

            SubmitButton(
                text = "Create account",
                busy = vm.busy,
                onClick = {
                    vm.register(
                        name,
                        email,
                        password,
                        confirm,
                        onAuthenticated
                    )
                }
            )
        }

        Spacer(Modifier.height(25.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Already have an account? ",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Sign in",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(
                    enabled = !vm.busy
                ) {
                    onLogin()
                }
            )
        }
    }
}
