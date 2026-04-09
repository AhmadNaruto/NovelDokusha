package my.noveldokusha

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import dagger.hilt.EntryPoints
import dagger.hilt.android.HiltAndroidApp
import my.noveldokusha.di.HiltAppEntryPoint
import my.noveldokusha.network.NetworkClient
import my.noveldokusha.network.ScraperNetworkClient
import my.noveldokusha.tooling.application_workers.setup.PeriodicWorkersInitializer
import timber.log.Timber
import javax.inject.Inject


@HiltAndroidApp
class App : Application(), ImageLoaderFactory, Configuration.Provider {

    @Inject
    lateinit var networkClient: NetworkClient

    @Inject
    lateinit var periodicWorkersInitializer: PeriodicWorkersInitializer

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        periodicWorkersInitializer.init()
    }

    override fun newImageLoader(): ImageLoader = when (val networkClient = networkClient) {
        is ScraperNetworkClient -> ImageLoader
            .Builder(this)
            .okHttpClient(networkClient.client)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .memoryCache(
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25) // 25% of available app memory
                    .build()
            )
            .diskCachePolicy(CachePolicy.ENABLED)
            .diskCache(
                DiskCache.Builder()
                    .directory(cacheDir.resolve("coil_image_cache"))
                    .maxSizePercent(0.02) // 2% of available disk space
                    .maxSizeBytes(50L * 1024 * 1024) // Cap at 50 MB
                    .build()
            )
            .respectCacheHeaders(false) // Use our own caching strategy via Coil disk cache
            .crossfade(true)
            .build()

        else -> ImageLoader(this)
    }

    // WorkManager
    override val workManagerConfiguration: Configuration by lazy {
        val appWorkerFactory = EntryPoints
            .get(this, HiltAppEntryPoint::class.java)
            .workerFactory()

        Configuration.Builder()
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) Log.DEBUG else Log.INFO)
            .setWorkerFactory(appWorkerFactory)
            .build()
    }
}
