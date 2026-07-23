package aimar.rojas.avmadmin.features.apuntes.domain

import aimar.rojas.avmadmin.features.apuntes.domain.model.Apunte
import kotlinx.coroutines.flow.Flow

interface ApuntesRepository {
    fun observeApuntes(): Flow<List<Apunte>>
    suspend fun getApuntes(): Result<List<Apunte>>
    suspend fun getApunteById(localId: Int): Result<Apunte>
    suspend fun createApunte(observations: String, details: List<aimar.rojas.avmadmin.features.apuntes.domain.model.ApunteDetail>): Result<Apunte>
    suspend fun updateApunte(localId: Int, observations: String, details: List<aimar.rojas.avmadmin.features.apuntes.domain.model.ApunteDetail>): Result<Apunte>
}
