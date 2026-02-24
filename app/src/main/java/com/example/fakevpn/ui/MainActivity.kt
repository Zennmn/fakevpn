package com.example.fakevpn.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FakeVPNTheme {
                MainScreen()
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
//  主题
// ──────────────────────────────────────────────────────────────
@Composable
fun FakeVPNTheme(content: @Composable () -> Unit) {
    val colorScheme = darkColorScheme(
        primary = Color(0xFF6C63FF),
        onPrimary = Color.White,
        secondary = Color(0xFF03DAC6),
        surface = Color(0xFF1E1E2E),
        onSurface = Color(0xFFE0E0E0),
        background = Color(0xFF121218),
        onBackground = Color(0xFFE0E0E0),
        surfaceVariant = Color(0xFF2A2A3C),
        onSurfaceVariant = Color(0xFFB0B0C0),
    )
    MaterialTheme(colorScheme = colorScheme, content = content)
}

// ──────────────────────────────────────────────────────────────
//  主界面
// ──────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val isModuleActive = isXposedModuleActive()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "FakeVPN",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 模块状态卡片
            ModuleStatusCard(isActive = isModuleActive)

            Spacer(modifier = Modifier.height(8.dp))

            // Hook 功能说明
            SectionTitle("Hook 列表")

            HookInfoCard(
                icon = Icons.Filled.Lock,
                title = "伪装 VPN Transport",
                description = "拦截 NetworkCapabilities.hasTransport()，\n让应用查询时返回 TRANSPORT_VPN = true",
                gradientStart = Color(0xFF6C63FF),
                gradientEnd = Color(0xFF4834DF)
            )

            HookInfoCard(
                icon = Icons.Filled.NetworkCheck,
                title = "伪装 VPN Capability",
                description = "拦截 hasCapability(NET_CAPABILITY_NOT_VPN)，\n返回 false，表示当前网络是 VPN",
                gradientStart = Color(0xFF00B894),
                gradientEnd = Color(0xFF00897B)
            )

            HookInfoCard(
                icon = Icons.Filled.Wifi,
                title = "伪装活动网络类型",
                description = "修改 getActiveNetworkInfo() 的网络类型为\nTYPE_VPN (17) 和类型名称为 \"VPN\"",
                gradientStart = Color(0xFFE17055),
                gradientEnd = Color(0xFFC62828)
            )

            HookInfoCard(
                icon = Icons.Filled.SettingsEthernet,
                title = "注入 tun0 网卡接口",
                description = "在 NetworkInterface.getNetworkInterfaces()\n中注入假 tun0 接口，模拟 VPN 隧道",
                gradientStart = Color(0xFF74B9FF),
                gradientEnd = Color(0xFF0984E3)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 使用说明
            SectionTitle("使用说明")

            InfoCard(
                text = "1. 在 LSPosed 管理器中启用本模块\n" +
                        "2. 勾选需要伪装 VPN 的目标应用\n" +
                        "3. 强制停止并重新打开目标应用\n" +
                        "4. 目标应用将认为设备正在使用 VPN"
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ──────────────────────────────────────────────────────────────
//  模块状态卡片
// ──────────────────────────────────────────────────────────────
@Composable
fun ModuleStatusCard(isActive: Boolean) {
    val bgColor by animateColorAsState(
        targetValue = if (isActive) Color(0xFF1B5E20) else Color(0xFF4A1010),
        animationSpec = tween(500),
        label = "statusBg"
    )
    val accentColor = if (isActive) Color(0xFF4CAF50) else Color(0xFFEF5350)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isActive) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column {
                Text(
                    text = if (isActive) "模块已激活" else "模块未激活",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isActive) "VPN 伪装正在运行中" else "请在 LSPosed 管理器中启用本模块",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
//  Hook 信息卡片
// ──────────────────────────────────────────────────────────────
@Composable
fun HookInfoCard(
    icon: ImageVector,
    title: String,
    description: String,
    gradientStart: Color,
    gradientEnd: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(listOf(gradientStart, gradientEnd))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
//  通用信息卡片
// ──────────────────────────────────────────────────────────────
@Composable
fun InfoCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(20.dp),
            fontSize = 14.sp,
            lineHeight = 22.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ──────────────────────────────────────────────────────────────
//  小标题
// ──────────────────────────────────────────────────────────────
@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        modifier = Modifier.padding(start = 4.dp)
    )
}

// ──────────────────────────────────────────────────────────────
//  检测 Xposed 模块激活状态
//  该函数的返回值会被 Xposed 在注入后替换为 true。
//  在未被 Hook 的情况下默认返回 false。
// ──────────────────────────────────────────────────────────────
fun isXposedModuleActive(): Boolean {
    return false
}
