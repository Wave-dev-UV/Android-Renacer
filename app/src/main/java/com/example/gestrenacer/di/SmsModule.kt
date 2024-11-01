package com.example.gestrenacer.di

import com.example.gestrenacer.utils.Constants.URL_SMS_API
import com.example.gestrenacer.webservices.SmsService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SmsModule {
    @Singleton
    @Provides
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(URL_SMS_API)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun provideSmsService(retrofit: Retrofit): SmsService {
        return retrofit.create(SmsService::class.java)
    }
}