package com.vainkop.opspocket.domain.usecase

import com.vainkop.opspocket.domain.model.AppResult
import com.vainkop.opspocket.domain.repository.CastAiRepository
import javax.inject.Inject

class ValidateApiKeyUseCase @Inject constructor(
    private val repository: CastAiRepository,
) {
    suspend operator fun invoke(): AppResult<Unit> = when (val result = repository.getClusters()) {
        is AppResult.Success -> AppResult.Success(Unit)
        is AppResult.Error -> when (result.code) {
            401 -> AppResult.Error("Invalid API key. Please check your key and try again.", 401)
            403 -> AppResult.Error("Insufficient permissions. The API key lacks required access.", 403)
            else -> result
        }
        is AppResult.NetworkError -> AppResult.NetworkError(result.throwable)
    }
}
