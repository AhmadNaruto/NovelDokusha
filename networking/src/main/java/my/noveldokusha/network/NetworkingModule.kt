package my.noveldokusha.network

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides networking dependencies.
 */
@InstallIn(SingletonComponent::class)
@Module
abstract class NetworkingModule {

    @Singleton
    @Binds
    abstract fun bindNetworkClient(client: ScraperNetworkClient): NetworkClient
}
