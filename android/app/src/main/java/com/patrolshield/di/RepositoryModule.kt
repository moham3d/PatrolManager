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
import com.patrolshield.data.repository.VisitorRepositoryImpl
import com.patrolshield.domain.repository.VisitorRepository
import com.patrolshield.data.repository.ShiftRepositoryImpl
import com.patrolshield.domain.repository.ShiftRepository
import com.patrolshield.data.repository.AdminRepositoryImpl
import com.patrolshield.domain.repository.AdminRepository
import com.patrolshield.data.repository.UserRepositoryImpl
import com.patrolshield.domain.repository.UserRepository
import com.patrolshield.data.repository.SiteRepositoryImpl
import com.patrolshield.domain.repository.SiteRepository
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

    @Binds
    @Singleton
    abstract fun bindVisitorRepository(
        visitorRepositoryImpl: VisitorRepositoryImpl
    ): VisitorRepository

    @Binds
    @Singleton
    abstract fun bindShiftRepository(
        shiftRepositoryImpl: ShiftRepositoryImpl
    ): ShiftRepository

    @Binds
    @Singleton
    abstract fun bindAdminRepository(
        adminRepositoryImpl: AdminRepositoryImpl
    ): AdminRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindSiteRepository(
        siteRepositoryImpl: SiteRepositoryImpl
    ): SiteRepository
}
