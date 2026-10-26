package ru.shashkov.antioblomov.rules.i18n

/** Вкладка «Приложения», названия вкладок, экран блокировки, служба. */
interface AppsStrings {
    // Нижнее меню — короткие: подпись стоит в узкой капсуле рядом с иконкой
    val tabHome: String
    val tabSchedule: String
    val tabTimer: String
    /** Название вкладки и заголовок экрана «Приложения» */
    val tabApps: String

    // Экран «Приложения»
    /** Подсказка под счётчиком, когда что-то выбрано */
    val hint: String
    /** Подсказка, когда не выбрано ничего (янтарная) */
    val hintEmpty: String
    val search: String
    /** Подпись кнопки-крестика в поиске (для TalkBack) */
    val clearSearch: String
    val loading: String
    val notFound: String
    val sectionBlocked: String
    val sectionDistracting: String
    val sectionAll: String
    /** Счётчик: «Ничего не выбрано» */
    val nothingSelected: String
    /** Счётчик: «Заблокировано: 3» */
    fun blockedCount(n: Int): String
    /** Заголовки паузы перед изменением во время блокировки */
    val waitAddTitle: String
    val waitRemoveTitle: String

    // Экран блокировки
    /** «YouTube на паузе» — когда время окончания неизвестно */
    fun paused(app: String): String
    /** «YouTube на паузе до 19:14»; [time] — «19:14», «завтра 01:30», «Чт 10:00» */
    fun pausedUntil(app: String, time: String): String
    /** Если не удалось узнать название приложения */
    val thisApp: String
    /** Кнопка внизу экрана блокировки */
    val backToWork: String
    /** Кавычки вокруг цитаты на экране блокировки: «…» / “…” / „…“ */
    fun quoted(text: String): String
}

object AppsRu : AppsStrings {
    override val tabHome = "Главная"
    override val tabSchedule = "Расписание"
    override val tabTimer = "Таймер"
    override val tabApps = "Приложения"
    override val hint = "Отметь, что блокировать по расписанию и таймеру."
    override val hintEmpty = "Ничего не выбрано — расписание и таймер ничего не блокируют. Отметь, что блокировать."
    override val search = "Поиск"
    override val clearSearch = "Очистить поиск"
    override val loading = "Загружаю список приложений…"
    override val notFound = "Ничего не найдено"
    override val sectionBlocked = "Заблокированные"
    override val sectionDistracting = "Часто отвлекают"
    override val sectionAll = "Все приложения"
    override val nothingSelected = "Ничего не выбрано"
    override fun blockedCount(n: Int) = "Заблокировано: $n"
    override val waitAddTitle = "Добавить приложение в блокировку?"
    override val waitRemoveTitle = "Убрать приложение из блокировки?"
    override fun paused(app: String) = "$app на паузе"
    override fun pausedUntil(app: String, time: String) = "$app на паузе до $time"
    override val thisApp = "Это приложение"
    override val backToWork = "Вернуться к делу"
    override fun quoted(text: String) = "«$text»"
}

object AppsEn : AppsStrings {
    override val tabHome = "Home"
    override val tabSchedule = "Schedule"
    override val tabTimer = "Timer"
    override val tabApps = "Apps"
    override val hint = "Pick what the schedule and timer should block."
    override val hintEmpty = "Nothing selected — the schedule and timer won’t block anything. Pick what to block."
    override val search = "Search"
    override val clearSearch = "Clear search"
    override val loading = "Loading your apps…"
    override val notFound = "Nothing found"
    override val sectionBlocked = "Blocked"
    override val sectionDistracting = "Often distracting"
    override val sectionAll = "All apps"
    override val nothingSelected = "Nothing selected"
    override fun blockedCount(n: Int) = "Blocked: $n"
    override val waitAddTitle = "Start blocking this app?"
    override val waitRemoveTitle = "Stop blocking this app?"
    override fun paused(app: String) = "$app is paused"
    override fun pausedUntil(app: String, time: String) = "$app is paused until $time"
    override val thisApp = "This app"
    override val backToWork = "Back to work"
    override fun quoted(text: String) = "“$text”"
}

object AppsEs : AppsStrings {
    override val tabHome = "Inicio"
    override val tabSchedule = "Horario"
    override val tabTimer = "Temporizador"
    override val tabApps = "Apps"
    override val hint = "Elige qué bloquear con el horario y el temporizador."
    override val hintEmpty = "No hay nada seleccionado: el horario y el temporizador no bloquean nada. Elige qué bloquear."
    override val search = "Buscar"
    override val clearSearch = "Borrar búsqueda"
    override val loading = "Cargando aplicaciones…"
    override val notFound = "No se encontró nada"
    override val sectionBlocked = "Bloqueadas"
    override val sectionDistracting = "Suelen distraer"
    override val sectionAll = "Todas las apps"
    override val nothingSelected = "Nada seleccionado"
    override fun blockedCount(n: Int) = "Bloqueadas: $n"
    override val waitAddTitle = "¿Bloquear esta app?"
    override val waitRemoveTitle = "¿Dejar de bloquear esta app?"
    override fun paused(app: String) = "$app en pausa"
    override fun pausedUntil(app: String, time: String) = "$app en pausa hasta $time"
    override val thisApp = "Esta app"
    override val backToWork = "Volver a la tarea"
    override fun quoted(text: String) = "«$text»"
}

object AppsPt : AppsStrings {
    override val tabHome = "Início"
    override val tabSchedule = "Agenda"
    override val tabTimer = "Timer"
    override val tabApps = "Apps"
    override val hint = "Escolha o que bloquear na agenda e no timer."
    override val hintEmpty = "Nada selecionado — a agenda e o timer não bloqueiam nada. Escolha o que bloquear."
    override val search = "Pesquisar"
    override val clearSearch = "Limpar pesquisa"
    override val loading = "Carregando apps…"
    override val notFound = "Nada encontrado"
    override val sectionBlocked = "Bloqueados"
    override val sectionDistracting = "Costumam distrair"
    override val sectionAll = "Todos os apps"
    override val nothingSelected = "Nada selecionado"
    override fun blockedCount(n: Int) = "Bloqueados: $n"
    override val waitAddTitle = "Bloquear este app?"
    override val waitRemoveTitle = "Desbloquear este app?"
    override fun paused(app: String) = "$app pausado"
    override fun pausedUntil(app: String, time: String) = "$app pausado até $time"
    override val thisApp = "Este app"
    override val backToWork = "Voltar ao foco"
    override fun quoted(text: String) = "“$text”"
}

object AppsDe : AppsStrings {
    override val tabHome = "Start"
    override val tabSchedule = "Zeitplan"
    override val tabTimer = "Timer"
    override val tabApps = "Apps"
    override val hint = "Wähle, was Zeitplan und Timer sperren sollen."
    override val hintEmpty = "Nichts ausgewählt – Zeitplan und Timer sperren nichts. Wähle, was gesperrt werden soll."
    override val search = "Suchen"
    override val clearSearch = "Suche löschen"
    override val loading = "Apps werden geladen…"
    override val notFound = "Nichts gefunden"
    override val sectionBlocked = "Gesperrt"
    override val sectionDistracting = "Lenken oft ab"
    override val sectionAll = "Alle Apps"
    override val nothingSelected = "Nichts ausgewählt"
    override fun blockedCount(n: Int) = "Gesperrt: $n"
    override val waitAddTitle = "Diese App sperren?"
    override val waitRemoveTitle = "Sperre für diese App aufheben?"
    override fun paused(app: String) = "$app pausiert"
    override fun pausedUntil(app: String, time: String) = "$app pausiert bis $time"
    override val thisApp = "Diese App"
    override val backToWork = "Zurück an die Arbeit"
    override fun quoted(text: String) = "„$text“"
}

object AppsTr : AppsStrings {
    override val tabHome = "Ana sayfa"
    override val tabSchedule = "Program"
    override val tabTimer = "Zamanlayıcı"
    override val tabApps = "Uygulamalar"
    override val hint = "Program ve zamanlayıcıyla neyin engelleneceğini seç."
    override val hintEmpty = "Hiçbir şey seçilmedi — program ve zamanlayıcı hiçbir şeyi engellemiyor. Neyin engelleneceğini seç."
    override val search = "Ara"
    override val clearSearch = "Aramayı temizle"
    override val loading = "Uygulamalar yükleniyor…"
    override val notFound = "Hiçbir şey bulunamadı"
    override val sectionBlocked = "Engellenenler"
    override val sectionDistracting = "Sık dikkat dağıtanlar"
    override val sectionAll = "Tüm uygulamalar"
    override val nothingSelected = "Hiçbir şey seçilmedi"
    override fun blockedCount(n: Int) = "Engellenen: $n"
    override val waitAddTitle = "Bu uygulama engellensin mi?"
    override val waitRemoveTitle = "Bu uygulamanın engeli kaldırılsın mı?"
    override fun paused(app: String) = "$app duraklatıldı"
    // «19:14'e kadar» — ek saate göre değişir, bu yüzden «bitiş:»
    override fun pausedUntil(app: String, time: String) = "$app duraklatıldı · bitiş: $time"
    override val thisApp = "Bu uygulama"
    override val backToWork = "İşine dön"
    override fun quoted(text: String) = "“$text”"
}

object AppsKk : AppsStrings {
    override val tabHome = "Басты бет"
    override val tabSchedule = "Кесте"
    override val tabTimer = "Таймер"
    override val tabApps = "Қосымшалар"
    override val hint = "Кесте мен таймер бойынша нені бұғаттау керегін белгіле."
    override val hintEmpty = "Ештеңе таңдалмаған — кесте мен таймер ештеңені бұғаттамайды. Нені бұғаттау керегін белгіле."
    override val search = "Іздеу"
    override val clearSearch = "Іздеуді тазалау"
    override val loading = "Қосымшалар тізімі жүктелуде…"
    override val notFound = "Ештеңе табылмады"
    override val sectionBlocked = "Бұғатталғандар"
    override val sectionDistracting = "Жиі алаңдататындар"
    override val sectionAll = "Барлық қосымшалар"
    override val nothingSelected = "Ештеңе таңдалмаған"
    override fun blockedCount(n: Int) = "Бұғатталған: $n"
    override val waitAddTitle = "Қосымшаны бұғаттауға қосу керек пе?"
    override val waitRemoveTitle = "Қосымшаны бұғаттаудан алып тастау керек пе?"
    override fun paused(app: String) = "$app үзілісте"
    // «19:14-ке дейін» — жалғау уақытқа қарай өзгереді, сондықтан «аяқталуы:»
    override fun pausedUntil(app: String, time: String) = "$app үзілісте · аяқталуы: $time"
    override val thisApp = "Бұл қосымша"
    override val backToWork = "Іске оралу"
    override fun quoted(text: String) = "«$text»"
}

object AppsUz : AppsStrings {
    override val tabHome = "Bosh sahifa"
    override val tabSchedule = "Jadval"
    override val tabTimer = "Taymer"
    override val tabApps = "Ilovalar"
    override val hint = "Jadval va taymer bo‘yicha nimani bloklashni belgila."
    override val hintEmpty = "Hech narsa tanlanmagan — jadval va taymer hech narsani bloklamaydi. Nimani bloklashni belgila."
    override val search = "Qidirish"
    override val clearSearch = "Qidiruvni tozalash"
    override val loading = "Ilovalar ro‘yxati yuklanmoqda…"
    override val notFound = "Hech narsa topilmadi"
    override val sectionBlocked = "Bloklanganlar"
    override val sectionDistracting = "Ko‘p chalg‘itadiganlar"
    override val sectionAll = "Barcha ilovalar"
    override val nothingSelected = "Hech narsa tanlanmagan"
    override fun blockedCount(n: Int) = "Bloklangan: $n"
    override val waitAddTitle = "Ilova bloklansinmi?"
    override val waitRemoveTitle = "Ilova blokdan chiqarilsinmi?"
    override fun paused(app: String) = "$app pauzada"
    override fun pausedUntil(app: String, time: String) = "$app $time gacha pauzada"
    override val thisApp = "Bu ilova"
    override val backToWork = "Ishga qaytish"
    override fun quoted(text: String) = "“$text”"
}

object AppsZh : AppsStrings {
    override val tabHome = "首页"
    override val tabSchedule = "日程"
    override val tabTimer = "计时器"
    override val tabApps = "应用"
    override val hint = "选择日程和计时器要屏蔽的应用。"
    override val hintEmpty = "未选择任何应用——日程和计时器不会屏蔽任何内容。选择要屏蔽的应用。"
    override val search = "搜索"
    override val clearSearch = "清除搜索"
    override val loading = "正在加载应用列表…"
    override val notFound = "未找到结果"
    override val sectionBlocked = "已屏蔽"
    override val sectionDistracting = "常让人分心"
    override val sectionAll = "全部应用"
    override val nothingSelected = "未选择任何应用"
    override fun blockedCount(n: Int) = "已屏蔽：$n"
    override val waitAddTitle = "要屏蔽此应用吗？"
    override val waitRemoveTitle = "要取消屏蔽此应用吗？"
    override fun paused(app: String) = "$app 已暂停"
    override fun pausedUntil(app: String, time: String) = "$app 暂停至 $time"
    override val thisApp = "此应用"
    override val backToWork = "回到正事"
    override fun quoted(text: String) = "“$text”"
}
