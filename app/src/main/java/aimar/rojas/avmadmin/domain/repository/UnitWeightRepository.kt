package aimar.rojas.avmadmin.domain.repository

import aimar.rojas.avmadmin.domain.model.UnitWeight

interface UnitWeightRepository {
    suspend fun getUnitWeights(): List<UnitWeight>
    suspend fun saveUnitWeights()
    suspend fun editUnitWeightById(UnitWeight: UnitWeight)
}