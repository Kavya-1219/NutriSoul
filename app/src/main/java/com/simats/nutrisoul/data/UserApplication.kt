package com.simats.nutrisoul.data

import android.app.Application

class UserApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { UserRepository(database.userDao()) }
}
