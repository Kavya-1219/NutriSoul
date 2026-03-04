package com.simats.nutrisoul.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [FoodItemEntity::class, LoggedFood::class, User::class, IntakeEntity::class, CustomFoodEntity::class, StepsEntity::class, FoodLogEntity::class], version = 8, exportSchema = false)
@TypeConverters(DateConverter::class, Converters::class, DateConverters::class, StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun foodDao(): FoodDao
    abstract fun userDao(): UserDao
    abstract fun intakeDao(): IntakeDao
    abstract fun customFoodDao(): CustomFoodDao
    abstract fun stepsDao(): StepsDao
    abstract fun foodLogDao(): FoodLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS food_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userEmail TEXT NOT NULL,
                        name TEXT NOT NULL,
                        caloriesPerUnit INTEGER NOT NULL,
                        proteinPerUnit INTEGER NOT NULL,
                        carbsPerUnit INTEGER NOT NULL,
                        fatsPerUnit INTEGER NOT NULL,
                        quantity REAL NOT NULL,
                        unit TEXT NOT NULL,
                        timestampMillis INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_food_logs_userEmail_timestampMillis ON food_logs(userEmail, timestampMillis)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nutrisoul_database"
                )
                .addMigrations(MIGRATION_6_7)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
