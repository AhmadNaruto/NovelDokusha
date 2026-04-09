package my.noveldokusha.scraper.sources

import my.noveldokusha.network.NetworkClient
import my.noveldokusha.scraper.R

class AllNovel(
    networkClient: NetworkClient
) : NovelFullLike(
    networkClient = networkClient,
    baseUrlValue = "https://allnovel.org/",
    adDomainString = "allnovel.org"
) {
    override val id = "allnovel"
    override val nameStrId = R.string.source_name_allnovel
}
