package com.example.ui

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

sealed class Screen {
    object HomeMenu : Screen()
    object Dashboard : Screen()
    object KelolaBarang : Screen()
    object TransaksiBaru : Screen()
    object Pembayaran : Screen()
    object Laporan : Screen()
    object Pengeluaran : Screen()
    object Hutang : Screen()
    object Statistik : Screen()
    object Pengaturan : Screen()
    object ManajemenData : Screen()
}

class WarungViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = AppRepository(db)
    private val prefs = application.getSharedPreferences("catatan_warung_prefs", Context.MODE_PRIVATE)

    // === Navigation State Stack ===
    private val _navigationStack = MutableStateFlow<List<Screen>>(listOf(Screen.HomeMenu))
    val currentScreen: StateFlow<Screen> = _navigationStack
        .map { it.lastOrNull() ?: Screen.HomeMenu }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Screen.HomeMenu)

    fun navigateTo(screen: Screen) {
        _navigationStack.update { it + screen }
    }

    fun navigateBack(): Boolean {
        var handled = false
        _navigationStack.update {
            if (it.size > 1) {
                handled = true
                it.dropLast(1)
            } else {
                it
            }
        }
        return handled
    }

    // === Store Settings States ===
    private val _warungName = MutableStateFlow(prefs.getString("warung_name", "Warung Saya") ?: "Warung Saya")
    val warungName: StateFlow<String> = _warungName.asStateFlow()

    private val _darkThemeEnabled = MutableStateFlow(prefs.getBoolean("dark_theme", false))
    val darkThemeEnabled: StateFlow<Boolean> = _darkThemeEnabled.asStateFlow()

    fun updateWarungName(newName: String) {
        val trimmed = newName.trim()
        val nameToSave = if (trimmed.isEmpty()) "Warung Saya" else trimmed
        prefs.edit().putString("warung_name", nameToSave).apply()
        _warungName.value = nameToSave
    }

    fun toggleDarkTheme(enabled: Boolean) {
        prefs.edit().putBoolean("dark_theme", enabled).apply()
        _darkThemeEnabled.value = enabled
    }

    // === Database Data Flows ===
    val barangList: StateFlow<List<Barang>> = repository.allBarang
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transaksiList: StateFlow<List<Transaksi>> = repository.allTransaksi
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val detailList: StateFlow<List<TransaksiDetail>> = repository.allTransaksiDetail
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pengeluaranList: StateFlow<List<Pengeluaran>> = repository.allPengeluaran
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hutangList: StateFlow<List<Hutang>> = repository.allHutang
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // === Cashier Shopping Cart State ===
    // Pair: Product to quantity
    private val _cart = MutableStateFlow<List<Pair<Barang, Int>>>(emptyList())
    val cart: StateFlow<List<Pair<Barang, Int>>> = _cart.asStateFlow()

    fun addToCart(barang: Barang, qty: Int) {
        if (qty <= 0) return
        _cart.update { currentCart ->
            val existingIndex = currentCart.indexOfFirst { it.first.id == barang.id }
            if (existingIndex >= 0) {
                val updated = currentCart.toMutableList()
                val oldQty = updated[existingIndex].second
                updated[existingIndex] = Pair(barang, oldQty + qty)
                updated
            } else {
                currentCart + Pair(barang, qty)
            }
        }
    }

    fun updateCartQty(barangId: Int, newQty: Int) {
        _cart.update { currentCart ->
            if (newQty <= 0) {
                currentCart.filter { it.first.id != barangId }
            } else {
                currentCart.map {
                    if (it.first.id == barangId) Pair(it.first, newQty) else it
                }
            }
        }
    }

    fun removeFromCart(barangId: Int) {
        _cart.update { it.filter { item -> item.first.id != barangId } }
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    val cartTotalBelanja: StateFlow<Double> = _cart.map { cartItems ->
        cartItems.sumOf { it.first.hargaJual * it.second }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // === Check-out Pembayaran States ===
    private val _nominalBayarInput = MutableStateFlow("")
    val nominalBayarInput: StateFlow<String> = _nominalBayarInput.asStateFlow()

    fun updateNominalBayar(input: String) {
        _nominalBayarInput.value = input.filter { it.isDigit() }
    }

    fun saveCurrentTransaction(onSuccess: () -> Unit) {
        val total = cartTotalBelanja.value
        val bayar = _nominalBayarInput.value.toDoubleOrNull() ?: 0.0
        val kembalian = bayar - total
        if (bayar < total) {
            Toast.makeText(getApplication(), "Nominal bayar masih kurang!", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            repository.saveTransaksi(
                totalBelanja = total,
                nominalBayar = bayar,
                kembalian = kembalian,
                itemsInCart = _cart.value
            )
            clearCart()
            _nominalBayarInput.value = ""
            Toast.makeText(getApplication(), "Transaksi berhasil disimpan!", Toast.LENGTH_SHORT).show()
            onSuccess()
        }
    }

    // === Inventory Form Inputs ===
    var productFormId = 0
    val productName = MutableStateFlow("")
    val productCategory = MutableStateFlow("")
    val productCostPrice = MutableStateFlow("")
    val productSalePrice = MutableStateFlow("")
    val productStock = MutableStateFlow("")
    val productUnit = MutableStateFlow("Pcs") // Default Pcs

    fun setProductForm(barang: Barang?) {
        if (barang == null) {
            productFormId = 0
            productName.value = ""
            productCategory.value = ""
            productCostPrice.value = ""
            productSalePrice.value = ""
            productStock.value = ""
            productUnit.value = "Pcs"
        } else {
            productFormId = barang.id
            productName.value = barang.nama
            productCategory.value = barang.kategori
            productCostPrice.value = barang.hargaModal.toInt().toString()
            productSalePrice.value = barang.hargaJual.toInt().toString()
            productStock.value = barang.stok.toString()
            productUnit.value = barang.satuan
        }
    }

    fun saveProduct() {
        val name = productName.value.trim()
        val cat = productCategory.value.trim()
        val modal = productCostPrice.value.toDoubleOrNull() ?: 0.0
        val jual = productSalePrice.value.toDoubleOrNull() ?: 0.0
        val stok = productStock.value.toIntOrNull() ?: 0
        val sat = productUnit.value

        if (name.isEmpty()) {
            Toast.makeText(getApplication(), "Nama barang harus diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            val barang = Barang(
                id = productFormId,
                nama = name,
                kategori = if (cat.isEmpty()) "Umum" else cat,
                hargaModal = modal,
                hargaJual = jual,
                stok = stok,
                satuan = sat
            )
            if (productFormId == 0) {
                repository.insertBarang(barang)
                Toast.makeText(getApplication(), "Barang ditambahkan!", Toast.LENGTH_SHORT).show()
            } else {
                repository.updateBarang(barang)
                Toast.makeText(getApplication(), "Barang diupdate!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deleteProduct(barang: Barang) {
        viewModelScope.launch {
            repository.deleteBarang(barang)
            Toast.makeText(getApplication(), "Barang dihapus!", Toast.LENGTH_SHORT).show()
        }
    }

    fun restokProduct(barang: Barang, jumlahRestok: Int) {
        if (jumlahRestok <= 0) return
        viewModelScope.launch {
            repository.restokBarang(barang.id, jumlahRestok)
            Toast.makeText(getApplication(), "Restok ${barang.nama} sukses!", Toast.LENGTH_SHORT).show()
        }
    }

    // === Expense Form Inputs ===
    var expenseFormId = 0
    val expenseName = MutableStateFlow("")
    val expenseAmount = MutableStateFlow("")
    val expenseDesc = MutableStateFlow("")

    fun setExpenseForm(pengeluaran: Pengeluaran?) {
        if (pengeluaran == null) {
            expenseFormId = 0
            expenseName.value = ""
            expenseAmount.value = ""
            expenseDesc.value = ""
        } else {
            expenseFormId = pengeluaran.id
            expenseName.value = pengeluaran.nama
            expenseAmount.value = pengeluaran.nominal.toInt().toString()
            expenseDesc.value = pengeluaran.keterangan
        }
    }

    fun saveExpense() {
        val name = expenseName.value.trim()
        val amount = expenseAmount.value.toDoubleOrNull() ?: 0.0
        val desc = expenseDesc.value.trim()

        if (name.isEmpty() || amount <= 0.0) {
            Toast.makeText(getApplication(), "Nama pengeluaran dan nominal harus diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            val pengeluaran = Pengeluaran(
                id = expenseFormId,
                nama = name,
                nominal = amount,
                tanggal = System.currentTimeMillis(),
                keterangan = desc
            )
            if (expenseFormId == 0) {
                repository.insertPengeluaran(pengeluaran)
                Toast.makeText(getApplication(), "Pengeluaran dicatat!", Toast.LENGTH_SHORT).show()
            } else {
                repository.updatePengeluaran(pengeluaran)
                Toast.makeText(getApplication(), "Pengeluaran diupdate!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deleteExpense(pengeluaran: Pengeluaran) {
        viewModelScope.launch {
            repository.deletePengeluaran(pengeluaran)
            Toast.makeText(getApplication(), "Pengeluaran dihapus!", Toast.LENGTH_SHORT).show()
        }
    }

    // === Debt Form Inputs ===
    var debtFormId = 0
    val debtCustomerName = MutableStateFlow("")
    val debtAmount = MutableStateFlow("")
    val debtStatus = MutableStateFlow("Belum Lunas")

    fun setDebtForm(hutang: Hutang?) {
        if (hutang == null) {
            debtFormId = 0
            debtCustomerName.value = ""
            debtAmount.value = ""
            debtStatus.value = "Belum Lunas"
        } else {
            debtFormId = hutang.id
            debtCustomerName.value = hutang.namaPelanggan
            debtAmount.value = hutang.nominal.toInt().toString()
            debtStatus.value = hutang.status
        }
    }

    fun saveDebt() {
        val name = debtCustomerName.value.trim()
        val amount = debtAmount.value.toDoubleOrNull() ?: 0.0
        val status = debtStatus.value

        if (name.isEmpty() || amount <= 0.0) {
            Toast.makeText(getApplication(), "Nama pelanggan dan nominal harus valid!", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            val hutang = Hutang(
                id = debtFormId,
                namaPelanggan = name,
                nominal = amount,
                tanggal = System.currentTimeMillis(),
                status = status
            )
            if (debtFormId == 0) {
                repository.insertHutang(hutang)
                Toast.makeText(getApplication(), "Hutang dicatat!", Toast.LENGTH_SHORT).show()
            } else {
                repository.updateHutang(hutang)
                Toast.makeText(getApplication(), "Hutang diupdate!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun toggleDebtStatus(hutang: Hutang) {
        val nextStatus = if (hutang.status == "Belum Lunas") "Lunas" else "Belum Lunas"
        viewModelScope.launch {
            repository.updateHutang(hutang.copy(status = nextStatus))
            Toast.makeText(getApplication(), "Status hutang diubah jadi $nextStatus!", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteDebt(hutang: Hutang) {
        viewModelScope.launch {
            repository.deleteHutang(hutang)
            Toast.makeText(getApplication(), "Catatan hutang dihapus!", Toast.LENGTH_SHORT).show()
        }
    }

    // === Backup & Restore Action ===
    private val _backupJsonString = MutableStateFlow("")
    val backupJsonString: StateFlow<String> = _backupJsonString.asStateFlow()

    fun triggerBackup() {
        viewModelScope.launch {
            val json = repository.exportBackupJson()
            _backupJsonString.value = json
            Toast.makeText(getApplication(), "Data berhasil diekspor! Siap dicadangkan.", Toast.LENGTH_SHORT).show()
        }
    }

    fun triggerRestore(jsonString: String, onCompleted: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.restoreBackupJson(jsonString)
            if (success) {
                Toast.makeText(getApplication(), "Pemulihan data berhasil!", Toast.LENGTH_LONG).show()
                onCompleted(true)
            } else {
                Toast.makeText(getApplication(), "Gagal memulihkan! Format berkas tidak cocok.", Toast.LENGTH_LONG).show()
                onCompleted(false)
            }
        }
    }

    // === Management Data (Reset Operations) ===
    fun resetDataHarian() {
        viewModelScope.launch {
            val startToday = getStartOfToday()
            repository.clearDailyData(startToday)
            Toast.makeText(getApplication(), "Laba, Omzet, dan Transaksi hari ini berhasil direset!", Toast.LENGTH_SHORT).show()
        }
    }

    fun resetStokBarang() {
        viewModelScope.launch {
            repository.resetAllStok()
            Toast.makeText(getApplication(), "Seluruh stok produk berhasil direset ke 0!", Toast.LENGTH_SHORT).show()
        }
    }

    fun resetRiwayatPenjualan() {
        viewModelScope.launch {
            repository.clearRiwayatPenjualan()
            Toast.makeText(getApplication(), "Seluruh riwayat transaksi & laporan penjualan berhasil dihapus!", Toast.LENGTH_SHORT).show()
        }
    }

    fun resetSemuaData() {
        viewModelScope.launch {
            repository.clearSemuaData()
            prefs.edit().clear().apply()
            _warungName.value = "Warung Saya"
            _darkThemeEnabled.value = false
            Toast.makeText(getApplication(), "Seluruh data aplikasi telah berhasil dibersihkan total!", Toast.LENGTH_LONG).show()
            _navigationStack.value = listOf(Screen.HomeMenu)
        }
    }

    // File-based Backup & Restore
    fun backupToLocalFile(onFileSaved: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportBackupJson()
            try {
                val file = getApplication<Application>().getExternalFilesDir(null)?.resolve("warung_saya_backup.json")
                if (file != null) {
                    file.writeText(json)
                    onFileSaved(file.absolutePath)
                } else {
                    Toast.makeText(getApplication(), "Gagal mengakses folder penyimpanan lokal!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(getApplication(), "Gagal menyimpan file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun restoreFromLocalFile(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val file = getApplication<Application>().getExternalFilesDir(null)?.resolve("warung_saya_backup.json")
                if (file != null && file.exists()) {
                    val json = file.readText()
                    val success = repository.restoreBackupJson(json)
                    if (success) {
                        onResult(true, "Berhasil memulihkan data dari file backup lokal!")
                    } else {
                        onResult(false, "Format file cadangan tidak cocok / korup.")
                    }
                } else {
                    onResult(false, "Tidak ditemukan file backup lokal 'warung_saya_backup.json' di penyimpanan.")
                }
            } catch (e: Exception) {
                onResult(false, "Error: ${e.message}")
            }
        }
    }

    // === Helper Time Boundaries ===
    private fun getStartOfToday(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getStartOfThisMonth(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getStartOfLastMonth(): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    // === Dashboard Live Computed States ===
    val omzetHariIni: StateFlow<Double> = transaksiList.map { txs ->
        val today = getStartOfToday()
        txs.filter { it.tanggal >= today }.sumOf { it.totalBelanja }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val labaHariIni: StateFlow<Double> = combine(transaksiList, pengeluaranList) { txs, exps ->
        val today = getStartOfToday()
        val grossProfit = txs.filter { it.tanggal >= today }.sumOf { it.totalLaba }
        val expenses = exps.filter { it.tanggal >= today }.sumOf { it.nominal }
        grossProfit - expenses // Net profit affects by expenditure per requirement!
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val transaksiCountHariIni: StateFlow<Int> = transaksiList.map { txs ->
        val today = getStartOfToday()
        txs.filter { it.tanggal >= today }.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalBarangCount: StateFlow<Int> = barangList.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val stokMenipisCount: StateFlow<Int> = barangList.map { list ->
        list.filter { it.stok <= 5 }.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val stokMenipisList: StateFlow<List<Barang>> = barangList.map { list ->
        list.filter { it.stok <= 5 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // === Laporan Harian Live Computed States ===
    val modalHariIni: StateFlow<Double> = transaksiList.map { txs ->
        val today = getStartOfToday()
        txs.filter { it.tanggal >= today }.sumOf { it.totalBelanja - it.totalLaba }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val labaKotorHariIni: StateFlow<Double> = transaksiList.map { txs ->
        val today = getStartOfToday()
        txs.filter { it.tanggal >= today }.sumOf { it.totalLaba }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val pengeluaranHariIni: StateFlow<Double> = pengeluaranList.map { exps ->
        val today = getStartOfToday()
        exps.filter { it.tanggal >= today }.sumOf { it.nominal }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val barangTerjualCountHariIni: StateFlow<Int> = combine(transaksiList, detailList) { txs, details ->
        val today = getStartOfToday()
        val todayTxIds = txs.filter { it.tanggal >= today }.map { it.id }.toSet()
        details.filter { it.transaksiId in todayTxIds }.sumOf { it.jumlah }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // === Monthly Statistics Live Computed States ===
    val omzetBulanan: StateFlow<Double> = transaksiList.map { txs ->
        val monthStart = getStartOfThisMonth()
        txs.filter { it.tanggal >= monthStart }.sumOf { it.totalBelanja }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val modalBulanan: StateFlow<Double> = transaksiList.map { txs ->
        val monthStart = getStartOfThisMonth()
        txs.filter { it.tanggal >= monthStart }.sumOf { it.totalBelanja - it.totalLaba }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val pengeluaranBulanan: StateFlow<Double> = pengeluaranList.map { exps ->
        val monthStart = getStartOfThisMonth()
        exps.filter { it.tanggal >= monthStart }.sumOf { it.nominal }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val labaKotorBulanan: StateFlow<Double> = transaksiList.map { txs ->
        val monthStart = getStartOfThisMonth()
        txs.filter { it.tanggal >= monthStart }.sumOf { it.totalLaba }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val labaBersihBulanan: StateFlow<Double> = combine(labaKotorBulanan, pengeluaranBulanan) { kotor, pengeluaran ->
        kotor - pengeluaran
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val transaksiCountBulanan: StateFlow<Int> = transaksiList.map { txs ->
        val monthStart = getStartOfThisMonth()
        txs.filter { it.tanggal >= monthStart }.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val barangTerjualCountBulanan: StateFlow<Int> = combine(transaksiList, detailList) { txs, details ->
        val monthStart = getStartOfThisMonth()
        val monthTxIds = txs.filter { it.tanggal >= monthStart }.map { it.id }.toSet()
        details.filter { it.transaksiId in monthTxIds }.sumOf { it.jumlah }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Comparison Previous Month
    val labaBersihBulanLalu: StateFlow<Double> = combine(transaksiList, pengeluaranList) { txs, exps ->
        val lastMonth = getStartOfLastMonth()
        val thisMonth = getStartOfThisMonth()
        val lastMonthTxs = txs.filter { it.tanggal >= lastMonth && it.tanggal < thisMonth }
        val lastMonthExps = exps.filter { it.tanggal >= lastMonth && it.tanggal < thisMonth }
        
        val kotorLalu = lastMonthTxs.sumOf { it.totalLaba }
        val pengeluaranLalu = lastMonthExps.sumOf { it.nominal }
        kotorLalu - pengeluaranLalu
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Percentage difference
    val labaPersentaseKenaikan: StateFlow<Double> = combine(labaBersihBulanan, labaBersihBulanLalu) { ini, lalu ->
        if (lalu == 0.0) {
            if (ini > 0.0) 100.0 else 0.0
        } else {
            ((ini - lalu) / lalu) * 100.0
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Best Seller / Low Seller Items This Month
    // Helper state returning Map<String, Int> (ProductName to Quantity)
    val barangRankBulanan: StateFlow<Map<String, Int>> = combine(transaksiList, detailList) { txs, details ->
        val monthStart = getStartOfThisMonth()
        val monthTxIds = txs.filter { it.tanggal >= monthStart }.map { it.id }.toSet()
        val filteredDetails = details.filter { it.transaksiId in monthTxIds }
        
        filteredDetails.groupBy { it.namaBarang }
            .mapValues { entry -> entry.value.sumOf { it.jumlah } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val barangTerlaris: StateFlow<Pair<String, Int>?> = barangRankBulanan.map { map ->
        if (map.isEmpty()) null else map.maxByOrNull { it.value }?.toPair()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val barangKurangLaku: StateFlow<Pair<String, Int>?> = CombineRankLowSeller()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private fun CombineRankLowSeller(): Flow<Pair<String, Int>?> {
        return combine(barangList, barangRankBulanan) { allItems, salesMap ->
            if (allItems.isEmpty()) return@combine null

            // Items that are completely unsold get 0 sales
            val fullMap = allItems.associate { it.nama to 0 }.toMutableMap()
            salesMap.forEach { (name, qty) ->
                if (fullMap.containsKey(name)) {
                    fullMap[name] = qty
                } else {
                    fullMap[name] = qty
                }
            }
            fullMap.minByOrNull { it.value }?.toPair()
        }
    }
}

// === Rupiah formatting helper ===
fun formatRupiah(value: Double): String {
    val formatter = DecimalFormat("#,###")
    formatter.decimalFormatSymbols = formatter.decimalFormatSymbols.apply {
        groupingSeparator = '.'
    }
    return "Rp " + formatter.format(value)
}
