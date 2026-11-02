import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensemanager.R
import com.example.expensemanager.profile.AboutUsScreen
import com.example.expensemanager.profile.fetchUserDataAndExportCSV
import com.example.expensemanager.profile.generateUserReport
import com.example.expensemanager.profile.saveCSVToUri
import com.example.expensemanager.signUp
import com.google.firebase.auth.FirebaseAuth

@Preview(showBackground = true)
@Composable
fun ProfileScreenContent() {
    val context = LocalContext.current
    var showAboutUs by remember { mutableStateOf(false) }
    var isLoggedIn by remember { mutableStateOf(true) }
    var reportText by remember { mutableStateOf<String?>(null) }
    var showReportScreen by remember { mutableStateOf(false) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    when {
        !isLoggedIn -> {
            Toast.makeText(context, "Logged Out", Toast.LENGTH_LONG).show()
            val intent = Intent(context, signUp::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(intent)
        }
        showAboutUs -> AboutUsScreen { showAboutUs = false }
        showReportScreen -> ReportScreen(reportText) { showReportScreen = false }
        else -> ProfileScreen(context, userId, { showAboutUs = true }, { isLoggedIn = false }, { showReportScreen = true;reportText = it })
    }
}

@Composable
fun ProfileScreen(
    context: Context,
    userId: String,
    onAboutUsClick: () -> Unit,
    onLogOutClick: () -> Unit,
    onReportClick: (String) -> Unit
) {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("Credentials", Context.MODE_PRIVATE)
    val name = sharedPreferences.getString("uname", "Unknown") ?: "Unknown"
    val email = sharedPreferences.getString("uemail", "Unknown") ?: "Unknown"
    val exportCsvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri: Uri? ->
        uri?.let {
            saveCSVToUri(context, it)
            Toast.makeText(context, "CSV file saved successfully", Toast.LENGTH_SHORT).show()
        } ?: run {
            Toast.makeText(context, "File save canceled", Toast.LENGTH_SHORT).show()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color(0xFF009688))
                .padding(8.dp)
        ) {
            Text(
                text = "PROFILE",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.profile),
                contentDescription = "Profile",
                modifier = Modifier
                    .padding(16.dp)
                    .size(130.dp)
            )
            Column(
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Text(
                    text = "Hello, Welcome $name",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Email: $email",
                    fontSize = 19.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { /* Open settings */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009688)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Settings", color = Color.White)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    fetchUserDataAndExportCSV(context, userId) {
                        exportCsvLauncher.launch("userdata.csv")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009688)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Export CSV File", color = Color.White)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    generateUserReport(context, userId) { report ->
                        onReportClick(report)  // Navigate to Report Screen with report content
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009688)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Generate Report", color = Color.White)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { onAboutUsClick() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009688)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "About Us", color = Color.White)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    sharedPreferences.edit().clear().apply()
                    onLogOutClick()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009688)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Log Out", color = Color.White)
            }
        }
    }
}

@Composable
fun ReportScreen(reportText: String?, onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "User Report",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = reportText ?: "No report generated.",
            fontSize = 16.sp,
            textAlign = TextAlign.Left,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Button(onClick = onBackClick, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009688))) {
            Text(text = "Back to Profile", color = Color.White)
        }
    }
}
