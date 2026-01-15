package aimar.rojas.avmadmin.domain.repository

import aimar.rojas.avmadmin.domain.model.User

interface UserRepository {
    suspend fun getUserMe(): User
}