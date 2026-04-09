package my.noveldokusha.scraper.sources

import my.noveldokusha.network.NetworkClient
import my.noveldokusha.scraper.R

class NewNovel(
    networkClient: NetworkClient
) : NovelFullLike(
    networkClient = networkClient,
    baseUrlValue = "https://newnovel.org/",
    adDomainString = "newnovel.org",
    catalogUrlValue = "https://novlove.com/sort/nov-love-daily-update"
) {
    override val id = "newnovel"
    override val nameStrId = R.string.source_name_newnovel
}
