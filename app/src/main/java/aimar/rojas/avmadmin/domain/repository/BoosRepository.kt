package aimar.rojas.avmadmin.domain.repository

import aimar.rojas.avmadmin.domain.model.Boss

interface BoosRepository {
    suspend fun getBossById(id: Int): Boss? = null
}