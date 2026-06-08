package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // === Barang ===
    @Query("SELECT * FROM barang ORDER BY nama ASC")
    fun getAllBarang(): Flow<List<Barang>>

    @Query("SELECT * FROM barang WHERE nama LIKE '%' || :query || '%' OR kategori LIKE '%' || :query || '%'")
    fun searchBarang(query: String): Flow<List<Barang>>

    @Query("SELECT * FROM barang WHERE id = :id LIMIT 1")
    suspend fun getBarangById(id: Int): Barang?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBarang(barang: Barang): Long

    @Update
    suspend fun updateBarang(barang: Barang)

    @Delete
    suspend fun deleteBarang(barang: Barang)

    // === Transaksi ===
    @Query("SELECT * FROM transaksi ORDER BY tanggal DESC")
    fun getAllTransaksi(): Flow<List<Transaksi>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaksi(transaksi: Transaksi): Long

    @Delete
    suspend fun deleteTransaksi(transaksi: Transaksi)

    // === Transaksi Detail ===
    @Query("SELECT * FROM transaksi_detail ORDER BY id DESC")
    fun getAllTransaksiDetail(): Flow<List<TransaksiDetail>>

    @Query("SELECT * FROM transaksi_detail WHERE transaksiId = :transaksiId")
    fun getDetailsByTransaksiId(transaksiId: Int): Flow<List<TransaksiDetail>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaksiDetail(detail: TransaksiDetail)

    // === Pengeluaran ===
    @Query("SELECT * FROM pengeluaran ORDER BY tanggal DESC")
    fun getAllPengeluaran(): Flow<List<Pengeluaran>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPengeluaran(pengeluaran: Pengeluaran): Long

    @Update
    suspend fun updatePengeluaran(pengeluaran: Pengeluaran)

    @Delete
    suspend fun deletePengeluaran(pengeluaran: Pengeluaran)

    // === Hutang ===
    @Query("SELECT * FROM hutang ORDER BY tanggal DESC")
    fun getAllHutang(): Flow<List<Hutang>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHutang(hutang: Hutang): Long

    @Update
    suspend fun updateHutang(hutang: Hutang)

    @Delete
    suspend fun deleteHutang(hutang: Hutang)

    // Bulk inserts during Restore
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBarang(barangList: List<Barang>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTransaksi(transaksiList: List<Transaksi>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTransaksiDetail(detailList: List<TransaksiDetail>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPengeluaran(pengeluaranList: List<Pengeluaran>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllHutang(hutangList: List<Hutang>)

    @Query("DELETE FROM barang")
    suspend fun clearBarang()

    @Query("DELETE FROM transaksi")
    suspend fun clearTransaksi()

    @Query("DELETE FROM transaksi_detail")
    suspend fun clearTransaksiDetail()

    @Query("DELETE FROM pengeluaran")
    suspend fun clearPengeluaran()

    @Query("DELETE FROM hutang")
    suspend fun clearHutang()
}
