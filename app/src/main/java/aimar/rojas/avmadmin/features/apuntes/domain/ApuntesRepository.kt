package aimar.rojas.avmadmin.features.apuntes.domain

import aimar.rojas.avmadmin.features.apuntes.domain.model.Apunte

interface ApuntesRepository {
    suspend fun getApuntes(): Result<List<Apunte>>
    suspend fun createApunte(observations: String, details: List<aimar.rojas.avmadmin.features.apuntes.domain.model.ApunteDetail>): Result<Apunte>
}
