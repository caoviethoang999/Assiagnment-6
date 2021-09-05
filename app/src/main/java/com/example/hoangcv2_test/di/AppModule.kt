package com.example.hoangcv2_test.di

import android.app.Application
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.example.hoangcv2_test.database.RunningDatabase
import com.example.hoangcv2_test.other.Constrants.KEY_FIRST_TIME_TOGGLE
import com.example.hoangcv2_test.other.Constrants.KEY_NAME
import com.example.hoangcv2_test.other.Constrants.KEY_WEIGHT
import com.example.hoangcv2_test.other.Constrants.RUNNING_DATABASE_NAME
import com.example.hoangcv2_test.other.Constrants.SHARED_PREFERENCES_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module //apply function for application
@InstallIn(ApplicationComponent::class) //where dependencies created and when its get destroyed
object AppModule {

    @Singleton //single instance running
    @Provides
    fun provideRunningDatabase(@ApplicationContext app:Context) = Room.databaseBuilder(app, RunningDatabase::class.java, RUNNING_DATABASE_NAME).build()

    @Singleton
    @Provides
    fun provideRunDao(database: RunningDatabase)=database.getRunDao()

    @Singleton
    @Provides
    //references to require value
    fun provideSharedPreferences(@ApplicationContext app: Context) =
        app.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)

    @Singleton
    @Provides
    fun provideName(sharedPref: SharedPreferences) = sharedPref.getString(KEY_NAME, "") ?: ""

    @Singleton
    @Provides
    fun provideWeight(sharedPref: SharedPreferences) = sharedPref.getFloat(KEY_WEIGHT, 80f)

    @Singleton
    @Provides
    fun provideFirstTimeToggle(sharedPref: SharedPreferences) =
        sharedPref.getBoolean(KEY_FIRST_TIME_TOGGLE, true)
}