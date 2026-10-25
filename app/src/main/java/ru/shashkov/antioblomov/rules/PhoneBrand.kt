package ru.shashkov.antioblomov.rules

/**
 * Оболочка Android по марке телефона — от неё зависит, где искать «Автозапуск»
 * и ограничения батареи (NFR-01, FR-08).
 */
enum class PhoneBrand(val title: String) {
    XIAOMI("Xiaomi / Redmi / Poco"),
    SAMSUNG("Samsung"),
    OPPO("OPPO / Realme / OnePlus"),
    HUAWEI("Huawei"),
    HONOR("Honor"),
    VIVO("Vivo / iQOO"),
    TRANSSION("Tecno / Infinix / itel"),
    OTHER("Android");

    companion object {
        /** По android.os.Build.MANUFACTURER и Build.BRAND. */
        fun detect(manufacturer: String?, brand: String?): PhoneBrand {
            val s = "${manufacturer.orEmpty()} ${brand.orEmpty()}".lowercase()
            return when {
                listOf("xiaomi", "redmi", "poco").any { it in s } -> XIAOMI
                "samsung" in s -> SAMSUNG
                listOf("oppo", "realme", "oneplus").any { it in s } -> OPPO
                "honor" in s -> HONOR
                "huawei" in s -> HUAWEI
                listOf("vivo", "iqoo").any { it in s } -> VIVO
                listOf("tecno", "infinix", "itel", "transsion").any { it in s } -> TRANSSION
                else -> OTHER
            }
        }
    }
}
