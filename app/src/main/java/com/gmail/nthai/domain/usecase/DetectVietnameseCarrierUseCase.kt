package com.gmail.nthai.domain.usecase

object DetectVietnameseCarrierUseCase {

    private const val MAX_CACHE_SIZE = 2000
    private const val NULL_MARKER = "__NULL__"

    private val cache = object : LinkedHashMap<String, String>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, String>?): Boolean {
            return size > MAX_CACHE_SIZE
        }
    }

    private val viettelPrefixes = (32..39).map { "0$it" }.toSet() +
        setOf("086", "096", "097", "098")
    private val vinaPhonePrefixes = (81..85).map { "0$it" }.toSet() +
        setOf("088", "091", "094")
    private val mobiFonePrefixes = setOf(
        "070", "076", "077", "078", "079", "089", "090", "093"
    )
    private val vietnamobilePrefixes = setOf("052", "056", "058", "092")
    private val gmobilePrefixes = setOf("059", "099")

    fun detect(phone: String): String? {
        val cached = synchronized(cache) { cache.get(phone) }
        if (cached != null) {
            return if (cached == NULL_MARKER) null else cached
        }

        var digits = phone.filter(Char::isDigit)
        if (digits.startsWith("84") && digits.length == 11) {
            digits = "0${digits.drop(2)}"
        }
        if (digits.length != 10 || !digits.startsWith("0")) {
            synchronized(cache) { cache.put(phone, NULL_MARKER) }
            return null
        }

        val result = when (digits.take(3)) {
            in viettelPrefixes -> "Viettel"
            in vinaPhonePrefixes -> "VinaPhone"
            in mobiFonePrefixes -> "MobiFone"
            in vietnamobilePrefixes -> "Vietnamobile"
            in gmobilePrefixes -> "Gmobile"
            "055" -> "Wintel"
            "087" -> "iTel"
            else -> null
        }

        val valueToCache = result ?: NULL_MARKER
        synchronized(cache) { cache.put(phone, valueToCache) }
        return result
    }
}
