package ru.shashkov.antioblomov.rules.i18n

/**
 * Языки интерфейса (v1.2). Порядок — как в списке выбора.
 * [nativeName] — название на самом языке: так его узнают при любом текущем языке.
 */
enum class Lang(val code: String, val nativeName: String, val shortCode: String) {
    RU("ru", "Русский", "RU"),
    EN("en", "English", "EN"),
    ES("es", "Español", "ES"),
    PT("pt", "Português", "PT"),
    DE("de", "Deutsch", "DE"),
    TR("tr", "Türkçe", "TR"),
    KK("kk", "Қазақша", "KK"),
    UZ("uz", "O‘zbekcha", "UZ"),
    ZH("zh", "中文", "中文");

    companion object {
        fun of(code: String?): Lang? = entries.firstOrNull { it.code == code }

        /**
         * Выбранный пользователем язык; если не выбирал — язык телефона, если он есть в списке;
         * иначе английский. Участники эксперимента с русским телефоном останутся на русском.
         */
        fun resolve(saved: String?, systemLanguage: String?): Lang = of(saved) ?: of(systemLanguage) ?: EN
    }
}
