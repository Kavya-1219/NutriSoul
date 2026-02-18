package com.simats.nutrisoul

import android.app.Application
import com.simats.nutrisoul.data.AppDatabase
import com.simats.nutrisoul.data.UserRepository

class MyApplication : Application() {
    // Using by lazy so the database and repository are only created when they're needed
    // rather than when the application starts
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { UserRepository(database.userDao()) }
}
