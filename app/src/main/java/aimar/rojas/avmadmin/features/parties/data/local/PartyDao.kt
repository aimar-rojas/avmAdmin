package aimar.rojas.avmadmin.features.parties.data.local

import aimar.rojas.avmadmin.features.parties.data.local.entities.PartyEntity
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PartyDao {
    @Query("SELECT * FROM parties WHERE isPendingSync = 0 OR syncOperation != 'DELETE'")
    fun getParties(): Flow<List<PartyEntity>>

    @Query("SELECT * FROM parties WHERE partyId = :partyId")
    suspend fun getPartyById(partyId: Int): PartyEntity?

    @Query("SELECT * FROM parties WHERE isPendingSync = 1")
    suspend fun getPendingSyncParties(): List<PartyEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParty(party: PartyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParties(parties: List<PartyEntity>)

    @Query("UPDATE parties SET partyId = :newId, isPendingSync = 0, syncOperation = NULL WHERE partyId = :oldId")
    suspend fun updatePartyIdAndMarkSynced(oldId: Int, newId: Int)

    @Query("DELETE FROM parties WHERE isPendingSync = 0")
    suspend fun clearSyncedParties()

    @Query("SELECT COUNT(*) FROM parties")
    suspend fun getPartyCount(): Int
}
