package com.simats.nutrisoul.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.simats.nutrisoul.data.*
import com.simats.nutrisoul.data.network.NutritionApiService
import com.simats.nutrisoul.data.network.UserApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
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
    fun provideFoodLogDao(database: AppDatabase): FoodLogDao {
        return database.foodLogDao()
    }

    @Provides
    fun provideSleepDao(database: AppDatabase): SleepDao {
        return database.sleepDao()
    }

    @Provides
    @Singleton
    fun provideSessionManager(@ApplicationContext context: Context): SessionManager {
        return SessionManager(context)
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
    @Named("NutritionRetrofit")
    fun provideNutritionRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.nal.usda.gov/fdc/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("DjangoRetrofit")
    fun provideDjangoRetrofit(sessionManager: SessionManager): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                sessionManager.getToken()?.let {
                    request.addHeader("Authorization", "Bearer $it")
                }
                chain.proceed(request.build())
            }
            .build()

        return Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/") // Default Django dev server URL for emulator
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideNutritionApiService(@Named("NutritionRetrofit") retrofit: Retrofit): NutritionApiService {
        return retrofit.create(NutritionApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideUserApi(@Named("DjangoRetrofit") retrofit: Retrofit): UserApi {
        return retrofit.create(UserApi::class.java)
    }
}
