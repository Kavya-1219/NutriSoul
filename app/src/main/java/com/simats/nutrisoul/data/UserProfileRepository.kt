package com.simats.nutrisoul.data

import com.simats.nutrisoul.data.models.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface UserProfileRepository {
    fun getUserProfile(): Flow<UserProfile>
}

class UserProfileRepositoryImpl : UserProfileRepository {
    // For now, we'll use a static profile. In a real app, this would come from a database or network.
    private val userProfile = MutableStateFlow(
        UserProfile(
            age = 30,
            gender = "Male",
            height = 180.0,
            weight = 75.0,
            activityLevel = "Slightly Active",
            goal = "Maintain"
        )
    )

    override fun getUserProfile(): Flow<UserProfile> = userProfile
}
