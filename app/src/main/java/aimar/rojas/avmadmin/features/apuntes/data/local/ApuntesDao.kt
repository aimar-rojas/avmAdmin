package aimar.rojas.avmadmin.features.apuntes.data.local

import aimar.rojas.avmadmin.features.apuntes.data.local.entities.ApunteDetailEntity
import aimar.rojas.avmadmin.features.apuntes.data.local.entities.ApunteEntity
import aimar.rojas.avmadmin.features.apuntes.data.local.entities.ApunteWithDetails
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ApuntesDao {
    @Transaction
    @Query("SELECT * FROM apuntes ORDER BY recordDate DESC, localId DESC")
    suspend fun getApuntesWithDetails(): List<ApunteWithDetails>

    @Transaction
    @Query("SELECT * FROM apuntes ORDER BY recordDate DESC, localId DESC")
    fun observeApuntesWithDetails(): Flow<List<ApunteWithDetails>>

    @Transaction
    @Query("SELECT * FROM apuntes WHERE localId = :localId LIMIT 1")
    suspend fun getApunteWithDetailsById(localId: Int): ApunteWithDetails?

    @Transaction
    @Query("SELECT * FROM apuntes WHERE syncState != 'CLEAN' ORDER BY localId ASC")
    fun observePendingApuntes(): Flow<List<ApunteWithDetails>>

    @Transaction
    @Query("SELECT * FROM apuntes WHERE syncState != 'CLEAN' AND syncState != 'CONFLICT' ORDER BY localId ASC")
    suspend fun getPendingSyncApuntes(): List<ApunteWithDetails>

    @Query("SELECT COUNT(*) FROM apuntes WHERE syncState != 'CLEAN'")
    fun observePendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM apuntes WHERE syncState != 'CLEAN'")
    suspend fun getPendingCount(): Int

    @Query("SELECT * FROM apuntes WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getApunteByRemoteId(remoteId: Int): ApunteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApunte(apunte: ApunteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetails(details: List<ApunteDetailEntity>)

    @Query("DELETE FROM apunte_details WHERE apunteLocalId = :apunteLocalId")
    suspend fun deleteDetailsByApunteId(apunteLocalId: Int)

    @Transaction
    suspend fun insertRecordWithDetails(
        apunte: ApunteEntity,
        details: List<ApunteDetailEntity>
    ): Int {
        val localId = insertApunte(apunte).toInt()
        insertDetails(details.map { it.copy(apunteLocalId = localId) })
        return localId
    }

    @Transaction
    suspend fun replaceRecordWithDetails(
        apunte: ApunteEntity,
        details: List<ApunteDetailEntity>
    ) {
        val localId = insertApunte(apunte).toInt()
        deleteDetailsByApunteId(localId)
        insertDetails(details.map { it.copy(apunteLocalId = localId) })
    }
}
