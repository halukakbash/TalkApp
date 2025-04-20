package com.halukakbash.talk_app.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.halukakbash.talk_app.viewmodel.AuthViewModel
import com.halukakbash.talk_app.viewmodel.AuthState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onNavigateToLogin: () -> Unit,
    onSignUpSuccess: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var languageLevel by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var genderExpanded by remember { mutableStateOf(false) }
    var levelExpanded by remember { mutableStateOf(false) }
    var nativeLanguage by remember { mutableStateOf("") }
    var nativeLangExpanded by remember { mutableStateOf(false) }
    var countryExpanded by remember { mutableStateOf(false) }
    
    val scrollState = rememberScrollState()
    val authState by authViewModel.authState.collectAsState()
    
    val languageLevels = listOf(
        "A1 (Beginner)", 
        "A2 (Elementary)", 
        "B1 (Intermediate)", 
        "B2 (Upper-Intermediate)", 
        "C1 (Advanced)", 
        "C2 (Proficient/Fluent)"
    )
    val genderOptions = listOf("Male", "Female")
    val nativeLanguages = listOf(
        "English", "Spanish", "French", "German", "Italian", 
        "Portuguese", "Russian", "Chinese", "Japanese", "Korean",
        "Arabic", "Turkish", "Hindi", "Other"
    )
    
    val countries = listOf(
        "Afghanistan", "Albania", "Algeria", "Andorra", "Angola", "Argentina", "Armenia", "Australia", "Austria", "Azerbaijan",
        "Bahamas", "Bahrain", "Bangladesh", "Belarus", "Belgium", "Bhutan", "Brazil", "Bulgaria",
        "Cambodia", "Cameroon", "Canada", "Chile", "China", "Colombia", "Croatia", "Cyprus", "Czech Republic",
        "Denmark", "Dominican Republic",
        "Ecuador", "Egypt", "Estonia", "Ethiopia",
        "Finland", "France",
        "Georgia", "Germany", "Greece",
        "Hungary",
        "Iceland", "India", "Indonesia", "Iran", "Iraq", "Ireland", "Israel", "Italy",
        "Japan", "Jordan",
        "Kazakhstan", "Kenya", "Kuwait", "Kyrgyzstan",
        "Latvia", "Lebanon", "Libya", "Lithuania", "Luxembourg",
        "Malaysia", "Malta", "Mexico", "Moldova", "Monaco", "Mongolia", "Morocco",
        "Nepal", "Netherlands", "New Zealand", "Nigeria", "North Korea", "Norway",
        "Oman",
        "Pakistan", "Palestine", "Panama", "Peru", "Philippines", "Poland", "Portugal",
        "Qatar",
        "Romania", "Russia",
        "Saudi Arabia", "Serbia", "Singapore", "Slovakia", "Slovenia", "South Africa", "South Korea", "Spain", "Sweden", "Switzerland", "Syria",
        "Taiwan", "Thailand", "Turkey", "Turkmenistan",
        "Ukraine", "United Arab Emirates", "United Kingdom", "United States", "Uruguay", "Uzbekistan",
        "Vatican City", "Venezuela", "Vietnam",
        "Yemen",
        "Zimbabwe"
    ).sorted()
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { photoUri = it }
    }

    LaunchedEffect(authState) {
        println("DEBUG: Auth state changed to: $authState")
        if (authState is AuthState.Success) {
            println("DEBUG: SignUp successful, calling onSignUpSuccess")
            onSignUpSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Profile photo selection
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { launcher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (photoUri != null) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = "Profile photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Add photo",
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Last Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = age,
            onValueChange = { if (it.length <= 3) age = it.filter { char -> char.isDigit() } },
            label = { Text("Age") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = countryExpanded,
            onExpandedChange = { countryExpanded = !countryExpanded }
        ) {
            OutlinedTextField(
                value = country,
                onValueChange = {},
                readOnly = true,
                label = { Text("Country") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = countryExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            DropdownMenu(
                expanded = countryExpanded,
                onDismissRequest = { countryExpanded = false },
                modifier = Modifier.exposedDropdownSize()
            ) {
                countries.forEach { countryName ->
                    DropdownMenuItem(
                        text = { Text(countryName) },
                        onClick = {
                            country = countryName
                            countryExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Gender dropdown
        ExposedDropdownMenuBox(
            expanded = genderExpanded,
            onExpandedChange = { genderExpanded = !genderExpanded }
        ) {
            OutlinedTextField(
                value = gender,
                onValueChange = {},
                readOnly = true,
                label = { Text("Gender") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            DropdownMenu(
                expanded = genderExpanded,
                onDismissRequest = { genderExpanded = false },
                modifier = Modifier.exposedDropdownSize()
            ) {
                genderOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            gender = option
                            genderExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Language level dropdown
        ExposedDropdownMenuBox(
            expanded = levelExpanded,
            onExpandedChange = { levelExpanded = !levelExpanded }
        ) {
            OutlinedTextField(
                value = languageLevel,
                onValueChange = {},
                readOnly = true,
                label = { Text("Language Level") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = levelExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            DropdownMenu(
                expanded = levelExpanded,
                onDismissRequest = { levelExpanded = false },
                modifier = Modifier.exposedDropdownSize()
            ) {
                languageLevels.forEach { level ->
                    DropdownMenuItem(
                        text = { Text(level) },
                        onClick = {
                            languageLevel = level
                            levelExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Native language dropdown
        ExposedDropdownMenuBox(
            expanded = nativeLangExpanded,
            onExpandedChange = { nativeLangExpanded = !nativeLangExpanded }
        ) {
            OutlinedTextField(
                value = nativeLanguage,
                onValueChange = {},
                readOnly = true,
                label = { Text("Native Language") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = nativeLangExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            DropdownMenu(
                expanded = nativeLangExpanded,
                onDismissRequest = { nativeLangExpanded = false },
                modifier = Modifier.exposedDropdownSize()
            ) {
                nativeLanguages.forEach { language ->
                    DropdownMenuItem(
                        text = { Text(language) },
                        onClick = {
                            nativeLanguage = language
                            nativeLangExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { 
                println("DEBUG: Attempting signup...")
                if (validateInputs(
                    email = email,
                    password = password,
                    name = name,
                    lastName = lastName,
                    age = age,
                    country = country,
                    gender = gender,
                    languageLevel = languageLevel,
                    nativeLanguage = nativeLanguage
                )) {
                    authViewModel.signUp(
                        email = email,
                        password = password,
                        name = name,
                        lastName = lastName,
                        age = age.toInt(),
                        country = country,
                        gender = gender,
                        languageLevel = languageLevel,
                        nativeLanguage = nativeLanguage,
                        photoUri = photoUri
                    )
                }
            },
            enabled = authState !is AuthState.Loading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Sign Up")
            }
        }

        TextButton(onClick = onNavigateToLogin) {
            Text("Already have an account? Login")
        }

        if (authState is AuthState.Error) {
            Text(
                text = (authState as AuthState.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
    }
}

private fun validateInputs(
    email: String,
    password: String,
    name: String,
    lastName: String,
    age: String,
    country: String,
    gender: String,
    languageLevel: String,
    nativeLanguage: String
): Boolean {
    if (email.isBlank() || password.isBlank() || name.isBlank() || 
        lastName.isBlank() || age.isBlank() || country.isBlank() || 
        gender.isBlank() || languageLevel.isBlank() || nativeLanguage.isBlank()) {
        return false
    }
    
    // Basic email validation
    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        return false
    }
    
    // Password length validation
    if (password.length < 6) {
        return false
    }
    
    // Age validation
    try {
        val ageInt = age.toInt()
        if (ageInt < 13 || ageInt > 120) {
            return false
        }
    } catch (e: NumberFormatException) {
        return false
    }
    
    return true
} 