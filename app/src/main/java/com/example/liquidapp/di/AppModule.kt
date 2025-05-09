package com.example.liquidapp.di

import android.content.Context
import com.example.liquidapp.data.LiquidDatabase
import com.example.liquidapp.data.dao.HydrationDao
import com.example.liquidapp.data.repository.HydrationRepository
import com.example.liquidapp.util.PreferenceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Hilt module that provides singleton dependencies.
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LiquidDatabase {
        return LiquidDatabase.getInstance(context)
    }
    
    @Provides
    @Singleton
    fun provideHydrationDao(database: LiquidDatabase): HydrationDao {
        return database.hydrationDao()
    }
    
    @Provides
    @Singleton
    fun providePreferenceManager(@ApplicationContext context: Context): PreferenceManager {
        return PreferenceManager(context)
    }
    
    @Provides
    @Singleton
    fun provideHydrationRepository(
        hydrationDao: HydrationDao,
        preferenceManager: PreferenceManager
    ): HydrationRepository {
        return HydrationRepository(hydrationDao, preferenceManager)
    }
} 