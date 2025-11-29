package my.noveldokusha

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import coil3.ImageLoader                     // ✅ Coil 3
import coil3.disk.DiskCache                  // ✅ Coil 3
import coil3.memory.MemoryCache              // ✅ Coil 3
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.util.IoDispatcher
import dagger.hilt.EntryPoints
import dagger.hilt.android.HiltAndroidApp
import my.noveldokusha.di.HiltAppEntryPoint
import my.noveldokusha.network.ScraperNetworkClient
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), coil3.ImageLoader.Factory, Configuration.Provider { // ✅ Coil 3 interface

    @Inject
    lateinit var periodicWorkersInitializer: PeriodicWorkersInitializer

    @Inject
    lateinit var networkClient: ScraperNetworkClient

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            // Aktifkan logger Coil 3 untuk debug (opsional)
            // coil3.util.Logger.setLevel(coil3.util.Logger.Level.Debug)
        }
        periodicWorkersInitializer.init()
    }

    /**
     * FIXED: ImageLoader.Builder() tanpa context.
     * Coil 3 mengambil context dari ImageView/CompositionLocal secara otomatis.
     */
    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder() // ✅ HAPUS "this"
            .components {
                // Registrasi OkHttp fetcher
                add(
                    OkHttpNetworkFetcherFactory(
                        callFactory = networkClient.client // Reuse client dari ScraperNetworkClient
                    )
                )
            }
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(0.05) // 5% RAM = efisien untuk thumbnail novel
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(200L * 1024 * 1024) // 200 MB
                    .cleanupDispatcher(IoDispatcher()) // I/O di background thread
                    .build()
            }
            .respectCacheHeaders(true) // Patuhi Cache-Control header
            .build()

    override val workManagerConfiguration: Configuration by lazy {
        val factory = EntryPoints.get(this, HiltAppEntryPoint::class.java).workerFactory()
        Configuration.Builder()
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) Log.DEBUG else Log.INFO)
            .setWorkerFactory(factory)
            .build()
    }
}
