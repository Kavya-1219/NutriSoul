package com.simats.personalisednutritionapp.data

import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {

    fun getLatestUser(): Flow<User?> = userDao.getLatestUser()

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    suspend fun insertUser(user: User) {
        userDao.insertUser(user)
    }
}
