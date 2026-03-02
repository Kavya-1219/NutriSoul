package com.simats.nutrisoul.data

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.simats.nutrisoul.steps.StepTrackingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {

    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _automaticTracking = MutableStateFlow(false)
    val automaticTracking = _automaticTracking.asStateFlow()

    private val _darkMode = MutableStateFlow(false)
    val darkMode = _darkMode.asStateFlow()

    private val _profilePictureUri = MutableStateFlow<Uri?>(null)
    val profilePictureUri = _profilePictureUri.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore
    private val sessionManager = SessionManager(application)

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                runBlocking { sessionManager.setCurrentUser(firebaseUser.email!!) }
                loadUserData(firebaseUser.uid, Source.DEFAULT)
            } else {
                _user.value = null
                _isLoading.value = false
                runBlocking { sessionManager.clearCurrentUser() }
                getApplication<Application>().stopService(Intent(getApplication(), StepTrackingService::class.java))
            }
        }
    }

    private fun loadUserData(uid: String, source: Source) {
        _isLoading.value = true
        val docRef = db.collection("users").document(uid)
        docRef.get(source)
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    try {
                        val user = mapDocumentToUser(document)
                        _user.value = user
                        _darkMode.value = document.getBoolean("darkMode") ?: false
                        val profilePicUriString = document.getString("profilePictureUri")
                        if (profilePicUriString != null) {
                            _profilePictureUri.value = Uri.parse(profilePicUriString)
                        }
                        _isLoading.value = false

                        // After successfully loading, check if a migration is needed and perform it.
                        val restrictionsField = document.get("dietaryRestrictions")
                        if (restrictionsField is String) {
                            val updatedRestrictions = if (restrictionsField.isBlank()) emptyList() else listOf(restrictionsField)
                            docRef.update("dietaryRestrictions", updatedRestrictions)
                                .addOnFailureListener { e ->
                                    Log.e("UserViewModel", "Failed to update dietaryRestrictions", e)
                                }
                        }

                    } catch (e: Exception) {
                        Log.e("UserViewModel", "Error mapping user document", e)
                        _isLoading.value = false
                        _user.value = null
                    }
                } else {
                    // Document doesn't exist, create a new user profile
                    _user.value = User()
                    _isLoading.value = false
                }
            }
            .addOnFailureListener { e ->
                Log.e("UserViewModel", "Failed to load user data", e)
                _isLoading.value = false
                _user.value = null // Indicate an error state
            }
    }

    private fun mapDocumentToUser(doc: DocumentSnapshot): User {
        // Safely extract and convert dietaryRestrictions
        val dietaryRestrictionsRaw = doc.get("dietaryRestrictions")
        val dietaryRestrictions = when (dietaryRestrictionsRaw) {
            is String -> if (dietaryRestrictionsRaw.isBlank()) emptyList() else listOf(dietaryRestrictionsRaw)
            is List<*> -> dietaryRestrictionsRaw.mapNotNull { it as? String }
            else -> emptyList()
        }

        // Helper function for safe number conversion
        fun getFloat(field: String): Float = (doc.get(field) as? Number)?.toFloat() ?: 0f
        fun getInt(field: String): Int = (doc.get(field) as? Number)?.toInt() ?: 0

        return User(
            id = getInt("id"),
            name = doc.getString("name") ?: "",
            age = getInt("age"),
            gender = doc.getString("gender") ?: "",
            height = getFloat("height"),
            weight = getFloat("weight"),
            activityLevel = doc.getString("activityLevel") ?: "",
            goal = doc.getString("goal") ?: "",
            targetWeight = getFloat("targetWeight"),
            currentWeight = getFloat("currentWeight"),
            mealsPerDay = getInt("mealsPerDay"),
            healthConditions = (doc.get("healthConditions") as? List<*>)?.mapNotNull { it.toString() } ?: emptyList(),
            todaysCalories = getInt("todaysCalories"),
            todaysWaterIntake = getInt("todaysWaterIntake"),
            todaysSteps = getInt("todaysSteps"),
            bmr = getInt("bmr"),
            lastLogin = doc.getTimestamp("lastLogin") ?: Timestamp.now(),
            allergies = (doc.get("allergies") as? List<*>)?.mapNotNull { it.toString() } ?: emptyList(),
            foodAllergies = (doc.get("foodAllergies") as? List<*>)?.mapNotNull { it.toString() } ?: emptyList(),
            cholesterolLevel = doc.getString("cholesterolLevel") ?: "",
            password = doc.getString("password") ?: "",
            dietaryRestrictions = dietaryRestrictions,
            otherAllergies = doc.getString("otherAllergies") ?: "",
            email = doc.getString("email") ?: "",
            dislikes = (doc.get("dislikes") as? List<*>)?.mapNotNull { it.toString() } ?: emptyList(),
            diabetesType = doc.getString("diabetesType") ?: "",
            diastolic = getInt("diastolic"),
            systolic = getInt("systolic"),
            thyroidCondition = doc.getString("thyroidCondition") ?: "",
            targetCalories = getInt("targetCalories")
        )
    }

    fun updateUser(updatedUser: User) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            _user.value = updatedUser
            db.collection("users").document(uid).set(updatedUser)
        }
    }

    fun retryLoadUserData() {
        auth.currentUser?.uid?.let {
            loadUserData(it, Source.SERVER)
        }
    }

    fun updateGoal(goal: String) {
        _user.value?.let { currentUser ->
            updateUser(currentUser.copy(goal = goal))
        }
    }

    fun updateTargetWeight(weight: Float) {
        _user.value?.let { currentUser ->
            updateUser(currentUser.copy(targetWeight = weight))
        }
    }

    fun updateCurrentWeight(weight: Float) {
        _user.value?.let { currentUser ->
            updateUser(currentUser.copy(currentWeight = weight))
        }
    }

    fun addCalories(calories: Int) {
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

    fun setDarkMode(enabled: Boolean) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            _darkMode.value = enabled
            db.collection("users").document(uid).update("darkMode", enabled)
        }
    }

    fun setProfilePictureUri(uri: Uri) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            _profilePictureUri.value = uri
            db.collection("users").document(uid).update("profilePictureUri", uri.toString())
        }
    }

    fun logout() {
        auth.signOut()
    }
}
