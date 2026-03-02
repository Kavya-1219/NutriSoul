package com.simats.nutrisoul.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.simats.nutrisoul.data.AppDatabase
import com.simats.nutrisoul.data.CustomFoodDao
import com.simats.nutrisoul.data.FoodDao
import com.simats.nutrisoul.data.IntakeDao
import com.simats.nutrisoul.data.UserDao
import com.simats.nutrisoul.data.local.StepsDao
import com.simats.nutrisoul.data.network.NutritionApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideFoodDao(database: AppDatabase): FoodDao {
        return database.foodDao()
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun provideIntakeDao(database: AppDatabase): IntakeDao {
        return database.intakeDao()
    }

    @Provides
    fun provideCustomFoodDao(database: AppDatabase): CustomFoodDao {
        return database.customFoodDao()
    }
    
    @Provides
    fun provideStepsDao(database: AppDatabase): StepsDao {
        return database.stepsDao()
    }

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("user_prefs") }
        )
    }

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.nal.usda.gov/fdc/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideNutritionApiService(retrofit: Retrofit): NutritionApiService {
        return retrofit.create(NutritionApiService::class.java)
    }
}
