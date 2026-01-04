package com.patrolshield.di

import com.patrolshield.data.repository.AuthRepositoryImpl
import com.patrolshield.data.repository.PatrolRepositoryImpl
import com.patrolshield.data.repository.IncidentRepositoryImpl
import com.patrolshield.domain.repository.AuthRepository
import com.patrolshield.domain.repository.PatrolRepository
import com.patrolshield.domain.repository.IncidentRepository
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
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindPatrolRepository(
        patrolRepositoryImpl: PatrolRepositoryImpl
    ): PatrolRepository

    @Binds
    @Singleton
    abstract fun bindIncidentRepository(
        incidentRepositoryImpl: IncidentRepositoryImpl
    ): IncidentRepository
}
