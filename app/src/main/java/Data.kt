import androidx.annotation.Keep

@Keep
data class Data(
    var id : String? = "" ,
    var authors: List<String>? = emptyList() ,
    var contentLength: Int? =0 ,
    var date: String? = "",
    var excerpt: String?= "",
    var keywords: List<String>? = emptyList() ,
    var language: String?= "" ,
    var paywall: Boolean? = false ,
    var publisher: Publisher? = Publisher(),
    var thumbnail: String? = "",
    var title: String? = "",
    var url: String? = "",
    var time: Int? = 0
)