package com.patrolshield.di

import com.patrolshield.data.repository.AuthRepositoryImpl
import com.patrolshield.data.repository.IncidentRepositoryImpl
import com.patrolshield.data.repository.NotificationRepositoryImpl
import com.patrolshield.data.repository.PatrolRepositoryImpl
import com.patrolshield.domain.repository.AuthRepository
import com.patrolshield.domain.repository.IncidentRepository
import com.patrolshield.domain.repository.NotificationRepository
import com.patrolshield.domain.repository.PatrolRepository
import com.patrolshield.data.repository.SupervisorRepositoryImpl
import com.patrolshield.domain.repository.SupervisorRepository
import com.patrolshield.data.repository.ManagerRepositoryImpl
import com.patrolshield.domain.repository.ManagerRepository
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

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        notificationRepositoryImpl: NotificationRepositoryImpl
    ): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindSupervisorRepository(
        supervisorRepositoryImpl: SupervisorRepositoryImpl
    ): SupervisorRepository

    @Binds
    @Singleton
    abstract fun bindManagerRepository(
        managerRepositoryImpl: ManagerRepositoryImpl
    ): ManagerRepository
}
