package com.simats.nutrisoul.di

import android.content.Context
import com.simats.nutrisoul.data.AppDatabase
import com.simats.nutrisoul.data.CustomFoodDao
import com.simats.nutrisoul.data.FoodRepository
import com.simats.nutrisoul.data.IntakeDao
import com.simats.nutrisoul.data.UserDao
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
    @Singleton
    fun provideCustomFoodDao(appDatabase: AppDatabase): CustomFoodDao {
        return appDatabase.customFoodDao()
    }

    @Provides
    @Singleton
    fun provideUserDao(appDatabase: AppDatabase): UserDao {
        return appDatabase.userDao()
    }

    @Provides
    @Singleton
    fun provideIntakeDao(appDatabase: AppDatabase): IntakeDao {
        return appDatabase.intakeDao()
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

    @Provides
    @Singleton
    fun provideFoodRepository(customFoodDao: CustomFoodDao, nutritionApiService: NutritionApiService, intakeDao: IntakeDao): FoodRepository {
        return FoodRepository(customFoodDao, nutritionApiService, intakeDao)
    }
}
