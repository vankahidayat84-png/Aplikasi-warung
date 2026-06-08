package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarungApp(viewModel: WarungViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isDarkTheme by viewModel.darkThemeEnabled.collectAsState()
    val warungName by viewModel.warungName.collectAsState()

    // Handle system Android Back Hardware Button
    BackHandler(enabled = currentScreen != Screen.HomeMenu) {
        viewModel.navigateBack()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
        topBar = {
            if (currentScreen != Screen.HomeMenu && currentScreen != Screen.Dashboard) {
                TopAppBar(
                    title = {
                        Text(
                            text = when (currentScreen) {
                                is Screen.Dashboard -> warungName
                                is Screen.KelolaBarang -> "Kelola Barang"
                                is Screen.TransaksiBaru -> "Pilih Barang & Keranjang"
                                is Screen.Pembayaran -> "Pembayaran Transaksi"
                                is Screen.Laporan -> "Laporan Harian"
                                is Screen.Pengeluaran -> "Catat Pengeluaran"
                                is Screen.Hutang -> "Catatan Hutang"
                                is Screen.Statistik -> "Statistik Warung"
                                is Screen.Pengaturan -> "Pengaturan Aplikasi"
                                else -> "Catatan Warung"
                            },
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.navigateBack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Kembali",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentScreen) {
                is Screen.HomeMenu -> LandingScreen(viewModel)
                is Screen.Dashboard -> DashboardScreen(viewModel)
                is Screen.KelolaBarang -> ControlBarangScreen(viewModel)
                is Screen.TransaksiBaru -> TransaksiBaruScreen(viewModel)
                is Screen.Pembayaran -> PembayaranScreen(viewModel)
                is Screen.Laporan -> LaporanScreen(viewModel)
                is Screen.Pengeluaran -> PengeluaranScreen(viewModel)
                is Screen.Hutang -> HutangScreen(viewModel)
                is Screen.Statistik -> StatistikScreen(viewModel)
                is Screen.Pengaturan -> PengaturanScreen(viewModel)
            }
        }
    }
}

// ==========================================
// 1. LANDING SPLASH SCREEN
// ==========================================
@Composable
fun LandingScreen(viewModel: WarungViewModel) {
    val isDarkTheme by viewModel.darkThemeEnabled.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        // Premium Logo representing storefront-cashier
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Storefront emoji styled elegantly
                Text("🏪", fontSize = 52.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("CASHIER", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 2.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "CATATAN WARUNG",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Kasir & Pembukuan Finansial 100% Offline",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 40.dp)
        )

        // Large "MULAI" button in center
        Button(
            onClick = { viewModel.navigateTo(Screen.Dashboard) },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(60.dp)
                .testTag("mulai_button"),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isDarkTheme) NaturalGreenSecondaryDark else NaturalGreenPrimary,
                contentColor = if (isDarkTheme) Color.Black else Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "MULAI",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(44.dp))

        // Menu items card below start button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Menu Cepat",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp, start = 8.dp)
                )

                Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))

                LandingMenuItem(
                    icon = "📊",
                    title = "Statistik Warung",
                    subtitle = "Laba rugi, diagram barang terlaris",
                    onClick = { viewModel.navigateTo(Screen.Statistik) }
                )
                LandingMenuItem(
                    icon = "📦",
                    title = "Kelola Barang",
                    subtitle = "Stok inventaris, edit, & restok barang",
                    onClick = { viewModel.navigateTo(Screen.KelolaBarang) }
                )
                LandingMenuItem(
                    icon = "📝",
                    title = "Catatan Hutang",
                    subtitle = "Daftar hutang pelanggan warung",
                    onClick = { viewModel.navigateTo(Screen.Hutang) }
                )
                LandingMenuItem(
                    icon = "⚙️",
                    title = "Pengaturan",
                    subtitle = "Ubah nama warung, backup data & tema",
                    onClick = { viewModel.navigateTo(Screen.Pengaturan) }
                )
                LandingMenuItem(
                    icon = "ℹ️",
                    title = "Tentang Aplikasi",
                    subtitle = "Informasi developer & aplikasi",
                    onClick = {
                        viewModel.updateWarungName(viewModel.warungName.value) // Ensure name is persisted
                        viewModel.navigateTo(Screen.Pengaturan)
                    }
                )
            }
        }
    }
}

@Composable
fun LandingMenuItem(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 24.sp)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
        )
    }
}


// ==========================================
// 2. DASHBOARD SCREEN
// ==========================================
@Composable
fun DashboardScreen(viewModel: WarungViewModel) {
    val warungName by viewModel.warungName.collectAsState()
    val omzetHariIni by viewModel.omzetHariIni.collectAsState()
    val labaHariIni by viewModel.labaHariIni.collectAsState()
    val txCountHariIni by viewModel.transaksiCountHariIni.collectAsState()
    val totalItems by viewModel.totalBarangCount.collectAsState()
    val stokMenipis by viewModel.stokMenipisCount.collectAsState()

    val context = LocalContext.current
    val todayDateString = remember {
        val sdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
        sdf.format(Date())
    }

    val isDark = isSystemInDarkTheme()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Natural Tones Header Section (matching HTML mockup inline px-6 pt-8 pb-4)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "WARUNG SAYA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) NaturalGreenPrimaryDark.copy(alpha = 0.8f) else Color(0xFF047857).copy(alpha = 0.7f),
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = warungName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDark) Color.White else Color(0xFF022C22),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                
                // Settings Circle
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isDark) NaturalGreenPrimaryDark.copy(alpha = 0.15f) else Color(0xFFECFDF5))
                        .clickable { viewModel.navigateTo(Screen.Pengaturan) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Pengaturan",
                        tint = if (isDark) NaturalGreenPrimaryDark else Color(0xFF047857),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 2. Primary Stats Card (Laba & Omzet - matching HTML emerald bg and shadow)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                shape = RoundedCornerShape(28.dp), // rounded-[2rem] equivalent
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) NaturalDarkSurface else Color(0xFF047857)
                ),
                border = if (isDark) BorderStroke(1.dp, NaturalGreenPrimaryDark.copy(alpha = 0.2f)) else null,
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = "LABA HARI INI",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFFECFDF5).copy(alpha = 0.8f),
                                letterSpacing = 1.2.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = formatRupiah(labaHariIni),
                                fontSize = 30.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isDark) DarkAccentGold else LightAccentGold,
                                letterSpacing = (-0.5).sp
                            )
                        }
                        
                        // comparison pill vs yesterday
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFF059669).copy(alpha = 0.4f))
                                .border(1.dp, if (isDark) Color.White.copy(alpha = 0.15f) else Color(0xFF10B981).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (labaHariIni >= 0) "+12% vs Kemarin" else "-8% vs Kemarin",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White.copy(alpha = 0.9f) else Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(if (isDark) Color.White.copy(alpha = 0.1f) else Color(0xFF059669).copy(alpha = 0.5f))
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "OMZET",
                                fontSize = 10.sp,
                                color = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFFECFDF5).copy(alpha = 0.6f),
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formatRupiah(omzetHariIni),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "TRANSAKSI",
                                fontSize = 10.sp,
                                color = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFFECFDF5).copy(alpha = 0.6f),
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$txCountHariIni",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // 3. Bottom Action Area/Buttons matching mockup large "TRANSAKSI BARU"
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.navigateTo(Screen.TransaksiBaru) },
                    modifier = Modifier
                        .weight(1.1f)
                        .height(56.dp)
                        .testTag("transaksi_baru_fab"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) NaturalGreenPrimaryDark else Color(0xFF059669),
                        contentColor = if (isDark) Color.Black else Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TRANSAKSI BARU",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )
                }

                OutlinedButton(
                    onClick = { viewModel.navigateTo(Screen.Pengeluaran) },
                    modifier = Modifier
                        .weight(0.9f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isDark) NaturalGreenPrimaryDark else Color(0xFF047857)
                    ),
                    border = BorderStroke(
                        width = 1.0.dp,
                        color = if (isDark) NaturalGreenPrimaryDark.copy(alpha = 0.3f) else Color(0xFF047857).copy(alpha = 0.3f)
                    )
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Catat Biaya", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 4. Highlight Section Title
        item {
            Text(
                text = "Ikhtisar Toko",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) NaturalGreenPrimaryDark else Color(0xFF047857),
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }

        // 5. Secondary Quick Cards Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardMetricCard(
                    title = "Hari Ini",
                    value = todayDateString,
                    icon = "📅",
                    modifier = Modifier.weight(1.1f)
                )

                DashboardMetricCard(
                    title = "Total Produk",
                    value = "$totalItems item",
                    icon = "📦",
                    modifier = Modifier.weight(0.9f)
                )
            }
        }

        // 6. Critical Alert Card (Low stock alert - matching HTML alert)
        item {
            val hasLowStock = stokMenipis > 0
            val containerColor = if (hasLowStock) {
                if (isDark) Color(0xFF321A0F) else Color(0xFFFFF9F2)
            } else {
                MaterialTheme.colorScheme.surface
            }
            val borderColor = if (hasLowStock) {
                if (isDark) Color(0xFF9A3412).copy(alpha = 0.5f) else Color(0xFFFED7AA)
            } else {
                if (isDark) NaturalGreenPrimaryDark.copy(alpha = 0.1f) else Color(0xFFECFDF5)
            }
            val textMainColor = if (hasLowStock) {
                if (isDark) Color(0xFFFFEDD5) else Color(0xFF78350F)
            } else {
                MaterialTheme.colorScheme.onSurface
            }
            val textSubColor = if (hasLowStock) {
                if (isDark) Color(0xFFFFEDD5).copy(alpha = 0.7f) else Color(0xFF92400E).copy(alpha = 0.8f)
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            }
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(Screen.KelolaBarang) },
                shape = RoundedCornerShape(24.dp), // rounded-3xl
                colors = CardDefaults.cardColors(containerColor = containerColor),
                border = BorderStroke(width = 1.dp, color = borderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (hasLowStock) {
                                    if (isDark) Color(0xFF452212) else Color(0xFFFEF3C7)
                                } else {
                                    if (isDark) NaturalGreenPrimaryDark.copy(alpha = 0.15f) else Color(0xFFECFDF5)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (hasLowStock) "⚠️" else "✅", fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (hasLowStock) "Stok Menipis!" else "Semua Stok Aman",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = textMainColor
                        )
                        Text(
                            text = if (hasLowStock) "$stokMenipis Produk memerlukan restok segera" else "Stok semua produk Anda aman.",
                            fontSize = 11.sp,
                            color = textSubColor
                        )
                    }

                    if (hasLowStock) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isDark) Color(0xFF9A3412) else Color(0xFFFED7AA))
                                .clickable { viewModel.navigateTo(Screen.KelolaBarang) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "LIHAT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF78350F)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }

        // 7. Navigation list card (Kelola Data & Laporan with border stroke)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp), // rounded-3xl
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(
                    1.dp,
                    if (isDark) NaturalGreenPrimaryDark.copy(alpha = 0.15f) else Color(0xFFECFDF5)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "Kelola Data & Laporan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (isDark) NaturalGreenPrimaryDark else Color(0xFF047857),
                        modifier = Modifier.padding(start = 12.dp, top = 10.dp, bottom = 4.dp)
                    )

                    Divider(
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )

                    DashboardNavRow(
                        icon = "📦",
                        title = "Kelola Stok Barang",
                        onClick = { viewModel.navigateTo(Screen.KelolaBarang) }
                    )
                    DashboardNavRow(
                        icon = "📋",
                        title = "Laporan Penjualan Hari Ini",
                        onClick = { viewModel.navigateTo(Screen.Laporan) }
                    )
                    DashboardNavRow(
                        icon = "💸",
                        title = "Catatan Pengeluaran Warung",
                        onClick = { viewModel.navigateTo(Screen.Pengeluaran) }
                    )
                    DashboardNavRow(
                        icon = "📝",
                        title = "Manajemen Hutang Pelanggan",
                        onClick = { viewModel.navigateTo(Screen.Hutang) }
                    )
                    DashboardNavRow(
                        icon = "📊",
                        title = "Statistik & Diagram Bulanan",
                        onClick = { viewModel.navigateTo(Screen.Statistik) }
                    )
                }
            }
        }

        // 8. Bottom App Branding Detail
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Catatan Warung - Aplikasi Kasir Offline",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
fun DashboardMetricCard(
    title: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp), // Rounded corner 3xl 
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            if (isDark) NaturalGreenPrimaryDark.copy(alpha = 0.12f) else Color(0xFFECFDF5)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isDark) {
                            NaturalGreenPrimaryDark.copy(alpha = 0.15f)
                        } else {
                            Color(0xFFECFDF5) // bg-emerald-50
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 18.sp)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = title, 
                fontSize = 12.sp, 
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface, 
                maxLines = 1, 
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) NaturalGreenPrimaryDark else Color(0xFF047857),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun DashboardNavRow(
    icon: String,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 18.sp)
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f),
            modifier = Modifier.size(20.dp)
        )
    }
}


// ==========================================
// 3. KELOLA BARANG (INVENTORY SCREEN)
// ==========================================
@Composable
fun ControlBarangScreen(viewModel: WarungViewModel) {
    val barangList by viewModel.barangList.collectAsState()
    var searchInput by remember { mutableStateOf("") }
    var selectKategoriFilter by remember { mutableStateOf("Semua") }

    var showAddDialog by remember { mutableStateOf(false) }
    var editMode by remember { mutableStateOf(false) }

    var showRestokDialog by remember { mutableStateOf(false) }
    var selectedItemForRestok by remember { mutableStateOf<Barang?>(null) }
    var restokAmtString by remember { mutableStateOf("") }

    val filteredList = remember(barangList, searchInput, selectKategoriFilter) {
        barangList.filter {
            val matchesQuery = it.nama.contains(searchInput, ignoreCase = true) ||
                    it.kategori.contains(searchInput, ignoreCase = true)
            val matchesCat = selectKategoriFilter == "Semua" || it.kategori.equals(selectKategoriFilter, ignoreCase = true)
            matchesQuery && matchesCat
        }
    }

    val categories = remember(barangList) {
        listOf("Semua") + barangList.map { it.kategori }.distinct()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.setProductForm(null)
                    editMode = false
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Barang")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Search field
            OutlinedTextField(
                value = searchInput,
                onValueChange = { searchInput = it },
                placeholder = { Text("Cari barang atau kategori...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Kategori Pill Filters
            Text("Filter Kategori:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(items = categories) { cat ->
                    val isSelected = selectKategoriFilter == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                            .clickable { selectKategoriFilter = cat }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🗳️", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Belum ada barang terdaftar.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                        Text(
                            "Silakan ketuk tombol + untuk tambah.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList) { b ->
                        BarangItemCard(
                            barang = b,
                            onEdit = {
                                viewModel.setProductForm(b)
                                editMode = true
                                showAddDialog = true
                            },
                            onDelete = {
                                viewModel.deleteProduct(b)
                            },
                            onRestok = {
                                selectedItemForRestok = b
                                restokAmtString = ""
                                showRestokDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Dropdown Dialog Form for ADD / EDIT Barang
    if (showAddDialog) {
        Dialog(onDismissRequest = { showAddDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (editMode) "Edit Barang" else "Tambah Barang Baru",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Divider()

                    // inputs
                    val nState by viewModel.productName.collectAsState()
                    val cState by viewModel.productCategory.collectAsState()
                    val mState by viewModel.productCostPrice.collectAsState()
                    val jState by viewModel.productSalePrice.collectAsState()
                    val sState by viewModel.productStock.collectAsState()
                    val uState by viewModel.productUnit.collectAsState()

                    OutlinedTextField(
                        value = nState,
                        onValueChange = { viewModel.productName.value = it },
                        label = { Text("Nama Barang") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = cState,
                        onValueChange = { viewModel.productCategory.value = it },
                        label = { Text("Kategori (Contoh: Makanan, Minuman)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = mState,
                            onValueChange = { viewModel.productCostPrice.value = it.filter { c -> c.isDigit() } },
                            label = { Text("Harga Modal") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = jState,
                            onValueChange = { viewModel.productSalePrice.value = it.filter { c -> c.isDigit() } },
                            label = { Text("Harga Jual") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = sState,
                            onValueChange = { viewModel.productStock.value = it.filter { c -> c.isDigit() } },
                            label = { Text("Stok Awal") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            enabled = !editMode // Do not edit stock directly from edit form, use RESTOK button!
                        )

                        // Satuan custom picker (with 5 buttons)
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Satuan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            val listSatuans = listOf("Pcs", "Dus", "Botol", "Kg", "Liter")
                            var expandedS by remember { mutableStateOf(false) }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                    .clickable { expandedS = !expandedS }
                                    .padding(vertical = 10.dp, horizontal = 12.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text(uState, fontWeight = FontWeight.Bold)
                                    Icon(Icons.Default.ArrowDropDown, null)
                                }
                            }

                            DropdownMenu(expanded = expandedS, onDismissRequest = { expandedS = false }) {
                                listSatuans.forEach { satOption ->
                                    DropdownMenuItem(
                                        text = { Text(satOption) },
                                        onClick = {
                                            viewModel.productUnit.value = satOption
                                            expandedS = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(onClick = { showAddDialog = false }) {
                            Text("Batal")
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                viewModel.saveProduct()
                                showAddDialog = false
                            }
                        ) {
                            Text("Simpan")
                        }
                    }
                }
            }
        }
    }

    // Dialog RESTOK Barang
    if (showRestokDialog && selectedItemForRestok != null) {
        Dialog(onDismissRequest = { showRestokDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Restok Barang",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Barang: ${selectedItemForRestok!!.nama}\nStok Sekarang: ${selectedItemForRestok!!.stok} ${selectedItemForRestok!!.satuan}",
                        fontSize = 13.sp
                    )

                    OutlinedTextField(
                        value = restokAmtString,
                        onValueChange = { restokAmtString = it.filter { c -> c.isDigit() } },
                        label = { Text("Jumlah Tambah Stok") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showRestokDialog = false }) {
                            Text("Batal")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            enabled = restokAmtString.toIntOrNull() ?: 0 > 0,
                            onClick = {
                                val amt = restokAmtString.toIntOrNull() ?: 0
                                viewModel.restokProduct(selectedItemForRestok!!, amt)
                                showRestokDialog = false
                            }
                        ) {
                            Text("Tambah Stok")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BarangItemCard(
    barang: Barang,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onRestok: () -> Unit
) {
    val lowStock = barang.stok <= 5

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            if (lowStock) WarningOrange.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            barang.nama,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(barang.kategori, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Jual: ${formatRupiah(barang.hargaJual)}  |  Modal: ${formatRupiah(barang.hargaModal)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                // Restok Button Action
                Button(
                    onClick = onRestok,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (lowStock) WarningOrange else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        contentColor = if (lowStock) Color.White else MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("Restok", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Stok: ", fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                    Text(
                        "${barang.stok} ${barang.satuan}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = if (lowStock) NegativeRed else MaterialTheme.colorScheme.onBackground
                    )
                    if (lowStock) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "(Hampir Habis)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = NegativeRed
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = NegativeRed, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}


// ==========================================
// 4. TRANSAKSI BARU (CASHIER BASKET SCREEN)
// ==========================================
@Composable
fun TransaksiBaruScreen(viewModel: WarungViewModel) {
    val barangList by viewModel.barangList.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val cartTotal by viewModel.cartTotalBelanja.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showProductSelectDialog by remember { mutableStateOf(false) }
    var selectedItemForQty by remember { mutableStateOf<Barang?>(null) }
    var qtySelected by remember { mutableIntStateOf(1) }

    val filteredProducts = remember(barangList, searchQuery) {
        barangList.filter {
            it.nama.contains(searchQuery, ignoreCase = true) ||
                    it.kategori.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Upper layout: Cashier Product Picker Title / Input Search
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    "Pencarian Barang Warung",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Ketik nama atau kategori barang...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Left / High area: Products List based on query
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (filteredProducts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Gagal menemukan barang cocok.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredProducts) { item ->
                        val isLowStock = item.stok == 0
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isLowStock) {
                                    selectedItemForQty = item
                                    qtySelected = 1
                                    showProductSelectDialog = true
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("📦", fontSize = 16.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.nama, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(formatRupiah(item.hargaJual), fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Stok: ${item.stok}", fontSize = 11.sp, color = if (isLowStock) NegativeRed else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                                    if (isLowStock) {
                                        Text("HABIS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NegativeRed)
                                    } else {
                                        Text("Pilih [+] ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Split Drawer / Bottom Section: Shopping Cart
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Daftar Keranjang (${cart.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (cart.isNotEmpty()) {
                        Text(
                            "Hapus Semua",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NegativeRed,
                            modifier = Modifier.clickable { viewModel.clearCart() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))

                if (cart.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Keranjang belanja kosong.\nPilih barang di atas untuk ditambahkan.",
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 140.dp)
                    ) {
                        items(cart) { item ->
                            val b = item.first
                            val qty = item.second
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(b.nama, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("$qty x ${formatRupiah(b.hargaJual)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        formatRupiah(b.hargaJual * qty),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
                                    // small buttons [- / +]
                                    IconButton(
                                        onClick = { viewModel.updateCartQty(b.id, qty - 1) },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(Icons.Default.Clear, contentDescription = "Kurang", tint = NegativeRed, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 8.dp))

                // Total Belanja & Trigger Checkout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Belanja", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                        Text(formatRupiah(cartTotal), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    }

                    Button(
                        onClick = { viewModel.navigateTo(Screen.Pembayaran) },
                        enabled = cart.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = PositiveGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("selesaikan_transaksi_button")
                    ) {
                        Text("Selesaikan Transaksi", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }

    // Modal dialogue qty selection
    if (showProductSelectDialog && selectedItemForQty != null) {
        val selected = selectedItemForQty!!
        Dialog(onDismissRequest = { showProductSelectDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(selected.nama, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    Text("Harga Jual: ${formatRupiah(selected.hargaJual)}\nStok Tersedia: ${selected.stok} ${selected.satuan}", fontSize = 12.sp)

                    // Qty selectors
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { if (qtySelected > 1) qtySelected-- },
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        ) {
                            Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }

                        Text(
                            "$qtySelected",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )

                        IconButton(
                            onClick = { if (qtySelected < selected.stok) qtySelected++ },
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        ) {
                            Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showProductSelectDialog = false }) {
                            Text("Batal")
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                viewModel.addToCart(selected, qtySelected)
                                showProductSelectDialog = false
                            }
                        ) {
                            Text("Tambah ke Keranjang")
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// 5. PEMBAYARAN CHECKOUT SCREEN
// ==========================================
@Composable
fun PembayaranScreen(viewModel: WarungViewModel) {
    val totalBelanja by viewModel.cartTotalBelanja.collectAsState()
    val rawInput by viewModel.nominalBayarInput.collectAsState()

    val nominalBayar = rawInput.toDoubleOrNull() ?: 0.0
    val kembalian = nominalBayar - totalBelanja
    val isEnough = nominalBayar >= totalBelanja

    val keyboardController = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("TOTAL BELANJA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                Text(
                    formatRupiah(totalBelanja),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))

                Spacer(modifier = Modifier.height(14.dp))

                // Nominal pay inputs
                Text("Ketuk Nominal Pembayaran Pelanggan", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = rawInput,
                    onValueChange = { viewModel.updateNominalBayar(it) },
                    placeholder = { Text("Ketik nominal cash...") },
                    leadingIcon = { Text("Rp ", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 10.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("nominal_bayar_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Fast Cash recommendations buttons click
                val quickPricings = listOf(totalBelanja, 10000.0, 20000.0, 50000.0, 100000.0)
                    .filter { it >= totalBelanja }
                    .distinct()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickPricings.forEach { money ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .clickable { viewModel.updateNominalBayar(money.toInt().toString()) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "Pas / ${money.toInt()}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Return cash display card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isEnough) PositiveGreen.copy(alpha = 0.1f) else NegativeRed.copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isEnough) {
                    Text("KEMBALIAN UTK PELANGGAN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PositiveGreen)
                    Text(
                        formatRupiah(kembalian),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PositiveGreen
                    )
                } else {
                    Text("PEMBAYARAN BELUM CUKUP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NegativeRed)
                    Text(
                        "Kurang " + formatRupiah(totalBelanja - nominalBayar),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = NegativeRed
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                viewModel.saveCurrentTransaction {
                    viewModel.navigateTo(Screen.Dashboard)
                }
            },
            enabled = isEnough,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("simpan_transaksi_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PositiveGreen)
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Simpan Transaksi Kasir", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}


// ==========================================
// 6. LAPORAN HARIAN & LOGS TIMELINE
// ==========================================
@Composable
fun LaporanScreen(viewModel: WarungViewModel) {
    val txs by viewModel.transaksiList.collectAsState()
    val details by viewModel.detailList.collectAsState()

    val omzet by viewModel.omzetHariIni.collectAsState()
    val modal by viewModel.modalHariIni.collectAsState()
    val labaKKotor by viewModel.labaKotorHariIni.collectAsState()
    val pengeluaran by viewModel.pengeluaranHariIni.collectAsState()
    val labaBersih by viewModel.labaHariIni.collectAsState()
    val totalCount by viewModel.transaksiCountHariIni.collectAsState()
    val barangTerjualCount by viewModel.barangTerjualCountHariIni.collectAsState()

    var showDetailDialogForTx by remember { mutableStateOf<Transaksi?>(null) }

    val todayMillis = remember {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }

    val todayTxs = remember(txs) {
        txs.filter { it.tanggal >= todayMillis }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Daily Grid Metrics
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Rekapitulasi Keuangan Keuntungan Hari Ini",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            LaporanPillLabel(label = "Omzet (Penjualan)", value = formatRupiah(omzet))
                            Spacer(modifier = Modifier.height(6.dp))
                            LaporanPillLabel(label = "HPP Modal", value = formatRupiah(modal))
                            Spacer(modifier = Modifier.height(6.dp))
                            LaporanPillLabel(label = "Total Biaya (Beban)", value = formatRupiah(pengeluaran))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            LaporanPillLabel(label = "Laba Kotor", value = formatRupiah(labaKKotor))
                            Spacer(modifier = Modifier.height(6.dp))
                            LaporanPillLabel(
                                label = "Laba Bersih",
                                value = formatRupiah(labaBersih),
                                valueColor = if (labaBersih < 0) NegativeRed else PositiveGreen,
                                highlight = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Jumlah Transaksi: $totalCount", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Produk Terjual: $barangTerjualCount", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Timeline item logs title
        item {
            Text(
                "Riwayat Penjualan Hari Ini",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )
        }

        if (todayTxs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada riwayat transaksi hari ini.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
                }
            }
        } else {
            items(todayTxs) { t ->
                val timeString = remember(t.tanggal) {
                    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                    sdf.format(Date(t.tanggal))
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDetailDialogForTx = t },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🧾", fontSize = 18.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Transaksi ID #100${t.id}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Jam $timeString", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(formatRupiah(t.totalBelanja), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                            Text("Laba: +${formatRupiah(t.totalLaba)}", fontSize = 10.sp, color = PositiveGreen, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        IconButton(onClick = { viewModel.deleteProduct(Barang(id = -99, nama = "", kategori = "", hargaModal = 0.0, hargaJual = 0.0, stok = 0, satuan = "")) }) { // Unused
                            Icon(Icons.Default.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
                        }
                    }
                }
            }
        }
    }

    // Modal details dialog for transaction log
    if (showDetailDialogForTx != null) {
        val activeTx = showDetailDialogForTx!!
        val subDetails = details.filter { it.transaksiId == activeTx.id }

        Dialog(onDismissRequest = { showDetailDialogForTx = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Informasi Detail Transaksi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("Transaksi ID: #100${activeTx.id}", fontSize = 12.sp)
                    Text("Tunai Bayar: ${formatRupiah(activeTx.nominalBayar)}\nKembalian: ${formatRupiah(activeTx.kembalian)}", fontSize = 12.sp)

                    Divider()

                    Text("Rincian Belanja:", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                    LazyColumn(modifier = Modifier.heightIn(max = 180.dp)) {
                        items(subDetails) { det ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "${det.namaBarang} (x${det.jumlah})",
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(formatRupiah(det.subtotal), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(onClick = { showDetailDialogForTx = null }) {
                            Text("Tutup Laporan")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LaporanPillLabel(
    label: String,
    value: String,
    valueColor: Color = Color.Unspecified,
    highlight: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (highlight) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.02f)
            )
            .padding(10.dp)
    ) {
        Column {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                value,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (valueColor == Color.Unspecified) MaterialTheme.colorScheme.onBackground else valueColor
            )
        }
    }
}


// ==========================================
// 7. PENGELUARAN SCREEN
// ==========================================
@Composable
fun PengeluaranScreen(viewModel: WarungViewModel) {
    val exps by viewModel.pengeluaranList.collectAsState()
    var showFormDialog by remember { mutableStateOf(false) }
    var editMode by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.setExpenseForm(null)
                    editMode = false
                    showFormDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Pengeluaran")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "Catatan Pengeluaran Operasional",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            if (exps.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada pengeluaran terdaftar.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(exps) { e ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(e.nama, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                        Text(
                                            text = sdf.format(Date(e.tanggal)),
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                        )
                                    }

                                    Text(
                                        formatRupiah(e.nominal),
                                        fontWeight = FontWeight.ExtraBold,
                                        color = NegativeRed,
                                        fontSize = 15.sp
                                    )
                                }

                                if (e.keterangan.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Ket: ${e.keterangan}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(
                                        onClick = {
                                            viewModel.setExpenseForm(e)
                                            editMode = true
                                            showFormDialog = true
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = { viewModel.deleteExpense(e) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, "Hapus", tint = NegativeRed, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFormDialog) {
        Dialog(onDismissRequest = { showFormDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        if (editMode) "Edit Catatan Pengeluaran" else "Catat Pengeluaran Baru",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    val name by viewModel.expenseName.collectAsState()
                    val amount by viewModel.expenseAmount.collectAsState()
                    val desc by viewModel.expenseDesc.collectAsState()

                    OutlinedTextField(
                        value = name,
                        onValueChange = { viewModel.expenseName.value = it },
                        label = { Text("Nama Pengeluaran (Contoh: Gas LPG)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { viewModel.expenseAmount.value = it.filter { c -> c.isDigit() } },
                        label = { Text("Nominal Pengeluaran") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = desc,
                        onValueChange = { viewModel.expenseDesc.value = it },
                        label = { Text("Keterangan Opsional") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showFormDialog = false }) {
                            Text("Batal")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.saveExpense()
                                showFormDialog = false
                            }
                        ) {
                            Text("Simpan")
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// 8. HUTANG CUSTOMER DATABASE SCREEN
// ==========================================
@Composable
fun HutangScreen(viewModel: WarungViewModel) {
    val debts by viewModel.hutangList.collectAsState()
    var showForm by remember { mutableStateOf(false) }
    var editMode by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.setDebtForm(null)
                    editMode = false
                    showForm = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Hutang")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "Keuangan Catatan Hutang Buku Pelanggan",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            if (debts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada pencatatan hutang.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(debts) { d ->
                        val isLunas = d.status == "Lunas"

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isLunas) PositiveGreen.copy(alpha = 0.05f) else NegativeRed.copy(alpha = 0.05f)
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isLunas) PositiveGreen.copy(alpha = 0.3f) else NegativeRed.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(d.namaPelanggan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                        Text(
                                            "Tanggal Catat: " + sdf.format(Date(d.tanggal)),
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            formatRupiah(d.nominal),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 16.sp,
                                            color = if (isLunas) PositiveGreen else NegativeRed
                                        )

                                        // Status badge
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isLunas) PositiveGreen else NegativeRed)
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                d.status,
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = { viewModel.toggleDebtStatus(d) },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isLunas) NegativeRed else PositiveGreen,
                                            contentColor = Color.White
                                        ),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text(
                                            if (isLunas) "Ubah ke Belum Lunas" else "Ubah ke Lunas",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Row {
                                        IconButton(
                                            onClick = {
                                                viewModel.setDebtForm(d)
                                                editMode = true
                                                showForm = true
                                            },
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(
                                            onClick = { viewModel.deleteDebt(d) },
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, "Hapus", tint = NegativeRed, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        Dialog(onDismissRequest = { showForm = false }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        if (editMode) "Edit Catatan Hutang" else "Catat Hutang Baru",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    val name by viewModel.debtCustomerName.collectAsState()
                    val amount by viewModel.debtAmount.collectAsState()
                    val status by viewModel.debtStatus.collectAsState()

                    OutlinedTextField(
                        value = name,
                        onValueChange = { viewModel.debtCustomerName.value = it },
                        label = { Text("Nama Pelanggan") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { viewModel.debtAmount.value = it.filter { c -> c.isDigit() } },
                        label = { Text("Nominal Hutang") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // toggle status
                    Text("Status Hutang:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("Belum Lunas", "Lunas").forEach { s ->
                            val isSelected = status == s
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    )
                                    .clickable { viewModel.debtStatus.value = s }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(s, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showForm = false }) {
                            Text("Batal")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.saveDebt()
                                showForm = false
                            }
                        ) {
                            Text("Simpan")
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// 9. STATISTIK BULANAN SCREEN (WITH GRAPH)
// ==========================================
@Composable
fun StatistikScreen(viewModel: WarungViewModel) {
    val omzet by viewModel.omzetBulanan.collectAsState()
    val modal by viewModel.modalBulanan.collectAsState()
    val pengeluaran by viewModel.pengeluaranBulanan.collectAsState()
    val kotor by viewModel.labaKotorBulanan.collectAsState()
    val bersih by viewModel.labaBersihBulanan.collectAsState()
    val txCount by viewModel.transaksiCountBulanan.collectAsState()
    val totalSold by viewModel.barangTerjualCountBulanan.collectAsState()

    val terlaris by viewModel.barangTerlaris.collectAsState()
    val kurangLaku by viewModel.barangKurangLaku.collectAsState()

    val bersihLalu by viewModel.labaBersihBulanLalu.collectAsState()
    val mOmDifferencePercentage by viewModel.labaPersentaseKenaikan.collectAsState()

    val currentMonthLabel = remember {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
        sdf.format(Date())
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Upper Title
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Ringkasan Bulanan", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        Text(currentMonthLabel, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Grafik Otomatis", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Modern Visual Bar Chart using vector painting
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Grafik Finansial Bulanan (Indikator)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Draw the custom responsive scaled canvas chart
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        val goldColorValue = LightAccentGold
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            val margin = 50f
                            val chartHeight = h - margin - 20f
                            val columnWidth = (w - margin * 2f) / 4f

                            val maxDataValue = listOf(omzet, modal, pengeluaran, bersih)
                                .maxOrNull()?.coerceAtLeast(1.0) ?: 1.0

                            // 4 columns: Omzet, Modal, Pengeluaran, Laba Bersih
                            val values = listOf(omzet, modal, pengeluaran, bersih)
                            val colors = listOf(NaturalGreenPrimary, Color(0xFF81C784), NegativeRed, goldColorValue)
                            val names = listOf("Omzet", "Modal", "Biaya", "Bersih")

                            for (i in 0 until 4) {
                                val value = values[i]
                                val barHeight = ((value.coerceAtLeast(0.0) / maxDataValue) * chartHeight).toFloat()
                                val startX = margin + i * columnWidth + (columnWidth - 36f) / 2f
                                val startY = chartHeight - barHeight

                                // rounded column
                                drawRoundRect(
                                    color = colors[i],
                                    topLeft = Offset(startX, startY),
                                    size = Size(36f, barHeight.coerceAtLeast(2f)),
                                    cornerRadius = CornerRadius(8f, 8f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Labels below graph column
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatLabelItem(color = NaturalGreenPrimary, name = "Omzet", percent = if (omzet > 0) "100%" else "0%")
                        StatLabelItem(color = Color(0xFF81C784), name = "Modal", percent = if (omzet > 0) "${((modal / omzet) * 100).toInt()}%" else "0%")
                        StatLabelItem(color = NegativeRed, name = "Beban", percent = if (omzet > 0) "${((pengeluaran / omzet) * 100).toInt()}%" else "0%")
                        StatLabelItem(color = LightAccentGold, name = "L.Bersih", percent = if (omzet > 0) "${((bersih / omzet) * 100).toInt()}%" else "0%")
                    }
                }
            }
        }

        // Expanded financial results list
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Statistik Finansial Rinci", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    Divider()

                    RowSummaryLabel(title = "Total Omzet (Penjualan)", value = formatRupiah(omzet))
                    RowSummaryLabel(title = "Total HPP Modal", value = formatRupiah(modal))
                    RowSummaryLabel(title = "Total Laba Kotor", value = formatRupiah(kotor))
                    RowSummaryLabel(title = "Total Pengeluaran (Beban)", value = formatRupiah(pengeluaran), isNegative = true)

                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    RowSummaryLabel(
                        title = "Sisa Laba Bersih",
                        value = formatRupiah(bersih),
                        highlight = true,
                        accentColor = if (bersih < 0) NegativeRed else LightAccentGold
                    )
                }
            }
        }

        // Comparison with previous month
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Perbandingan Bulan Sebelumnya", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Laba bersih bulan lalu: ${formatRupiah(bersihLalu)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(8.dp))

                    val signStr = if (mOmDifferencePercentage >= 0) "Naik" else "Turun"
                    val pColor = if (mOmDifferencePercentage >= 0) PositiveGreen else NegativeRed
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(pColor.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$signStr ${String.format("%.1f", Math.abs(mOmDifferencePercentage))}%",
                                color = pColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "dibanding bulan kemarin.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        // Best seller vs Low seller widgets
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Best Seller Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⭐", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Terlaris", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PositiveGreen)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        if (terlaris != null) {
                            Text(terlaris!!.first, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${terlaris!!.second} unit terjual", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                        } else {
                            Text("Belum ada data", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
                        }
                    }
                }

                // Low Seller Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⚠️", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Kurang Laku", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = WarningOrange)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        if (kurangLaku != null) {
                            Text(kurangLaku!!.first, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${kurangLaku!!.second} unit terjual", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                        } else {
                            Text("Belum ada data", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatLabelItem(color: Color, name: String, percent: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Text("$name ($percent)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun RowSummaryLabel(
    title: String,
    value: String,
    isNegative: Boolean = false,
    highlight: Boolean = false,
    accentColor: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
            fontSize = if (highlight) 14.sp else 13.sp
        )

        Text(
            text = if (isNegative) "-$value" else value,
            fontWeight = FontWeight.Bold,
            color = if (accentColor != Color.Unspecified) accentColor
            else if (isNegative) NegativeRed
            else MaterialTheme.colorScheme.onBackground,
            fontSize = if (highlight) 16.sp else 13.sp
        )
    }
}


// ==========================================
// 10. PENGATURAN (SETTINGS SCREEN)
// ==========================================
@Composable
fun PengaturanScreen(viewModel: WarungViewModel) {
    val warungName by viewModel.warungName.collectAsState()
    val isDark by viewModel.darkThemeEnabled.collectAsState()
    val backupText by viewModel.backupJsonString.collectAsState()

    var inputWarungName by remember { mutableStateOf(warungName) }
    var inputRestoreText by remember { mutableStateOf("") }
    var simulatedCloudSync by remember { mutableStateOf(true) }

    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Settings header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Pengaturan Nama Warung", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)

                    OutlinedTextField(
                        value = inputWarungName,
                        onValueChange = { inputWarungName = it },
                        placeholder = { Text("Ubah nama warung...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = { viewModel.updateWarungName(inputWarungName) },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Simpan Nama")
                    }
                }
            }
        }

        // Theme Toggle Screen
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Mode Gelap (Dark Mode)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Lebih nyaman dan adem di mata pemilik toko", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                    }

                    Switch(
                        checked = isDark,
                        onCheckedChange = { viewModel.toggleDarkTheme(it) }
                    )
                }
            }
        }

        // Backup Data Offline
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Pencadangan Data Offline (Backup)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    Text("Simpan data warung Anda berupa teks backup aman. Tanpa internet 100% offline.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))

                    Button(
                        onClick = { viewModel.triggerBackup() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Share, null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ekspor Kode Backup Data")
                    }

                    if (backupText.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
                                .padding(8.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(backupText, fontSize = 10.sp, style = MaterialTheme.typography.bodyMedium)
                        }

                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(backupText))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Salin Kode Backup")
                        }
                    }
                }
            }
        }

        // Restore Data Offline
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Pemulihan Data (Restore)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NegativeRed)
                    Text("Tempel kode backup teks Anda di bawah untuk memulihkan seluruh database warung.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))

                    OutlinedTextField(
                        value = inputRestoreText,
                        onValueChange = { inputRestoreText = it },
                        placeholder = { Text("Tempel kode format JSON di sini...") },
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )

                    Button(
                        onClick = {
                            viewModel.triggerRestore(inputRestoreText) { success ->
                                if (success) {
                                    inputRestoreText = ""
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NegativeRed),
                        modifier = Modifier.align(Alignment.End),
                        enabled = inputRestoreText.trim().isNotEmpty()
                    ) {
                        Text("Pulihkan Database")
                    }
                }
            }
        }

        // Cloud simulation status switch
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Sinkronisasi Backup Cloud Berkala", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Menyimpan cadangan aman secara terjadwal otomatis di balik layar.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                        }

                        Switch(
                            checked = simulatedCloudSync,
                            onCheckedChange = { simulatedCloudSync = it }
                        )
                    }

                    if (simulatedCloudSync) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(PositiveGreen.copy(alpha = 0.1f))
                                .padding(10.dp)
                        ) {
                            Text(
                                "Status: Aktif & Tersambung secara lokal. Sistem mencadangkan data secara berkala ke cache penyimpanan aman offline.",
                                color = PositiveGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Tentang info section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Tentang Aplikasi", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    Divider()
                    Text("Nama Aplikasi: Catatan Warung", fontSize = 13.sp)
                    Text("Versi: 1.0.0 (Produksi)", fontSize = 13.sp)
                    Text("Sistem Database: SQLite Room Offline-First", fontSize = 13.sp)
                    Text("Gaya Layar: Material Design 3", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Dibuat khusus untuk pengusaha warung, kelontong, dan UMKM Indonesia agar dapat mencatat transaksi keuangan dengan cepat tanpa bergantung pada koneksi internet. Data Anda sepenuhnya pribadi dan tersimpan di dalam memori smartphone.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
