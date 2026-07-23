package aimar.rojas.avmadmin.features.parties.data.local

import aimar.rojas.avmadmin.features.parties.data.local.entities.PartyEntity
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PartyDao {
    @Query("SELECT * FROM parties ORDER BY COALESCE(aliasName, firstName, '') ASC")
    fun getParties(): Flow<List<PartyEntity>>

    @Query("SELECT * FROM parties ORDER BY COALESCE(aliasName, firstName, '') ASC")
    suspend fun getPartiesList(): List<PartyEntity>

    @Query("SELECT * FROM parties WHERE localId = :partyId")
    suspend fun getPartyById(partyId: Int): PartyEntity?

    @Query("SELECT * FROM parties WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getPartyByRemoteId(remoteId: Int): PartyEntity?

    @Query("SELECT * FROM parties WHERE syncState != 'CLEAN' AND syncState != 'CONFLICT' ORDER BY localId ASC")
    suspend fun getPendingSyncParties(): List<PartyEntity>

    @Query("SELECT * FROM parties WHERE syncState != 'CLEAN' ORDER BY localId ASC")
    fun observePendingSyncParties(): Flow<List<PartyEntity>>

    @Query("SELECT COUNT(*) FROM parties WHERE syncState != 'CLEAN'")
    fun observePendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM parties WHERE syncState != 'CLEAN'")
    suspend fun getPendingCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParty(party: PartyEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParties(parties: List<PartyEntity>)

    @Query("SELECT COUNT(*) FROM parties")
    suspend fun getPartyCount(): Int
}
