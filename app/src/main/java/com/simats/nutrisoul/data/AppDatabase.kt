package com.simats.nutrisoul.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [FoodItem::class, LoggedFood::class, User::class, IntakeEntity::class, CustomFoodEntity::class, StepsEntity::class], version = 5, exportSchema = false)
@TypeConverters(DateConverter::class, Converters::class, DateConverters::class, StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun foodDao(): FoodDao
    abstract fun userDao(): UserDao
    abstract fun intakeDao(): IntakeDao
    abstract fun customFoodDao(): CustomFoodDao
    abstract fun stepsDao(): StepsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nutrisoul_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
