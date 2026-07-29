package aimar.rojas.avmadmin.features.workers.data.local

import aimar.rojas.avmadmin.features.workers.data.local.entities.WorkerEntity
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkerDao {
    @Query("SELECT * FROM workers ORDER BY isActive DESC, fullName ASC")
    fun observeWorkers(): Flow<List<WorkerEntity>>

    @Query("SELECT * FROM workers WHERE isActive = 1 ORDER BY fullName ASC")
    fun observeActiveWorkers(): Flow<List<WorkerEntity>>

    @Query("SELECT * FROM workers ORDER BY isActive DESC, fullName ASC")
    suspend fun getWorkersList(): List<WorkerEntity>

    @Query("SELECT * FROM workers WHERE localId = :workerId LIMIT 1")
    suspend fun getWorkerById(workerId: Int): WorkerEntity?

    @Query("SELECT * FROM workers WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getWorkerByRemoteId(remoteId: Int): WorkerEntity?

    @Query("SELECT * FROM workers WHERE syncState != 'CLEAN' AND syncState != 'CONFLICT' ORDER BY localId ASC")
    suspend fun getPendingSyncWorkers(): List<WorkerEntity>

    @Query("SELECT * FROM workers WHERE syncState != 'CLEAN' ORDER BY localId ASC")
    fun observePendingSyncWorkers(): Flow<List<WorkerEntity>>

    @Query("SELECT COUNT(*) FROM workers WHERE syncState != 'CLEAN'")
    fun observePendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM workers WHERE syncState != 'CLEAN'")
    suspend fun getPendingCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorker(worker: WorkerEntity): Long
}
