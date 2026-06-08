package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "barang")
data class Barang(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nama: String,
    val kategori: String,
    val hargaModal: Double,
    val hargaJual: Double,
    val stok: Int,
    val satuan: String
)

@Entity(tableName = "transaksi")
data class Transaksi(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tanggal: Long,
    val totalBelanja: Double,
    val nominalBayar: Double,
    val kembalian: Double,
    val totalLaba: Double
)

@Entity(tableName = "transaksi_detail")
data class TransaksiDetail(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val transaksiId: Int,
    val barangId: Int,
    val namaBarang: String,
    val hargaJual: Double,
    val hargaModal: Double,
    val jumlah: Int,
    val subtotal: Double
)

@Entity(tableName = "pengeluaran")
data class Pengeluaran(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nama: String,
    val nominal: Double,
    val tanggal: Long,
    val keterangan: String
)

@Entity(tableName = "hutang")
data class Hutang(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val namaPelanggan: String,
    val nominal: Double,
    val tanggal: Long,
    val status: String // "Belum Lunas", "Lunas"
)
