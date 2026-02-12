package com.vainkop.opspocket.di

import com.russhwolf.settings.Settings
import com.vainkop.opspocket.data.local.AzureAuthManager
import com.vainkop.opspocket.data.local.AzurePreferences
import com.vainkop.opspocket.data.local.SecureApiKeyStorage
import com.vainkop.opspocket.data.local.SecureStorage
import com.vainkop.opspocket.data.remote.AzureAuthApiClient
import com.vainkop.opspocket.data.remote.AzureManagementApiClient
import com.vainkop.opspocket.data.remote.CastAiApiClient
import com.vainkop.opspocket.data.remote.HttpClientFactory
import com.vainkop.opspocket.data.repository.AzureRepositoryImpl
import com.vainkop.opspocket.data.repository.CastAiRepositoryImpl
import com.vainkop.opspocket.domain.repository.AzureRepository
import com.vainkop.opspocket.domain.repository.CastAiRepository
import com.vainkop.opspocket.domain.usecase.GetClusterDetailsUseCase
import com.vainkop.opspocket.domain.usecase.GetClustersUseCase
import com.vainkop.opspocket.domain.usecase.GetSubscriptionsUseCase
import com.vainkop.opspocket.domain.usecase.GetTenantsUseCase
import com.vainkop.opspocket.domain.usecase.GetVirtualMachinesUseCase
import com.vainkop.opspocket.domain.usecase.PerformVmActionUseCase
import com.vainkop.opspocket.domain.usecase.TriggerRebalancingUseCase
import com.vainkop.opspocket.domain.usecase.ValidateApiKeyUseCase
import com.vainkop.opspocket.presentation.apikey.ApiKeyViewModel
import com.vainkop.opspocket.presentation.azureauth.AzureAuthViewModel
import com.vainkop.opspocket.presentation.azuresetup.AzureSetupViewModel
import com.vainkop.opspocket.presentation.clusterdetails.ClusterDetailsViewModel
import com.vainkop.opspocket.presentation.clusterlist.ClusterListViewModel
import com.vainkop.opspocket.presentation.vmdetails.VmDetailsViewModel
import com.vainkop.opspocket.presentation.vmlist.VmListViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val storageModule = module {
    single { SecureApiKeyStorage(get<SecureStorage>()) }
    single { AzurePreferences(get<Settings>()) }
}

val networkModule = module {
    single { AzureAuthApiClient(HttpClientFactory.createAzureAuthClient()) }
    single { AzureAuthManager(get<AzureAuthApiClient>(), get<SecureStorage>()) }
    single {
        CastAiApiClient(HttpClientFactory.createCastAiClient { get<SecureApiKeyStorage>().getApiKey() })
    }
    single {
        AzureManagementApiClient(HttpClientFactory.createAzureManagementClient { get<AzureAuthManager>().getAccessToken() })
    }
}

val repositoryModule = module {
    singleOf(::CastAiRepositoryImpl) bind CastAiRepository::class
    singleOf(::AzureRepositoryImpl) bind AzureRepository::class
}

val useCaseModule = module {
    factory { ValidateApiKeyUseCase(get()) }
    factory { GetClustersUseCase(get()) }
    factory { GetClusterDetailsUseCase(get()) }
    factory { TriggerRebalancingUseCase(get()) }
    factory { GetTenantsUseCase(get()) }
    factory { GetSubscriptionsUseCase(get()) }
    factory { GetVirtualMachinesUseCase(get()) }
    factory { PerformVmActionUseCase(get()) }
}

val viewModelModule = module {
    viewModel { ApiKeyViewModel(get(), get()) }
    viewModel { AzureAuthViewModel(get(), get()) }
    viewModel { params -> AzureSetupViewModel(params.get(), get(), get(), get(), get()) }
    viewModel { ClusterListViewModel(get()) }
    viewModel { params -> ClusterDetailsViewModel(params.get(), get(), get()) }
    viewModel { VmListViewModel(get(), get()) }
    viewModel { params -> VmDetailsViewModel(params.get(), params.get(), params.get(), get(), get()) }
}

val sharedModules = listOf(
    storageModule,
    networkModule,
    repositoryModule,
    useCaseModule,
    viewModelModule,
)
