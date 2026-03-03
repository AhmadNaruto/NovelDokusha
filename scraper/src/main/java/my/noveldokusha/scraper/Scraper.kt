package my.noveldokusha.scraper

import my.noveldokusha.network.NetworkClient
import my.noveldokusha.scraper.databases.BakaUpdates
import my.noveldokusha.scraper.databases.NovelUpdates
import my.noveldokusha.scraper.sources.AT
import my.noveldokusha.scraper.sources.BacaLightnovel
import my.noveldokusha.scraper.sources.BoxNovel
import my.noveldokusha.scraper.sources.IndoWebnovel
import my.noveldokusha.scraper.sources.MeioNovel
import my.noveldokusha.scraper.sources.MoreNovel
import my.noveldokusha.scraper.sources.NovelBin
import my.noveldokusha.scraper.sources.NovelHall
import my.noveldokusha.scraper.sources.Novelku
import my.noveldokusha.scraper.sources.ReadNovelFull
import my.noveldokusha.scraper.sources.Reddit
import my.noveldokusha.scraper.sources.RoyalRoad
import my.noveldokusha.scraper.sources.Saikai
import my.noveldokusha.scraper.sources.SakuraNovel
import my.noveldokusha.scraper.sources.Sousetsuka
import my.noveldokusha.scraper.sources.WbNovel
import my.noveldokusha.scraper.sources.WuxiaWorld
import my.noveldokusha.scraper.sources.ScribbleHub
import my.noveldokusha.scraper.sources.FreeWebNovel
import my.noveldokusha.scraper.sources.NovelFull
import my.noveldokusha.scraper.sources.AllNovel
import my.noveldokusha.scraper.sources.NovelBinCom
import my.noveldokusha.scraper.sources.ReadMTL
import my.noveldokusha.scraper.sources.NewNovel
import my.noveldokusha.scraper.sources.SonicMTL
import my.noveldokusha.scraper.sources.NoBadNovel
import my.noveldokusha.scraper.sources.FanMTL
import my.noveldokusha.scraper.sources.LNMTL
import my.noveldokusha.scraper.sources.WtrLab
import my.noveldokusha.scraper.sources.Shuba69
import my.noveldokusha.scraper.sources.UuKanshu
import my.noveldokusha.scraper.sources.Ddxss
import my.noveldokusha.scraper.sources.LeYueDu
import my.noveldokusha.scraper.sources.Twkan
import my.noveldokusha.scraper.sources.Ttkan
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central registry for all novel sources and databases.
 * Provides methods to find compatible sources based on URLs.
 */
@Singleton
class Scraper @Inject constructor(
    private val networkClient: NetworkClient
) {
    /**
     * List of all registered databases.
     */
    val databases: Set<DatabaseInterface> = setOf(
        NovelUpdates(networkClient),
        BakaUpdates(networkClient)
    )

    /**
     * @deprecated Use [databases] instead
     */
    @Deprecated("Use databases", ReplaceWith("databases"))
    val databasesList: Set<DatabaseInterface> get() = databases

    /**
     * List of all registered sources.
     */
    val sources: Set<SourceInterface> = setOf(
        ReadNovelFull(networkClient),
        RoyalRoad(networkClient),
        my.noveldokusha.scraper.sources.NovelUpdates(networkClient),
        Reddit(),
        AT(),
        Sousetsuka(),
        Saikai(networkClient),
        BoxNovel(networkClient),
        NovelHall(networkClient),
        WuxiaWorld(networkClient),
        IndoWebnovel(networkClient),
        Shuba69(networkClient),
        UuKanshu(networkClient),
        Ddxss(networkClient),
        LeYueDu(networkClient),
        Twkan(networkClient),
        Ttkan(networkClient),
        BacaLightnovel(networkClient),
        SakuraNovel(networkClient),
        MeioNovel(networkClient),
        MoreNovel(networkClient),
        Novelku(networkClient),
        WbNovel(networkClient),
        NovelBin(networkClient),
        ScribbleHub(networkClient),
        FreeWebNovel(networkClient),
        NovelFull(networkClient),
        AllNovel(networkClient),
        NovelBinCom(networkClient),
        ReadMTL(networkClient),
        NewNovel(networkClient),
        SonicMTL(networkClient),
        NoBadNovel(networkClient),
        FanMTL(networkClient),
        LNMTL(networkClient),
        WtrLab(networkClient),
    )

    /**
     * Sources that support catalog browsing.
     */
    val catalogSources: List<SourceInterface.Catalog> =
        sources.filterIsInstance<SourceInterface.Catalog>()

    /**
     * @deprecated Use [catalogSources] instead
     */
    @Deprecated("Use catalogSources", ReplaceWith("catalogSources"))
    val sourcesCatalogsList: List<SourceInterface.Catalog> get() = catalogSources

    /**
     * Unique languages available across all catalog sources.
     */
    val catalogLanguages: Set<my.noveldokusha.core.LanguageCode> =
        catalogSources.mapNotNull { it.language }.toSet()

    /**
     * @deprecated Use [catalogLanguages] instead
     */
    @Deprecated("Use catalogLanguages", ReplaceWith("catalogLanguages"))
    val sourcesCatalogsLanguagesList: Set<my.noveldokusha.core.LanguageCode> get() = catalogLanguages

    /**
     * Finds a source that matches the given URL based on its base URL.
     */
    fun findSource(url: String): SourceInterface? =
        sources.find { url.isCompatibleWith(it.baseUrl) }

    /**
     * @deprecated Use [findSource] instead
     */
    @Deprecated("Use findSource", ReplaceWith("findSource(url)"))
    fun getCompatibleSource(url: String): SourceInterface? = findSource(url)

    /**
     * Finds a catalog source that matches the given URL.
     */
    fun findCatalogSource(url: String): SourceInterface.Catalog? =
        catalogSources.find { url.isCompatibleWith(it.baseUrl) }

    /**
     * @deprecated Use [findCatalogSource] instead
     */
    @Deprecated("Use findCatalogSource", ReplaceWith("findCatalogSource(url)"))
    fun getCompatibleSourceCatalog(url: String): SourceInterface.Catalog? = findCatalogSource(url)

    /**
     * Finds a database that matches the given URL.
     */
    fun findDatabase(url: String): DatabaseInterface? =
        databases.find { url.isCompatibleWith(it.baseUrl) }

    /**
     * @deprecated Use [findDatabase] instead
     */
    @Deprecated("Use findDatabase", ReplaceWith("findDatabase(url)"))
    fun getCompatibleDatabase(url: String): DatabaseInterface? = findDatabase(url)

    /**
     * Checks if a URL is compatible with a base URL.
     */
    private fun String.isCompatibleWith(baseUrl: String): Boolean {
        val normalizedUrl = removeSuffix("/")
        val normalizedBaseUrl = baseUrl.removeSuffix("/")
        return normalizedUrl == normalizedBaseUrl || normalizedUrl.startsWith("$normalizedBaseUrl/")
    }
}
