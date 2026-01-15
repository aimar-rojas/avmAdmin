package aimar.rojas.avmadmin.domain.repository

import aimar.rojas.avmadmin.domain.model.Party

interface PartyRepository {
    suspend fun getPartyBuyers(): List<Party>
    suspend fun getPartyBuyerById(id: Int): Party? = null
    suspend fun  addPartyBuyer(buyer: Party)
    suspend fun editPartyBuyer(buyer: Party)
    suspend fun getPartyProducers(): List<Party>
    suspend fun getPartyProducerById(id: Int): Party? = null
    suspend fun  addPartyProducer(producer: Party)
    suspend fun editPartyProducer(producer: Party)
}