package com.vainkop.opspocket.di

import com.vainkop.opspocket.data.repository.AzureRepositoryImpl
import com.vainkop.opspocket.domain.repository.AzureRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AzureRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAzureRepository(impl: AzureRepositoryImpl): AzureRepository
}
