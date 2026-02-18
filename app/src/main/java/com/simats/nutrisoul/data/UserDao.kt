package com.simats.nutrisoul.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM user_profile WHERE id = :id")
    fun getUserById(id: Int): Flow<User?>

    @Query("SELECT * FROM user_profile ORDER BY lastLogin DESC LIMIT 1")
    fun getLatestUser(): Flow<User?>
}
