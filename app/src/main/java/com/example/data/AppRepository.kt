package com.example.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

class AppRepository(private val db: AppDatabase) {
    private val dao = db.appDao()

    val allBarang: Flow<List<Barang>> = dao.getAllBarang()
    val allTransaksi: Flow<List<Transaksi>> = dao.getAllTransaksi()
    val allTransaksiDetail: Flow<List<TransaksiDetail>> = dao.getAllTransaksiDetail()
    val allPengeluaran: Flow<List<Pengeluaran>> = dao.getAllPengeluaran()
    val allHutang: Flow<List<Hutang>> = dao.getAllHutang()

    fun searchBarang(query: String): Flow<List<Barang>> = dao.searchBarang(query)
    fun getDetailsByTransaksiId(transaksiId: Int): Flow<List<TransaksiDetail>> = dao.getDetailsByTransaksiId(transaksiId)

    suspend fun getBarangById(id: Int): Barang? = dao.getBarangById(id)

    suspend fun insertBarang(barang: Barang) = dao.insertBarang(barang)
    suspend fun updateBarang(barang: Barang) = dao.updateBarang(barang)
    suspend fun deleteBarang(barang: Barang) = dao.deleteBarang(barang)

    suspend fun saveTransaksi(
        totalBelanja: Double,
        nominalBayar: Double,
        kembalian: Double,
        itemsInCart: List<Pair<Barang, Int>>
    ) {
        var totalLaba = 0.0

        db.withTransaction {
            for (cartItem in itemsInCart) {
                val barang = cartItem.first
                val qty = cartItem.second
                val modal = barang.hargaModal
                val jual = barang.hargaJual
                val profitPerItem = jual - modal
                totalLaba += profitPerItem * qty

                // Deduct stock
                val updatedBarang = barang.copy(stok = (barang.stok - qty).coerceAtLeast(0))
                dao.updateBarang(updatedBarang)
            }

            val transaksi = Transaksi(
                tanggal = System.currentTimeMillis(),
                totalBelanja = totalBelanja,
                nominalBayar = nominalBayar,
                kembalian = kembalian,
                totalLaba = totalLaba
            )
            val txId = dao.insertTransaksi(transaksi).toInt()

            for (cartItem in itemsInCart) {
                val barang = cartItem.first
                val qty = cartItem.second
                val detail = TransaksiDetail(
                    transaksiId = txId,
                    barangId = barang.id,
                    namaBarang = barang.nama,
                    hargaJual = barang.hargaJual,
                    hargaModal = barang.hargaModal,
                    jumlah = qty,
                    subtotal = barang.hargaJual * qty
                )
                dao.insertTransaksiDetail(detail)
            }
        }
    }

    suspend fun restokBarang(barangId: Int, jumlahBaru: Int) {
        val barang = dao.getBarangById(barangId)
        if (barang != null) {
            val updated = barang.copy(stok = barang.stok + jumlahBaru)
            dao.updateBarang(updated)
        }
    }

    suspend fun deleteTransaksi(transaksi: Transaksi) = dao.deleteTransaksi(transaksi)

    // === Pengeluaran ===
    suspend fun insertPengeluaran(pengeluaran: Pengeluaran) = dao.insertPengeluaran(pengeluaran)
    suspend fun updatePengeluaran(pengeluaran: Pengeluaran) = dao.updatePengeluaran(pengeluaran)
    suspend fun deletePengeluaran(pengeluaran: Pengeluaran) = dao.deletePengeluaran(pengeluaran)

    // === Hutang ===
    suspend fun insertHutang(hutang: Hutang) = dao.insertHutang(hutang)
    suspend fun updateHutang(hutang: Hutang) = dao.updateHutang(hutang)
    suspend fun deleteHutang(hutang: Hutang) = dao.deleteHutang(hutang)

    // === JSON Backup & Restore (Premium offline) ===
    suspend fun exportBackupJson(): String {
        val barangList = allBarang.first()
        val transaksiList = allTransaksi.first()
        val detailList = allTransaksiDetail.first()
        val pengeluaranList = allPengeluaran.first()
        val hutangList = allHutang.first()

        val backupObj = JSONObject()

        // Barang Array
        val barangArr = JSONArray()
        for (b in barangList) {
            val obj = JSONObject().apply {
                put("id", b.id)
                put("nama", b.nama)
                put("kategori", b.kategori)
                put("hargaModal", b.hargaModal)
                put("hargaJual", b.hargaJual)
                put("stok", b.stok)
                put("satuan", b.satuan)
            }
            barangArr.put(obj)
        }
        backupObj.put("barang", barangArr)

        // Transaksi Array
        val transaksiArr = JSONArray()
        for (t in transaksiList) {
            val obj = JSONObject().apply {
                put("id", t.id)
                put("tanggal", t.tanggal)
                put("totalBelanja", t.totalBelanja)
                put("nominalBayar", t.nominalBayar)
                put("kembalian", t.kembalian)
                put("totalLaba", t.totalLaba)
            }
            transaksiArr.put(obj)
        }
        backupObj.put("transaksi", transaksiArr)

        // TransaksiDetail Array
        val detailArr = JSONArray()
        for (d in detailList) {
            val obj = JSONObject().apply {
                put("id", d.id)
                put("transaksiId", d.transaksiId)
                put("barangId", d.barangId)
                put("namaBarang", d.namaBarang)
                put("hargaJual", d.hargaJual)
                put("hargaModal", d.hargaModal)
                put("jumlah", d.jumlah)
                put("subtotal", d.subtotal)
            }
            detailArr.put(obj)
        }
        backupObj.put("transaksiDetail", detailArr)

        // Pengeluaran Array
        val pengeluaranArr = JSONArray()
        for (p in pengeluaranList) {
            val obj = JSONObject().apply {
                put("id", p.id)
                put("nama", p.nama)
                put("nominal", p.nominal)
                put("tanggal", p.tanggal)
                put("keterangan", p.keterangan)
            }
            pengeluaranArr.put(obj)
        }
        backupObj.put("pengeluaran", pengeluaranArr)

        // Hutang Array
        val hutangArr = JSONArray()
        for (h in hutangList) {
            val obj = JSONObject().apply {
                put("id", h.id)
                put("namaPelanggan", h.namaPelanggan)
                put("nominal", h.nominal)
                put("tanggal", h.tanggal)
                put("status", h.status)
            }
            hutangArr.put(obj)
        }
        backupObj.put("hutang", hutangArr)

        return backupObj.toString(2)
    }

    suspend fun restoreBackupJson(jsonString: String): Boolean {
        return try {
            val backupObj = JSONObject(jsonString)

            db.withTransaction {
                // Clear all tables first
                dao.clearBarang()
                dao.clearTransaksi()
                dao.clearTransaksiDetail()
                dao.clearPengeluaran()
                dao.clearHutang()

                // Restore Barang
                if (backupObj.has("barang")) {
                    val arr = backupObj.getJSONArray("barang")
                    val list = mutableListOf<Barang>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        list.add(
                            Barang(
                                id = obj.optInt("id", 0),
                                nama = obj.getString("nama"),
                                kategori = obj.getString("kategori"),
                                hargaModal = obj.getDouble("hargaModal"),
                                hargaJual = obj.getDouble("hargaJual"),
                                stok = obj.getInt("stok"),
                                satuan = obj.getString("satuan")
                            )
                        )
                    }
                    dao.insertAllBarang(list)
                }

                // Restore Transaksi
                if (backupObj.has("transaksi")) {
                    val arr = backupObj.getJSONArray("transaksi")
                    val list = mutableListOf<Transaksi>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        list.add(
                            Transaksi(
                                id = obj.optInt("id", 0),
                                tanggal = obj.getLong("tanggal"),
                                totalBelanja = obj.getDouble("totalBelanja"),
                                nominalBayar = obj.getDouble("nominalBayar"),
                                kembalian = obj.getDouble("kembalian"),
                                totalLaba = obj.getDouble("totalLaba")
                            )
                        )
                    }
                    dao.insertAllTransaksi(list)
                }

                // Restore TransaksiDetail
                if (backupObj.has("transaksiDetail")) {
                    val arr = backupObj.getJSONArray("transaksiDetail")
                    val list = mutableListOf<TransaksiDetail>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        list.add(
                            TransaksiDetail(
                                id = obj.optInt("id", 0),
                                transaksiId = obj.getInt("transaksiId"),
                                barangId = obj.getInt("barangId"),
                                namaBarang = obj.getString("namaBarang"),
                                hargaJual = obj.getDouble("hargaJual"),
                                hargaModal = obj.getDouble("hargaModal"),
                                jumlah = obj.getInt("jumlah"),
                                subtotal = obj.getDouble("subtotal")
                            )
                        )
                    }
                    dao.insertAllTransaksiDetail(list)
                }

                // Restore Pengeluaran
                if (backupObj.has("pengeluaran")) {
                    val arr = backupObj.getJSONArray("pengeluaran")
                    val list = mutableListOf<Pengeluaran>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        list.add(
                            Pengeluaran(
                                id = obj.optInt("id", 0),
                                nama = obj.getString("nama"),
                                nominal = obj.getDouble("nominal"),
                                tanggal = obj.getLong("tanggal"),
                                keterangan = obj.optString("keterangan", "")
                            )
                        )
                    }
                    dao.insertAllPengeluaran(list)
                }

                // Restore Hutang
                if (backupObj.has("hutang")) {
                    val arr = backupObj.getJSONArray("hutang")
                    val list = mutableListOf<Hutang>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        list.add(
                            Hutang(
                                id = obj.optInt("id", 0),
                                namaPelanggan = obj.getString("namaPelanggan"),
                                nominal = obj.getDouble("nominal"),
                                tanggal = obj.getLong("tanggal"),
                                status = obj.getString("status")
                            )
                        )
                    }
                    dao.insertAllHutang(list)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
