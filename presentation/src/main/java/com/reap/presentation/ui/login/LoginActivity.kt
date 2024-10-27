package com.reap.presentation.ui.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reap.data.saveAccessToken
import com.reap.presentation.MainActivity
import com.reap.presentation.R
import com.reap.presentation.common.theme.REAPComposableTheme
import com.reap.presentation.ui.home.calendar.clickable
import com.reap.presentation.ui.splash.SplashScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : ComponentActivity() {
    private val loginViewModel: LoginViewModel by viewModels()
    private lateinit var kakaoLoginLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setKakaoLoginActivityForResult()

        setContent {
            REAPComposableTheme(darkTheme = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    LoginScreen(
                        context = this,
                        onLogin = {
                            Toast.makeText(this, "카카오 로그인 성공", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            startActivity(intent)
                        },
                        onStartKakaoLogin = { startKakaoLogin() },
                        loginViewModel = loginViewModel
                    )
                }
            }
        }
    }

    private fun startKakaoLogin() {
        val intent = Intent(this, AuthCodeHandlerActivity::class.java)
        kakaoLoginLauncher.launch(intent)
    }

    private fun setKakaoLoginActivityForResult() {
        kakaoLoginLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val accessToken = data?.getStringExtra("accessToken")

                if (accessToken != null) {
                    loginViewModel.getAccessToken(accessToken)
                } else {
                    Toast.makeText(this, "카카오 로그인 실패", Toast.LENGTH_SHORT).show()
                }
            } else { // 로그인 실패
                Log.e("KakaoLogin", "카카오 로그인 실패")
                Toast.makeText(this, "카카오 로그인 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }
}


@Composable
fun LoginScreen(
    context: Context,
    onLogin: () -> Unit,
    onStartKakaoLogin: () -> Unit,
    loginViewModel: LoginViewModel
) {
    val accessTokenState = loginViewModel.accessToken.collectAsState()
    var showSplashScreen by remember { mutableStateOf(true) }

    accessTokenState.value?.let { result ->
        if (result.success) {
            Log.e("LoginActivity", "카카오톡 jwt토큰 반환 성공 : ${result.jwtToken}")
            saveAccessToken(context, result.jwtToken)
            onLogin()

        } else {
            Log.e("LoginActivity", "카카오톡 jwt토큰 반환 실패")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = showSplashScreen,
            enter = fadeIn(),
            exit = fadeOut(animationSpec = tween(durationMillis = 1000)),
            modifier = Modifier.fillMaxSize()
        ) {
            SplashScreen(onSplashComplete = { showSplashScreen = false })
        }

        AnimatedVisibility(
            visible = !showSplashScreen,
            enter = fadeIn(animationSpec = tween(durationMillis = 1000)),
            modifier = Modifier.fillMaxSize()
        ) {
            Login(onStartKakaoLogin)
        }
    }
}


@Composable
internal fun Login(
    onStartKakaoLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

        Image(
            bitmap = ImageBitmap.imageResource(id = R.mipmap.ic_logo_foreground),
            contentDescription = "Logo",
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("이메일") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(id = R.color.signature_1),
                focusedLabelColor = Color.Black,
                cursorColor = Color.Black,
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("비밀번호") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(id = R.color.signature_1),
                focusedLabelColor = Color.Black,
                cursorColor = Color.Black,
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "회원가입",
                modifier = Modifier.clickable { },
                color = colorResource(id = R.color.cement_5),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Text(
                text = " | ",
                color = colorResource(id = R.color.cement_5),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Text(
                text = "비밀번호 찾기",
                modifier = Modifier.clickable { },
                color = colorResource(id = R.color.cement_5),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(colorResource(id = R.color.signature_1)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                "로그인", color = Color.Black, style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "간편 로그인",
            modifier = Modifier.fillMaxWidth(),
            color = colorResource(id = R.color.cement_5),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onStartKakaoLogin() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(colorResource(id = R.color.kakao)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_kakao),
                    contentDescription = "Kakao logo",
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 8.dp),
                    tint = Color.Unspecified
                )
                Text(
                    "카카오 로그인",
                    color = Color.Black,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

