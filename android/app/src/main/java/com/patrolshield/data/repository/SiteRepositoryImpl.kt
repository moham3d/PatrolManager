package com.patrolshield.data.repository

import com.patrolshield.data.remote.ApiService
import com.patrolshield.data.remote.dto.CreateSiteRequest
import com.patrolshield.data.remote.dto.SiteDto
import com.patrolshield.domain.repository.SiteRepository
import javax.inject.Inject

class SiteRepositoryImpl @Inject constructor(
    private val api: ApiService
) : SiteRepository {

    override suspend fun getSites(): Result<List<SiteDto>> {
        return try {
            val response = api.getSites() // Reusing existing endpoint
            if (response.isSuccessful && response.body() != null) {
                // Determine if body is List or Wrapper. Based on shiftController it wraps? 
                // Wait, clockInViewModel uses getSites which returns SiteListResponse or List<SiteDto>?
                // Let's check ApiService again or just assume SiteListResponse if defined elsewhere.
                // Assuming getSites returns SiteListResponse based on previous viewing.
                Result.success(response.body()!!.sites)
            } else {
                Result.failure(Exception("Failed to fetch sites: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createSite(name: String, address: String, lat: Double, lng: Double): Result<SiteDto> {
        return try {
            val request = CreateSiteRequest(name, address, lat, lng)
            val response = api.createSite(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to create site: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
