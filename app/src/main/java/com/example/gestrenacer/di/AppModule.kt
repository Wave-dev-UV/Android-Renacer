package com.example.gestrenacer.di

import android.content.Context
import com.example.gestrenacer.repository.UserRepositorio
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideUserRepositorio(@ApplicationContext context: Context): UserRepositorio {
        return UserRepositorio(context)
    }
}
