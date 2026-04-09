package my.noveldokusha.scraper.sources

import my.noveldokusha.network.NetworkClient
import my.noveldokusha.scraper.R
import org.jsoup.nodes.Document

class NovelFull(
    networkClient: NetworkClient
) : NovelFullLike(
    networkClient = networkClient,
    baseUrlValue = "https://novelfull.net/",
    adDomainString = "novelfull.net"
) {
    override val id = "novelfull"
    override val nameStrId = R.string.source_name_novelfull
}
