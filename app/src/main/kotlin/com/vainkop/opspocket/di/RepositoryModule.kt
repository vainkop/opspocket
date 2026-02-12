package com.vainkop.opspocket.di

import com.vainkop.opspocket.data.repository.CastAiRepositoryImpl
import com.vainkop.opspocket.domain.repository.CastAiRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCastAiRepository(impl: CastAiRepositoryImpl): CastAiRepository
}
