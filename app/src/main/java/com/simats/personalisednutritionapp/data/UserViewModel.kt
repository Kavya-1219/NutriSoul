package com.simats.personalisednutritionapp.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserViewModel(private val repository: UserRepository) : ViewModel() {

    private val _user = MutableStateFlow(User())
    val user = _user.asStateFlow()

    var goalType by mutableStateOf("")
    var currentWeight by mutableStateOf(0.0)
    var targetWeight by mutableStateOf(0.0)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getLatestUser().firstOrNull()?.let { user ->
                withContext(Dispatchers.Main) {
                    _user.value = user
                    goalType = user.goal
                    currentWeight = user.currentWeight
                    targetWeight = user.targetWeight
                }
            }
        }
    }

    fun updateUser(updatedUser: User) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateUser(updatedUser)
            withContext(Dispatchers.Main) {
                _user.value = updatedUser
                goalType = updatedUser.goal
                currentWeight = updatedUser.currentWeight
                targetWeight = updatedUser.targetWeight
            }
        }
    }

    fun updateGoal(goal: String) {
        goalType = goal
        updateUser(_user.value.copy(goal = goal))
    }

    fun updateTargetWeight(weight: Double) {
        targetWeight = weight
        updateUser(_user.value.copy(targetWeight = weight))
    }

    fun updateCurrentWeight(weight: Double) {
        currentWeight = weight
        updateUser(_user.value.copy(currentWeight = weight))
    }

    fun addCalories(calories: Double) {
        val currentCalories = _user.value.todaysCalories
        updateUser(_user.value.copy(todaysCalories = currentCalories + calories))
    }

    fun calculateTargetCalories() {
        // Placeholder for calorie calculation logic
    }
}
