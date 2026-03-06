package com.simats.nutrisoul.data

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.simats.nutrisoul.settings.SettingsStore
import com.simats.nutrisoul.steps.StepTrackingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    application: Application,
    private val sessionManager: SessionManager,
    private val settingsStore: SettingsStore
) : AndroidViewModel(application) {

    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _automaticTracking = MutableStateFlow(false)
    val automaticTracking = _automaticTracking.asStateFlow()

    // Professional: Use SettingsStore as the single source of truth for app-wide settings
    val darkMode = sessionManager.currentUserEmailFlow().flatMapLatest { email ->
        settingsStore.observe(email ?: "").map { it.darkMode }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val profilePictureUri = sessionManager.currentUserEmailFlow().flatMapLatest { email ->
        settingsStore.observe(email ?: "").map { it.profilePictureUri }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userName = sessionManager.currentUserEmailFlow().flatMapLatest { email ->
        settingsStore.observe(email ?: "").map { it.userName }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "User")

    private val auth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

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
                        
                        val email = auth.currentUser?.email
                        if (email != null) {
                            viewModelScope.launch {
                                // Sync Firestore data to local SettingsStore
                                settingsStore.setDarkMode(email, document.getBoolean("darkMode") ?: false)
                                if (user.name.isNotBlank()) {
                                    settingsStore.setUserName(email, user.name)
                                }
                                val photo = document.getString("profilePictureUri")
                                if (photo != null) {
                                    settingsStore.setProfilePicture(email, Uri.parse(photo))
                                }
                            }
                        }
                        
                        _isLoading.value = false

                        // Migration check for dietaryRestrictions
                        val restrictionsField = document.get("dietaryRestrictions")
                        if (restrictionsField is String) {
                            val updatedRestrictions = if (restrictionsField.isBlank()) emptyList() else listOf(restrictionsField)
                            docRef.update("dietaryRestrictions", updatedRestrictions)
                        }

                    } catch (e: Exception) {
                        Log.e("UserViewModel", "Error mapping user document", e)
                        _isLoading.value = false
                    }
                } else {
                    _user.value = User()
                    _isLoading.value = false
                }
            }
            .addOnFailureListener { e ->
                Log.e("UserViewModel", "Failed to load user data", e)
                _isLoading.value = false
            }
    }

    private fun mapDocumentToUser(doc: DocumentSnapshot): User {
        val dietaryRestrictionsRaw = doc.get("dietaryRestrictions")
        val dietaryRestrictions = when (dietaryRestrictionsRaw) {
            is String -> if (dietaryRestrictionsRaw.isBlank()) emptyList() else listOf(dietaryRestrictionsRaw)
            is List<*> -> dietaryRestrictionsRaw.mapNotNull { it as? String }
            else -> emptyList()
        }

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
            targetWeeks = getInt("targetWeeks"),
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
        if (uid == null) return

        _user.value = updatedUser
        db.collection("users").document(uid).set(updatedUser)
            .addOnFailureListener { e -> Log.e("UserViewModel", "Failed to update user", e) }

        // ✅ Sync name to SettingsStore (Figma Step 3)
        if (updatedUser.name.isNotBlank()) {
            viewModelScope.launch {
                // Prefer sessionManager (more stable), fallback to Firebase email
                val email = sessionManager.currentUserEmailFlow().firstOrNull()
                    ?: auth.currentUser?.email
                    ?: return@launch

                settingsStore.setUserName(email, updatedUser.name)
            }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        val uid = auth.currentUser?.uid
        val email = auth.currentUser?.email
        if (uid != null && email != null) {
            viewModelScope.launch {
                settingsStore.setDarkMode(email, enabled)
                db.collection("users").document(uid).update("darkMode", enabled)
            }
        }
    }

    fun setProfilePictureUri(uri: Uri) {
        val uid = auth.currentUser?.uid
        val email = auth.currentUser?.email
        if (uid != null && email != null) {
            viewModelScope.launch {
                settingsStore.setProfilePicture(email, uri)
                db.collection("users").document(uid).update("profilePictureUri", uri.toString())
            }
        }
    }

    fun updateGoal(goal: String) {
        _user.value?.let { currentUser -> updateUser(currentUser.copy(goal = goal)) }
    }

    fun updateTargetWeight(weight: Float, weeks: Int) {
        _user.value?.let { currentUser -> updateUser(currentUser.copy(targetWeight = weight, targetWeeks = weeks)) }
    }

    fun updateCurrentWeight(weight: Float) {
        _user.value?.let { currentUser -> updateUser(currentUser.copy(currentWeight = weight)) }
    }

    fun addCalories(calories: Int) {
        _user.value?.let { currentUser ->
            updateUser(currentUser.copy(todaysCalories = currentUser.todaysCalories + calories))
        }
    }

    fun updateWaterIntake(amount: Int) {
        _user.value?.let { currentUser ->
            updateUser(currentUser.copy(todaysWaterIntake = (currentUser.todaysWaterIntake + amount).coerceAtLeast(0)))
        }
    }

    fun updateSteps(steps: Int) {
        _user.value?.let { currentUser ->
            updateUser(currentUser.copy(todaysSteps = (currentUser.todaysSteps + steps).coerceAtLeast(0)))
        }
    }

    fun setAutomaticTracking(enabled: Boolean) {
        _automaticTracking.value = enabled
    }

    fun updateStepsFromSensor(steps: Int) {
        _user.value?.let { currentUser -> updateUser(currentUser.copy(todaysSteps = steps)) }
    }

    fun logout() {
        auth.signOut()
    }

    fun retryLoadUserData() {
        auth.currentUser?.uid?.let { loadUserData(it, Source.SERVER) }
    }
}
