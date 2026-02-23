package com.simats.nutrisoul.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserRepository @Inject constructor(private val userDao: UserDao) {

    fun getLatestUser(): Flow<User?> = userDao.getLatestUser()

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    suspend fun insertUser(user: User) {
        userDao.insertUser(user)
    }
}
