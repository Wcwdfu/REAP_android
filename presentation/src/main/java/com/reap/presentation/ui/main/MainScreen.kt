package com.reap.reap_android.ui.main

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.reap.data.getAccessToken
import com.reap.presentation.common.theme.SpeechRed
import com.reap.presentation.navigation.BottomBarItem
import com.reap.presentation.navigation.NavRoutes
import com.reap.presentation.ui.chat.ChatScreen
import com.reap.presentation.ui.home.HomeScreen
import com.reap.presentation.ui.home.calendar.clickable
import com.reap.presentation.ui.main.MainViewModel
import com.reap.presentation.ui.main.UploadStatus
import com.reap.presentation.ui.record.RecordScreen
import com.reap.presentation.ui.selectedDateRecord.SelectedDateRecordScreen

/**
 * Created by Beom_2 on 21.September.2024
 */
@Composable
fun MainScreen() {
    val mainViewModel: MainViewModel = hiltViewModel()
    val navController = rememberNavController()

    Log.d("MainScreen", "${getAccessToken(LocalContext.current)}")
    Box(
        modifier = Modifier
            .fillMaxSize()
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 1000,
                    easing = FastOutSlowInEasing
                )
            )
    ) {
        SettingUpBottomNavigationBarAndCollapsing(navController, mainViewModel)
    }
}

@Composable
fun SettingUpBottomNavigationBarAndCollapsing(navController: NavHostController, mainViewModel: MainViewModel) {
    val snackBarHostState = remember { SnackbarHostState() }
    val bottomBarState = rememberSaveable { (mutableStateOf(false)) }
    val showBottomSheet = remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier,
        snackbarHost = { SnackbarHost(snackBarHostState) },
        bottomBar = {
            if (bottomBarState.value) {
                BottomNavigationBar(
                    modifier = Modifier.padding(0.dp),
                    navController = navController,
                    onRecordClick = { showBottomSheet.value = true }
                )
            }
        }
    ) { paddingValues ->
        MainScreenNavigationConfigurations(navController, paddingValues, bottomBarState, mainViewModel)
    }

    if (showBottomSheet.value) {
        RecordBottomSheet(navController, onDismiss = { showBottomSheet.value = false }, mainViewModel)
    }
}

@Composable
private fun MainScreenNavigationConfigurations(
    navController: NavHostController,
    paddingValues: PaddingValues,
    bottomBarState: MutableState<Boolean>,
    mainViewModel: MainViewModel
) {
    NavHost(
        modifier = Modifier.padding(paddingValues),
        navController = navController,
        startDestination = NavRoutes.Home.route
    ) {
        homeScreen(navController, bottomBarState, mainViewModel)
        recordScreen(navController, bottomBarState)
        selectedDateRecordScreen(navController, bottomBarState)
        chatScreen(navController, bottomBarState)
    }
}

fun NavGraphBuilder.recordScreen(
    navController: NavController,
    bottomBarState: MutableState<Boolean>
) {
    composable(
        route = NavRoutes.Record.route
    ) {
        bottomBarState.value = false

        RecordScreen(navController, LocalContext.current)
    }
}

fun NavGraphBuilder.selectedDateRecordScreen(
    navController: NavController,
    bottomBarState: MutableState<Boolean>,
) {
    composable(
        route = NavRoutes.SelectedDateRecord.route,
        arguments = listOf(navArgument("selectedDate") { type = NavType.StringType })
    ) { backStackEntry ->
        val selectedDate = backStackEntry.arguments?.getString("selectedDate") ?: ""
        bottomBarState.value = true

        SelectedDateRecordScreen(navController = navController, selectedDate = selectedDate)
    }
}

fun NavGraphBuilder.homeScreen(
    navController: NavController,
    bottomBarState: MutableState<Boolean>,
    mainViewModel: MainViewModel
) {
    composable(
        route = NavRoutes.Home.route
    ) {
        bottomBarState.value = true

        HomeScreen(navController, mainViewModel)
    }
}

fun NavGraphBuilder.chatScreen(
    navController: NavController,
    bottomBarState: MutableState<Boolean>
) {
    composable(
        route = NavRoutes.Chat.route
    ) {
        bottomBarState.value = false

        ChatScreen(navController)
    }
}

@Composable
fun BottomNavigationBar(
    modifier: Modifier,
    navController: NavController,
    onRecordClick: () -> Unit
) {
    val bottomNavigationItems = listOf(
        BottomBarItem.Home,
        BottomBarItem.Record,
        BottomBarItem.Search
    )
    NavigationBar(
        modifier
            .graphicsLayer {
                shape = RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp
                )
                clip = true
            },
        containerColor = colorResource(id = com.reap.presentation.R.color.cement_2),
        contentColor = Color.Black
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        bottomNavigationItems.forEach { item ->
            NavigationBarItem(
                icon = { Icon(painterResource(id = item.icon), contentDescription = item.route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SpeechRed,
                    selectedTextColor = SpeechRed,
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = Color.Black,
                    unselectedTextColor = Color.Black,
                ),
                label = { Text(text = item.route) },
                alwaysShowLabel = true,
                selected = currentRoute == item.route,
                onClick = {
                    if (item.route == "Record") {
                        onRecordClick()
                    } else {
                        navController.navigate(item.route) {
                            navController.graph.startDestinationRoute?.let { route ->
                                popUpTo(route) {
                                    saveState = true
                                }
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordBottomSheet(navController: NavHostController, onDismiss: () -> Unit, mainViewModel: MainViewModel) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()
    val uploadStatus by mainViewModel.uploadStatus.collectAsState()
    var selectedTopic by remember { mutableStateOf("일상") } // 주제 기본값 설정
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) } // 선택된 파일의 URI
    var showTopicDialog by remember { mutableStateOf(false) } // 주제 선택 모달 창 표시 여부

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            if (isValidAudioFile(context, it)) {
                selectedFileUri = it // 파일 URI 저장
                showTopicDialog = true // 주제 선택 모달 창 표시
            } else {
                Toast.makeText(context, "유효하지 않은 오디오 파일입니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 주제 선택 모달 창
    if (showTopicDialog) {
        AlertDialog(
            onDismissRequest = { showTopicDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedFileUri?.let { uri ->
                        mainViewModel.uploadAudioFile(uri, selectedTopic) // 선택된 주제와 파일 URI를 업로드
                        showTopicDialog = false
                    }
                }) {
                    Text("업로드")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTopicDialog = false }) {
                    Text("취소")
                }
            },
            title = { Text("주제 선택") },
            text = {
                Column {
                    Text("파일을 업로드할 주제를 선택하세요:")
                    Spacer(modifier = Modifier.height(8.dp))
                    val topics = listOf("일상", "강의", "대화", "회의")
                    topics.forEach { topic ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (selectedTopic == topic),
                                    onClick = { selectedTopic = topic }
                                )
                                .padding(4.dp)
                        ) {
                            RadioButton(
                                selected = (selectedTopic == topic),
                                onClick = { selectedTopic = topic }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = topic)
                        }
                    }
                }
            }
        )
    }

    // 바텀 시트 UI
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "새로 만들기",
                style = MaterialTheme.typography.displayMedium,
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 18.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(id = com.reap.presentation.R.drawable.ic_mike),
                        contentDescription = "녹음",
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                color = colorResource(id = com.reap.presentation.R.color.cement_2),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .clickable {
                                navController.navigate(NavRoutes.Record.route)
                                onDismiss()
                            }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("녹음", style = MaterialTheme.typography.bodyMedium)
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(id = com.reap.presentation.R.drawable.ic_upload),
                        contentDescription = "업로드",
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                color = colorResource(id = com.reap.presentation.R.color.cement_2),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .clickable { launcher.launch("audio/*") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("업로드", style = MaterialTheme.typography.bodyMedium)
                }
            }

            when (uploadStatus) {
                is UploadStatus.Uploading -> CircularProgressIndicator()
                is UploadStatus.Success -> {
                    Toast.makeText(
                        context,
                        "업로드 성공! 파일 ID: ${(uploadStatus as UploadStatus.Success).fileId}",
                        Toast.LENGTH_SHORT
                    ).show()
                    mainViewModel.resetUploadSuccess()
                }
                is UploadStatus.Error -> {
                    Toast.makeText(
                        context,
                        "업로드 실패: ${(uploadStatus as UploadStatus.Error).message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    mainViewModel.resetUploadSuccess()
                }
                else -> { mainViewModel.resetUploadSuccess()}
            }
        }
    }
}



fun isValidAudioFile(context: Context, uri: Uri): Boolean {
    val contentResolver = context.contentResolver
    val mimeType = contentResolver.getType(uri)

    // MIME 타입 검사를 보다 세부적으로 조정
    if (mimeType == null || !(mimeType.startsWith("audio/") || mimeType == "audio/mp4" || mimeType == "audio/x-m4a")) {
        return false
    }


    // 파일 크기 검사 (예: 최대 10MB)
    val fileSize = contentResolver.openFileDescriptor(uri, "r")?.statSize ?: 0
    if (fileSize > 50 * 1024 * 1024) { // 50MB
        return false
    }

    // 추가적인 안전성 검사를 여기에 구현 가능
    // 예: 파일 헤더 검사, 악성코드 스캔 등

    return true
}
