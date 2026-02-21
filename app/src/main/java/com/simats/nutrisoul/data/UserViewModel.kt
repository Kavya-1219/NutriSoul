package com.simats.nutrisoul.data

import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserViewModel(private val repository: UserRepository, private val context: Context) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _automaticTracking = MutableStateFlow(false)
    val automaticTracking = _automaticTracking.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

    init {
        _automaticTracking.value = false

        auth.addAuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                loadUserData(firebaseUser.uid)
            } else {
                _user.value = null
                _isLoading.value = false
            }
        }
    }

    private fun loadUserData(uid: String) {
        _isLoading.value = true
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    _user.value = document.toObject<User>()
                } else {
                    _user.value = User() // New user, start with default data
                }
                _isLoading.value = false
            }
            .addOnFailureListener {
                _isLoading.value = false
                _user.value = null // Indicate an error state
            }
    }

    fun updateUser(updatedUser: User) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            _user.value = updatedUser
            db.collection("users").document(uid).set(updatedUser)
        }
    }

    fun updateGoal(goal: String) {
        _user.value?.let { currentUser ->
            updateUser(currentUser.copy(goal = goal))
        }
    }

    fun updateTargetWeight(weight: Double) {
        _user.value?.let { currentUser ->
            updateUser(currentUser.copy(targetWeight = weight))
        }
    }

    fun updateCurrentWeight(weight: Double) {
        _user.value?.let { currentUser ->
            updateUser(currentUser.copy(currentWeight = weight))
        }
    }

    fun addCalories(calories: Double) {
        _user.value?.let { currentUser ->
            val currentCalories = currentUser.todaysCalories
            updateUser(currentUser.copy(todaysCalories = currentCalories + calories))
        }
    }

    fun updateWaterIntake(amount: Int) {
        _user.value?.let { currentUser ->
            val newIntake = (currentUser.todaysWaterIntake + amount).coerceAtLeast(0)
            updateUser(currentUser.copy(todaysWaterIntake = newIntake))
        }
    }

    fun updateSteps(steps: Int) {
        _user.value?.let { currentUser ->
            val newSteps = (currentUser.todaysSteps + steps).coerceAtLeast(0)
            updateUser(currentUser.copy(todaysSteps = newSteps))
        }
    }

    fun setAutomaticTracking(enabled: Boolean) {
        _automaticTracking.value = enabled
    }

    fun updateStepsFromSensor(steps: Int) {
        _user.value?.let { currentUser ->
            updateUser(currentUser.copy(todaysSteps = steps))
        }
    }

    fun calculateTargetCalories() {
        // Placeholder for calorie calculation logic
    }
}
