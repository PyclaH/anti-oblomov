package ru.shashkov.antioblomov.rules.i18n

import java.util.Locale

/** Главная: кольцо, «Было / стало», благодарность, блок «Система», выбор языка в шапке. */
interface HomeStrings {
    // Шапка и предупреждения
    /** Название в шапке главной. */
    val appName: String
    /** Для описания кольца (озвучка): «…. Служба блокировки не работает» */
    val serviceDownShort: String
    val serviceDownWarning: String
    val noAppsWarning: String

    // Кольцо и статус (HomeStatusText)
    val freeTime: String
    /** «Блокировка до 14:00»; [at] — из TimeText.at: «14:00», «завтра 01:30», «Чт 10:00». */
    fun blockedUntil(at: String): String
    val bySchedule: String
    val byTimer: String
    val byScheduleAndTimer: String
    val noBlocksScheduled: String
    /**
     * «следующая в 16:00», «следующая завтра в 09:00», «следующая в Чт 10:00».
     * [day] — «», «завтра», «Чт» или «02.11»; [tomorrow] — day означает «завтра».
     */
    fun nextBlock(day: String, time: String, tomorrow: Boolean): String
    /** «ИДЁТ БЛОКИРОВКА» */
    val ringBlocked: String
    /** «СВОБОДНОЕ ВРЕМЯ» */
    val ringFree: String
    /** «до конца» — в строке «до конца · по расписанию», когда блокировка кончится сегодня. */
    val untilEnd: String
    /** «следующая блокировка» */
    val nextBlockLabel: String
    /** «следующая» — в строке «следующая · завтра» */
    val nextShort: String

    // «Было / стало» (StatsCard)
    val statsTitle: String
    val collapseAppList: String
    val statsAppsTitle: String
    val accessExplain: String
    val enableAccess: String
    val collecting: String
    val chooseAppsHint: String
    val tileBefore: String
    val tileAfter: String
    val tileMore: String
    val tileSaved: String
    /** «Всего ты вернул себе 5 ч · 1 учебный день»; [schoolDays] null — без второй части. */
    fun totalSaved(time: String, schoolDays: String?): String
    val howWeCount: String
    /** «Свернуть» */
    val collapse: String
    /** «Подробнее» (со стрелкой « ›» — в «Было / стало») */
    val details: String
    /** «До» — в среднем по [days] до [date]. (с пробелом в конце) */
    fun beforeExplain(days: String, date: String): String
    /** Истории до [date] нет. (с пробелом в конце) */
    fun noHistory(date: String): String
    val afterFirstDay: String
    fun afterExplain(days: String): String
    val screenBefore: String
    val screenAfter: String
    /** «ВЕХА · МЕСЯЦ» */
    fun milestoneOverline(title: String): String
    /** Названия вех по порядку: неделя, две недели, 1…12 месяцев (14 штук). */
    val milestoneTitles: List<String>
    fun milestoneGood(days: String, perDay: String, total: String): String
    fun milestoneBad(days: String, perDay: String): String

    // Приложения для статистики (StatsAppsPicker)
    val appsAllExceptSystem: String
    val appsNone: String
    fun appsAsBlockList(n: Int): String
    fun appsChosen(n: Int): String
    val activateAll: String
    val activateAllSub: String
    val searchApps: String
    val clearSearch: String
    val loadingApps: String
    val nothingChosen: String
    val nothingFound: String

    // «Поблагодарить автора»
    val noBrowser: String
    val thankAuthor: String
    fun youSave(time: String): String
    val freeNoAds: String
    /** «Поддержать ›» */
    val support: String

    // Блок «Система» и служба
    val system: String
    val serviceNotWorking: String
    val serviceTitle: String
    val serviceRunning: String
    val serviceRunningBody: String
    val accessibilitySettings: String
    val serviceStopped: String
    /** [label] — название службы в спецвозможностях; [autostart] — пункт автозапуска для марки или null. */
    fun serviceStoppedBody(label: String, autostart: String?): String
    val accessibilityShort: String
    val appSettings: String
    val serviceOff: String
    fun serviceOffBody(label: String): String
    val openSettings: String
}

object HomeRu : HomeStrings {
    override val appName = "Анти-Обломов"
    override val serviceDownShort = "Служба блокировки не работает"
    override val serviceDownWarning = "Служба блокировки не работает — приложения сейчас не блокируются."
    override val noAppsWarning = "Приложения для блокировки не выбраны — отметь их на вкладке «Приложения»."

    override val freeTime = "Свободное время"
    override fun blockedUntil(at: String) = "Блокировка до $at"
    override val bySchedule = "по расписанию"
    override val byTimer = "по таймеру"
    override val byScheduleAndTimer = "по расписанию и таймеру"
    override val noBlocksScheduled = "по расписанию блокировок нет"
    override fun nextBlock(day: String, time: String, tomorrow: Boolean) = when {
        tomorrow -> "следующая завтра в $time"
        day.isEmpty() -> "следующая в $time"
        else -> "следующая в $day $time"
    }
    override val ringBlocked = "ИДЁТ БЛОКИРОВКА"
    override val ringFree = "СВОБОДНОЕ ВРЕМЯ"
    override val untilEnd = "до конца"
    override val nextBlockLabel = "следующая блокировка"
    override val nextShort = "следующая"

    override val statsTitle = "БЫЛО / СТАЛО"
    override val collapseAppList = "Свернуть список приложений"
    override val statsAppsTitle = "Приложения для статистики"
    override val accessExplain = "Чтобы видеть, сколько времени ты проводил в соцсетях до «Анти-Обломова» и сколько теперь, " +
        "включи доступ к статистике. Данные остаются на телефоне."
    override val enableAccess = "Включить доступ"
    override val collecting = "Собираем статистику…"
    override val chooseAppsHint = "Выбери приложения для статистики выше — или включи «Активировать все»."
    override val tileBefore = "До, в день"
    override val tileAfter = "После, в день"
    override val tileMore = "Больше, в день"
    override val tileSaved = "Сэкономлено, в день"
    override fun totalSaved(time: String, schoolDays: String?) =
        "Всего ты вернул себе $time" + if (schoolDays != null) " · $schoolDays" else ""
    override val howWeCount = "Как считаем"
    override val collapse = "Свернуть"
    override val details = "Подробнее"
    override fun beforeExplain(days: String, date: String) = "«До» — в среднем по $days до $date. "
    override fun noHistory(date: String) = "Истории до $date в телефоне не нашлось — сравнивать не с чем. "
    override val afterFirstDay = "«После» — пока только за сегодня, с полуночи: со второго дня здесь будет среднее."
    override fun afterExplain(days: String) = "«После» — в среднем по $days после установки; день установки не считается."
    override val screenBefore = "Всё экранное время до, в день"
    override val screenAfter = "Всё экранное время после, в день"
    override fun milestoneOverline(title: String) = "ВЕХА · ${title.uppercase()}"
    override val milestoneTitles = listOf(
        "Неделя", "Две недели",
        "Месяц", "Два месяца", "Три месяца", "Четыре месяца", "Пять месяцев", "Полгода",
        "Семь месяцев", "Восемь месяцев", "Девять месяцев", "Десять месяцев", "Одиннадцать месяцев", "Год",
    )
    override fun milestoneGood(days: String, perDay: String, total: String) =
        "За $days с «Анти-Обломовым» — на $perDay в день меньше, " +
            "чем до установки. Ты вернул себе $total."
    override fun milestoneBad(days: String, perDay: String) =
        "За $days — на $perDay в день больше, чем до установки. " +
            "Бывает — попробуй добавить интервал в расписание."

    override val appsAllExceptSystem = "Все, кроме системных"
    override val appsNone = "Не выбраны"
    override fun appsAsBlockList(n: Int) = "Как в списке блокировки: $n"
    override fun appsChosen(n: Int) = "Выбрано: $n"
    override val activateAll = "Активировать все"
    override val activateAllSub = "Считать все приложения, кроме системных"
    override val searchApps = "Найти и добавить приложение"
    override val clearSearch = "Очистить поиск"
    override val loadingApps = "Загружаю список приложений…"
    override val nothingChosen = "Пока ничего не выбрано — найдите приложение поиском и включите"
    override val nothingFound = "Ничего не найдено"

    override val noBrowser = "Не найден браузер, чтобы открыть ссылку"
    override val thankAuthor = "Поблагодарить автора"
    override fun youSave(time: String) = "Ты возвращаешь себе $time в день"
    override val freeNoAds = "Приложение бесплатное и без рекламы"
    override val support = "Поддержать ›"

    override val system = "Система"
    override val serviceNotWorking = "служба не работает"
    override val serviceTitle = "Служба блокировки"
    override val serviceRunning = "Служба работает"
    override val serviceRunningBody = "Выбранные приложения блокируются по расписанию и таймеру."
    override val accessibilitySettings = "Настройки спецвозможностей"
    override val serviceStopped = "Служба включена, но не работает"
    override fun serviceStoppedBody(label: String, autostart: String?) =
        "Её остановила система — например, после перезагрузки. Выключите и снова " +
            "включите «$label» в спецвозможностях." +
            if (autostart != null) " Чтобы служба запускалась сама, проверьте «$autostart» в настройке телефона ниже." else ""
    override val accessibilityShort = "Спецвозможности"
    override val appSettings = "Настройки приложения"
    override val serviceOff = "Служба выключена"
    override fun serviceOffBody(label: String) =
        "В «Специальных возможностях» найдите в списке служб " +
            "«$label» и включите её."
    override val openSettings = "Открыть настройки"
}

object HomeEn : HomeStrings {
    override val appName = "Anti-Oblomov"
    override val serviceDownShort = "Blocking service isn't running"
    override val serviceDownWarning = "Blocking service isn't running — apps aren't being blocked right now."
    override val noAppsWarning = "No apps chosen for blocking — pick them on the “Apps” tab."

    override val freeTime = "Free time"
    override fun blockedUntil(at: String) = "Blocked until $at"
    override val bySchedule = "by schedule"
    override val byTimer = "by timer"
    override val byScheduleAndTimer = "by schedule and timer"
    override val noBlocksScheduled = "no blocks scheduled"
    override fun nextBlock(day: String, time: String, tomorrow: Boolean) = when {
        tomorrow -> "next block tomorrow at $time"
        day.isEmpty() -> "next block at $time"
        else -> "next block on $day at $time"
    }
    override val ringBlocked = "BLOCKING NOW"
    override val ringFree = "FREE TIME"
    override val untilEnd = "ends today"
    override val nextBlockLabel = "next block"
    override val nextShort = "next"

    override val statsTitle = "BEFORE / AFTER"
    override val collapseAppList = "Collapse app list"
    override val statsAppsTitle = "Apps for stats"
    override val accessExplain = "To see how much time you spent on social media before Anti-Oblomov and how much now, " +
        "turn on usage access. The data stays on your phone."
    override val enableAccess = "Turn on access"
    override val collecting = "Collecting stats…"
    override val chooseAppsHint = "Choose apps for stats above — or turn on “Include all”."
    override val tileBefore = "Before, per day"
    override val tileAfter = "After, per day"
    override val tileMore = "More, per day"
    override val tileSaved = "Saved, per day"
    override fun totalSaved(time: String, schoolDays: String?) =
        "In total you got back $time" + if (schoolDays != null) " · $schoolDays" else ""
    override val howWeCount = "How we count"
    override val collapse = "Hide"
    override val details = "Details"
    override fun beforeExplain(days: String, date: String) = "“Before” is the average over $days before $date. "
    override fun noHistory(date: String) = "No history before $date on the phone — nothing to compare with. "
    override val afterFirstDay = "“After” is just today so far, since midnight; from day two you'll see the average here."
    override fun afterExplain(days: String) = "“After” is the average over $days since installing; the install day doesn't count."
    override val screenBefore = "All screen time before, per day"
    override val screenAfter = "All screen time after, per day"
    override fun milestoneOverline(title: String) = "MILESTONE · ${title.uppercase(Locale.ENGLISH)}"
    override val milestoneTitles = listOf(
        "One week", "Two weeks",
        "One month", "Two months", "Three months", "Four months", "Five months", "Half a year",
        "Seven months", "Eight months", "Nine months", "Ten months", "Eleven months", "One year",
    )
    override fun milestoneGood(days: String, perDay: String, total: String) =
        "In $days with Anti-Oblomov — $perDay less per day than before installing. You got back $total."
    override fun milestoneBad(days: String, perDay: String) =
        "In $days — $perDay more per day than before installing. It happens — try adding an interval to your schedule."

    override val appsAllExceptSystem = "All except system"
    override val appsNone = "None selected"
    override fun appsAsBlockList(n: Int) = "Same as block list: $n"
    override fun appsChosen(n: Int) = "Selected: $n"
    override val activateAll = "Include all"
    override val activateAllSub = "Count all apps except system ones"
    override val searchApps = "Find and add an app"
    override val clearSearch = "Clear search"
    override val loadingApps = "Loading app list…"
    override val nothingChosen = "Nothing selected yet — find an app with search and turn it on"
    override val nothingFound = "Nothing found"

    override val noBrowser = "No browser found to open the link"
    override val thankAuthor = "Thank the author"
    override fun youSave(time: String) = "You get back $time a day"
    override val freeNoAds = "The app is free and ad-free"
    override val support = "Support ›"

    override val system = "System"
    override val serviceNotWorking = "service isn't running"
    override val serviceTitle = "Blocking service"
    override val serviceRunning = "Service is running"
    override val serviceRunningBody = "Chosen apps are blocked by schedule and timer."
    override val accessibilitySettings = "Accessibility settings"
    override val serviceStopped = "Service is on but not running"
    override fun serviceStoppedBody(label: String, autostart: String?) =
        "The system stopped it — for example, after a restart. Turn “$label” off and on again in Accessibility." +
            if (autostart != null) " To make the service start on its own, check “$autostart” in phone setup below." else ""
    override val accessibilityShort = "Accessibility"
    override val appSettings = "App settings"
    override val serviceOff = "Service is off"
    override fun serviceOffBody(label: String) = "In Accessibility, find “$label” in the list of services and turn it on."
    override val openSettings = "Open settings"
}

object HomeEs : HomeStrings {
    override val appName = "Anti-Oblomov"
    override val serviceDownShort = "El servicio de bloqueo no funciona"
    override val serviceDownWarning = "El servicio de bloqueo no funciona: ahora mismo las apps no se bloquean."
    override val noAppsWarning = "No has elegido apps para bloquear: márcalas en la pestaña «Apps»."

    override val freeTime = "Tiempo libre"
    override fun blockedUntil(at: String) = "Bloqueo hasta $at"
    override val bySchedule = "por horario"
    override val byTimer = "por temporizador"
    override val byScheduleAndTimer = "por horario y temporizador"
    override val noBlocksScheduled = "no hay bloqueos programados"
    override fun nextBlock(day: String, time: String, tomorrow: Boolean) = when {
        tomorrow -> "próximo bloqueo mañana a las $time"
        day.isEmpty() -> "próximo bloqueo a las $time"
        else -> "próximo bloqueo el $day a las $time"
    }
    override val ringBlocked = "BLOQUEO ACTIVO"
    override val ringFree = "TIEMPO LIBRE"
    override val untilEnd = "termina hoy"
    override val nextBlockLabel = "próximo bloqueo"
    override val nextShort = "próximo"

    override val statsTitle = "ANTES / DESPUÉS"
    override val collapseAppList = "Ocultar lista de apps"
    override val statsAppsTitle = "Apps para estadísticas"
    override val accessExplain = "Para ver cuánto tiempo pasabas en redes sociales antes de Anti-Oblomov y cuánto ahora, " +
        "activa el acceso a los datos de uso. Los datos se quedan en el teléfono."
    override val enableAccess = "Activar acceso"
    override val collecting = "Recopilando estadísticas…"
    override val chooseAppsHint = "Elige arriba las apps para estadísticas, o activa «Incluir todas»."
    override val tileBefore = "Antes, al día"
    override val tileAfter = "Después, al día"
    override val tileMore = "Más, al día"
    override val tileSaved = "Ahorrado, al día"
    override fun totalSaved(time: String, schoolDays: String?) =
        "En total recuperaste $time" + if (schoolDays != null) " · $schoolDays" else ""
    override val howWeCount = "Cómo lo calculamos"
    override val collapse = "Ocultar"
    override val details = "Detalles"
    override fun beforeExplain(days: String, date: String) = "«Antes»: promedio de $days antes del $date. "
    override fun noHistory(date: String) = "No hay historial antes del $date en el teléfono: no hay con qué comparar. "
    override val afterFirstDay = "«Después»: por ahora solo hoy, desde medianoche; a partir del segundo día aquí verás el promedio."
    override fun afterExplain(days: String) = "«Después»: promedio de $days tras la instalación; el día de instalación no cuenta."
    override val screenBefore = "Tiempo de pantalla total antes, al día"
    override val screenAfter = "Tiempo de pantalla total después, al día"
    override fun milestoneOverline(title: String) = "HITO · ${title.uppercase(Locale.forLanguageTag("es"))}"
    override val milestoneTitles = listOf(
        "Una semana", "Dos semanas",
        "Un mes", "Dos meses", "Tres meses", "Cuatro meses", "Cinco meses", "Medio año",
        "Siete meses", "Ocho meses", "Nueve meses", "Diez meses", "Once meses", "Un año",
    )
    override fun milestoneGood(days: String, perDay: String, total: String) =
        "En $days con Anti-Oblomov: $perDay menos al día que antes de instalarlo. Recuperaste $total."
    override fun milestoneBad(days: String, perDay: String) =
        "En $days: $perDay más al día que antes de instalarlo. Pasa; prueba a añadir un intervalo al horario."

    override val appsAllExceptSystem = "Todas excepto las del sistema"
    override val appsNone = "Ninguna elegida"
    override fun appsAsBlockList(n: Int) = "Como en la lista de bloqueo: $n"
    override fun appsChosen(n: Int) = "Elegidas: $n"
    override val activateAll = "Incluir todas"
    override val activateAllSub = "Contar todas las apps excepto las del sistema"
    override val searchApps = "Busca y añade una app"
    override val clearSearch = "Borrar búsqueda"
    override val loadingApps = "Cargando la lista de apps…"
    override val nothingChosen = "Aún no hay nada elegido: busca una app y actívala"
    override val nothingFound = "No se encontró nada"

    override val noBrowser = "No hay un navegador para abrir el enlace"
    override val thankAuthor = "Agradecer al autor"
    override fun youSave(time: String) = "Recuperas $time al día"
    override val freeNoAds = "La app es gratis y sin anuncios"
    override val support = "Apoyar ›"

    override val system = "Sistema"
    override val serviceNotWorking = "el servicio no funciona"
    override val serviceTitle = "Servicio de bloqueo"
    override val serviceRunning = "El servicio funciona"
    override val serviceRunningBody = "Las apps elegidas se bloquean según el horario y el temporizador."
    override val accessibilitySettings = "Ajustes de accesibilidad"
    override val serviceStopped = "El servicio está activado, pero no funciona"
    override fun serviceStoppedBody(label: String, autostart: String?) =
        "El sistema lo detuvo, por ejemplo, tras reiniciar. Desactiva y vuelve a activar «$label» en Accesibilidad." +
            if (autostart != null) " Para que el servicio se inicie solo, revisa «$autostart» en la configuración del teléfono, abajo." else ""
    override val accessibilityShort = "Accesibilidad"
    override val appSettings = "Ajustes de la app"
    override val serviceOff = "El servicio está desactivado"
    override fun serviceOffBody(label: String) = "En Accesibilidad, busca «$label» en la lista de servicios y actívalo."
    override val openSettings = "Abrir ajustes"
}

object HomePt : HomeStrings {
    override val appName = "Anti-Oblomov"
    override val serviceDownShort = "O serviço de bloqueio não está funcionando"
    override val serviceDownWarning = "O serviço de bloqueio não está funcionando — os apps não estão sendo bloqueados agora."
    override val noAppsWarning = "Nenhum app escolhido para bloquear — marque-os na aba “Apps”."

    override val freeTime = "Tempo livre"
    override fun blockedUntil(at: String) = "Bloqueio até $at"
    override val bySchedule = "pela agenda"
    override val byTimer = "pelo timer"
    override val byScheduleAndTimer = "pela agenda e pelo timer"
    override val noBlocksScheduled = "nenhum bloqueio programado"
    override fun nextBlock(day: String, time: String, tomorrow: Boolean) = when {
        tomorrow -> "próximo bloqueio amanhã às $time"
        day.isEmpty() -> "próximo bloqueio às $time"
        else -> "próximo bloqueio $day às $time"
    }
    override val ringBlocked = "BLOQUEIO ATIVO"
    override val ringFree = "TEMPO LIVRE"
    override val untilEnd = "termina hoje"
    override val nextBlockLabel = "próximo bloqueio"
    override val nextShort = "próximo"

    override val statsTitle = "ANTES / DEPOIS"
    override val collapseAppList = "Recolher lista de apps"
    override val statsAppsTitle = "Apps para estatísticas"
    override val accessExplain = "Para ver quanto tempo você passava nas redes sociais antes do Anti-Oblomov e quanto passa agora, " +
        "ative o acesso aos dados de uso. Os dados ficam no celular."
    override val enableAccess = "Ativar acesso"
    override val collecting = "Coletando estatísticas…"
    override val chooseAppsHint = "Escolha acima os apps para as estatísticas — ou ative “Incluir todos”."
    override val tileBefore = "Antes, por dia"
    override val tileAfter = "Depois, por dia"
    override val tileMore = "A mais, por dia"
    override val tileSaved = "Economizado, por dia"
    override fun totalSaved(time: String, schoolDays: String?) =
        "No total, você recuperou $time" + if (schoolDays != null) " · $schoolDays" else ""
    override val howWeCount = "Como calculamos"
    override val collapse = "Ocultar"
    override val details = "Detalhes"
    override fun beforeExplain(days: String, date: String) = "“Antes” é a média de $days antes de $date. "
    override fun noHistory(date: String) = "Não há histórico antes de $date no celular — não há com o que comparar. "
    override val afterFirstDay = "“Depois” por enquanto é só hoje, desde a meia-noite; a partir do segundo dia, aqui aparece a média."
    override fun afterExplain(days: String) = "“Depois” é a média de $days após a instalação; o dia da instalação não conta."
    override val screenBefore = "Tempo de tela total antes, por dia"
    override val screenAfter = "Tempo de tela total depois, por dia"
    override fun milestoneOverline(title: String) = "MARCO · ${title.uppercase(Locale.forLanguageTag("pt-BR"))}"
    override val milestoneTitles = listOf(
        "Uma semana", "Duas semanas",
        "Um mês", "Dois meses", "Três meses", "Quatro meses", "Cinco meses", "Meio ano",
        "Sete meses", "Oito meses", "Nove meses", "Dez meses", "Onze meses", "Um ano",
    )
    override fun milestoneGood(days: String, perDay: String, total: String) =
        "Em $days com o Anti-Oblomov: $perDay a menos por dia do que antes da instalação. Você recuperou $total."
    override fun milestoneBad(days: String, perDay: String) =
        "Em $days: $perDay a mais por dia do que antes da instalação. Acontece — tente adicionar um intervalo à agenda."

    override val appsAllExceptSystem = "Todos, exceto os do sistema"
    override val appsNone = "Nenhum escolhido"
    override fun appsAsBlockList(n: Int) = "Igual à lista de bloqueio: $n"
    override fun appsChosen(n: Int) = "Escolhidos: $n"
    override val activateAll = "Incluir todos"
    override val activateAllSub = "Contar todos os apps, exceto os do sistema"
    override val searchApps = "Encontre e adicione um app"
    override val clearSearch = "Limpar busca"
    override val loadingApps = "Carregando a lista de apps…"
    override val nothingChosen = "Nada escolhido ainda — encontre um app na busca e ative"
    override val nothingFound = "Nada encontrado"

    override val noBrowser = "Nenhum navegador encontrado para abrir o link"
    override val thankAuthor = "Agradecer ao autor"
    override fun youSave(time: String) = "Você recupera $time por dia"
    override val freeNoAds = "O app é gratuito e sem anúncios"
    override val support = "Apoiar ›"

    override val system = "Sistema"
    override val serviceNotWorking = "o serviço não está funcionando"
    override val serviceTitle = "Serviço de bloqueio"
    override val serviceRunning = "O serviço está funcionando"
    override val serviceRunningBody = "Os apps escolhidos são bloqueados pela agenda e pelo timer."
    override val accessibilitySettings = "Configurações de acessibilidade"
    override val serviceStopped = "O serviço está ativado, mas não funciona"
    override fun serviceStoppedBody(label: String, autostart: String?) =
        "O sistema o interrompeu — por exemplo, após reiniciar. Desative e ative de novo “$label” em Acessibilidade." +
            if (autostart != null) " Para o serviço iniciar sozinho, verifique “$autostart” na configuração do celular abaixo." else ""
    override val accessibilityShort = "Acessibilidade"
    override val appSettings = "Configurações do app"
    override val serviceOff = "O serviço está desativado"
    override fun serviceOffBody(label: String) = "Em Acessibilidade, encontre “$label” na lista de serviços e ative."
    override val openSettings = "Abrir configurações"
}

object HomeDe : HomeStrings {
    override val appName = "Anti-Oblomov"
    override val serviceDownShort = "Sperrdienst läuft nicht"
    override val serviceDownWarning = "Der Sperrdienst läuft nicht — Apps werden gerade nicht gesperrt."
    override val noAppsWarning = "Keine Apps zum Sperren ausgewählt — markiere sie im Tab „Apps“."

    override val freeTime = "Freizeit"
    override fun blockedUntil(at: String) = "Sperre bis $at"
    override val bySchedule = "nach Zeitplan"
    override val byTimer = "per Timer"
    override val byScheduleAndTimer = "nach Zeitplan und Timer"
    override val noBlocksScheduled = "keine Sperren geplant"
    override fun nextBlock(day: String, time: String, tomorrow: Boolean) = when {
        tomorrow -> "nächste Sperre morgen um $time"
        day.isEmpty() -> "nächste Sperre um $time"
        else -> "nächste Sperre am $day um $time"
    }
    override val ringBlocked = "SPERRE AKTIV"
    override val ringFree = "FREIZEIT"
    override val untilEnd = "endet heute"
    override val nextBlockLabel = "nächste Sperre"
    override val nextShort = "nächste"

    override val statsTitle = "VORHER / NACHHER"
    override val collapseAppList = "App-Liste einklappen"
    override val statsAppsTitle = "Apps für die Statistik"
    override val accessExplain = "Um zu sehen, wie viel Zeit du vor Anti-Oblomov in sozialen Netzwerken verbracht hast und wie viel jetzt, " +
        "erlaube den Zugriff auf Nutzungsdaten. Die Daten bleiben auf dem Handy."
    override val enableAccess = "Zugriff erlauben"
    override val collecting = "Statistik wird gesammelt…"
    override val chooseAppsHint = "Wähle oben Apps für die Statistik — oder aktiviere „Alle einbeziehen“."
    override val tileBefore = "Vorher, pro Tag"
    override val tileAfter = "Nachher, pro Tag"
    override val tileMore = "Mehr, pro Tag"
    override val tileSaved = "Gespart, pro Tag"
    override fun totalSaved(time: String, schoolDays: String?) =
        "Insgesamt zurückgewonnen: $time" + if (schoolDays != null) " · $schoolDays" else ""
    override val howWeCount = "Wie wir rechnen"
    override val collapse = "Ausblenden"
    override val details = "Details"
    override fun beforeExplain(days: String, date: String) = "„Vorher“ ist der Durchschnitt über $days vor dem $date. "
    override fun noHistory(date: String) = "Vor dem $date gibt es keinen Verlauf auf dem Handy — nichts zum Vergleichen. "
    override val afterFirstDay = "„Nachher“ ist vorerst nur heute, seit Mitternacht; ab dem zweiten Tag steht hier der Durchschnitt."
    override fun afterExplain(days: String) = "„Nachher“ ist der Durchschnitt über $days nach der Installation; der Installationstag zählt nicht."
    override val screenBefore = "Bildschirmzeit gesamt vorher, pro Tag"
    override val screenAfter = "Bildschirmzeit gesamt nachher, pro Tag"
    override fun milestoneOverline(title: String) = "MEILENSTEIN · ${title.uppercase(Locale.GERMAN)}"
    override val milestoneTitles = listOf(
        "Eine Woche", "Zwei Wochen",
        "Ein Monat", "Zwei Monate", "Drei Monate", "Vier Monate", "Fünf Monate", "Ein halbes Jahr",
        "Sieben Monate", "Acht Monate", "Neun Monate", "Zehn Monate", "Elf Monate", "Ein Jahr",
    )
    override fun milestoneGood(days: String, perDay: String, total: String) =
        "In $days mit Anti-Oblomov — $perDay weniger pro Tag als vor der Installation. Du hast $total zurückgewonnen."
    override fun milestoneBad(days: String, perDay: String) =
        "In $days — $perDay mehr pro Tag als vor der Installation. Kommt vor — füg dem Zeitplan ein Intervall hinzu."

    override val appsAllExceptSystem = "Alle außer System-Apps"
    override val appsNone = "Keine ausgewählt"
    override fun appsAsBlockList(n: Int) = "Wie in der Sperrliste: $n"
    override fun appsChosen(n: Int) = "Ausgewählt: $n"
    override val activateAll = "Alle einbeziehen"
    override val activateAllSub = "Alle Apps außer System-Apps zählen"
    override val searchApps = "App suchen und hinzufügen"
    override val clearSearch = "Suche löschen"
    override val loadingApps = "App-Liste wird geladen…"
    override val nothingChosen = "Noch nichts ausgewählt — such eine App und schalte sie ein"
    override val nothingFound = "Nichts gefunden"

    override val noBrowser = "Kein Browser zum Öffnen des Links gefunden"
    override val thankAuthor = "Dem Autor danken"
    override fun youSave(time: String) = "Du gewinnst $time am Tag zurück"
    override val freeNoAds = "Die App ist kostenlos und werbefrei"
    override val support = "Unterstützen ›"

    override val system = "System"
    override val serviceNotWorking = "Dienst läuft nicht"
    override val serviceTitle = "Sperrdienst"
    override val serviceRunning = "Dienst läuft"
    override val serviceRunningBody = "Ausgewählte Apps werden nach Zeitplan und Timer gesperrt."
    override val accessibilitySettings = "Bedienungshilfen-Einstellungen"
    override val serviceStopped = "Dienst ist an, läuft aber nicht"
    override fun serviceStoppedBody(label: String, autostart: String?) =
        "Das System hat ihn gestoppt — zum Beispiel nach einem Neustart. Schalte „$label“ in den Bedienungshilfen aus und wieder ein." +
            if (autostart != null) " Damit der Dienst von selbst startet, prüfe „$autostart“ in der Handy-Einrichtung unten." else ""
    override val accessibilityShort = "Bedienungshilfen"
    override val appSettings = "App-Einstellungen"
    override val serviceOff = "Dienst ist aus"
    override fun serviceOffBody(label: String) = "Such in den Bedienungshilfen in der Liste der Dienste „$label“ und schalte ihn ein."
    override val openSettings = "Einstellungen öffnen"
}

object HomeTr : HomeStrings {
    private val tr = Locale.forLanguageTag("tr")
    override val appName = "Anti-Oblomov"
    override val serviceDownShort = "Engelleme hizmeti çalışmıyor"
    override val serviceDownWarning = "Engelleme hizmeti çalışmıyor — uygulamalar şu an engellenmiyor."
    override val noAppsWarning = "Engellenecek uygulama seçilmedi — onları “Uygulamalar” sekmesinde işaretle."

    override val freeTime = "Serbest zaman"
    override fun blockedUntil(at: String) = "Engelleme bitişi: $at"
    override val bySchedule = "programa göre"
    override val byTimer = "zamanlayıcıya göre"
    override val byScheduleAndTimer = "program ve zamanlayıcıya göre"
    override val noBlocksScheduled = "planlı engelleme yok"
    override fun nextBlock(day: String, time: String, tomorrow: Boolean) = when {
        tomorrow -> "sonraki engelleme yarın saat $time"
        day.isEmpty() -> "sonraki engelleme saat $time"
        else -> "sonraki engelleme $day saat $time"
    }
    override val ringBlocked = "ENGELLEME SÜRÜYOR"
    override val ringFree = "SERBEST ZAMAN"
    override val untilEnd = "bugün bitiyor"
    override val nextBlockLabel = "sonraki engelleme"
    override val nextShort = "sonraki"

    override val statsTitle = "ÖNCE / SONRA"
    override val collapseAppList = "Uygulama listesini daralt"
    override val statsAppsTitle = "İstatistik için uygulamalar"
    override val accessExplain = "Anti-Oblomov'dan önce sosyal medyada ne kadar zaman geçirdiğini ve şimdi ne kadar geçirdiğini görmek için " +
        "kullanım erişimini aç. Veriler telefonda kalır."
    override val enableAccess = "Erişimi aç"
    override val collecting = "İstatistik toplanıyor…"
    override val chooseAppsHint = "Yukarıdan istatistik için uygulama seç — ya da “Tümünü etkinleştir” seçeneğini aç."
    override val tileBefore = "Önce, günlük"
    override val tileAfter = "Sonra, günlük"
    override val tileMore = "Fazla, günlük"
    override val tileSaved = "Kazanılan, günlük"
    override fun totalSaved(time: String, schoolDays: String?) =
        "Toplamda kendine $time kazandırdın" + if (schoolDays != null) " · $schoolDays" else ""
    override val howWeCount = "Nasıl hesaplıyoruz"
    override val collapse = "Gizle"
    override val details = "Ayrıntılar"
    override fun beforeExplain(days: String, date: String) = "“Önce”, $date öncesindeki $days ortalaması. "
    override fun noHistory(date: String) = "Telefonda $date öncesine ait geçmiş yok — karşılaştıracak bir şey yok. "
    override val afterFirstDay = "“Sonra” şimdilik sadece bugün, gece yarısından beri; ikinci günden itibaren burada ortalama olacak."
    override fun afterExplain(days: String) = "“Sonra”, kurulumdan sonraki $days ortalaması; kurulum günü sayılmaz."
    override val screenBefore = "Toplam ekran süresi önce, günlük"
    override val screenAfter = "Toplam ekran süresi sonra, günlük"
    override fun milestoneOverline(title: String) = "DÖNÜM NOKTASI · ${title.uppercase(tr)}"
    override val milestoneTitles = listOf(
        "Bir hafta", "İki hafta",
        "Bir ay", "İki ay", "Üç ay", "Dört ay", "Beş ay", "Altı ay",
        "Yedi ay", "Sekiz ay", "Dokuz ay", "On ay", "On bir ay", "Bir yıl",
    )
    override fun milestoneGood(days: String, perDay: String, total: String) =
        "Anti-Oblomov ile $days içinde — kurulumdan öncesine göre günde $perDay daha az. Kendine $total kazandırdın."
    override fun milestoneBad(days: String, perDay: String) =
        "$days içinde — kurulumdan öncesine göre günde $perDay daha fazla. Olur böyle — programa bir aralık eklemeyi dene."

    override val appsAllExceptSystem = "Sistem hariç tümü"
    override val appsNone = "Seçilmedi"
    override fun appsAsBlockList(n: Int) = "Engelleme listesindeki gibi: $n"
    override fun appsChosen(n: Int) = "Seçilen: $n"
    override val activateAll = "Tümünü etkinleştir"
    override val activateAllSub = "Sistem uygulamaları hariç tümünü say"
    override val searchApps = "Uygulama bul ve ekle"
    override val clearSearch = "Aramayı temizle"
    override val loadingApps = "Uygulama listesi yükleniyor…"
    override val nothingChosen = "Henüz bir şey seçilmedi — aramayla bir uygulama bul ve aç"
    override val nothingFound = "Hiçbir şey bulunamadı"

    override val noBrowser = "Bağlantıyı açacak tarayıcı bulunamadı"
    override val thankAuthor = "Yazara teşekkür et"
    override fun youSave(time: String) = "Günde kendine $time kazandırıyorsun"
    override val freeNoAds = "Uygulama ücretsiz ve reklamsız"
    override val support = "Destekle ›"

    override val system = "Sistem"
    override val serviceNotWorking = "hizmet çalışmıyor"
    override val serviceTitle = "Engelleme hizmeti"
    override val serviceRunning = "Hizmet çalışıyor"
    override val serviceRunningBody = "Seçilen uygulamalar programa ve zamanlayıcıya göre engellenir."
    override val accessibilitySettings = "Erişilebilirlik ayarları"
    override val serviceStopped = "Hizmet açık ama çalışmıyor"
    override fun serviceStoppedBody(label: String, autostart: String?) =
        "Sistem onu durdurdu — örneğin yeniden başlatmadan sonra. Erişilebilirlik bölümünde “$label” seçeneğini kapatıp yeniden aç." +
            if (autostart != null) " Hizmetin kendiliğinden başlaması için aşağıdaki telefon ayarlarında “$autostart” seçeneğini kontrol et." else ""
    override val accessibilityShort = "Erişilebilirlik"
    override val appSettings = "Uygulama ayarları"
    override val serviceOff = "Hizmet kapalı"
    override fun serviceOffBody(label: String) = "Erişilebilirlik bölümünde hizmet listesinden “$label” seçeneğini bul ve aç."
    override val openSettings = "Ayarları aç"
}

object HomeKk : HomeStrings {
    private val kk = Locale.forLanguageTag("kk")
    override val appName = "Анти-Обломов"
    override val serviceDownShort = "Бұғаттау қызметі жұмыс істемейді"
    override val serviceDownWarning = "Бұғаттау қызметі жұмыс істемейді — қолданбалар қазір бұғатталмайды."
    override val noAppsWarning = "Бұғаттауға қолданбалар таңдалмаған — оларды «Қолданбалар» қойындысында белгіле."

    override val freeTime = "Бос уақыт"
    override fun blockedUntil(at: String) = "Бұғаттау $at дейін"
    override val bySchedule = "кесте бойынша"
    override val byTimer = "таймер бойынша"
    override val byScheduleAndTimer = "кесте мен таймер бойынша"
    override val noBlocksScheduled = "жоспарланған бұғаттау жоқ"
    override fun nextBlock(day: String, time: String, tomorrow: Boolean) = when {
        tomorrow -> "келесі бұғаттау ертең сағат $time"
        day.isEmpty() -> "келесі бұғаттау сағат $time"
        else -> "келесі бұғаттау $day сағат $time"
    }
    override val ringBlocked = "БҰҒАТТАУ ҚОСУЛЫ"
    override val ringFree = "БОС УАҚЫТ"
    override val untilEnd = "бүгін бітеді"
    override val nextBlockLabel = "келесі бұғаттау"
    override val nextShort = "келесісі"

    override val statsTitle = "БҰРЫН / ҚАЗІР"
    override val collapseAppList = "Қолданбалар тізімін жию"
    override val statsAppsTitle = "Статистикаға арналған қолданбалар"
    override val accessExplain = "«Анти-Обломов» орнатылғанға дейін әлеуметтік желілерде қанша уақыт өткізгеніңді және қазір қанша " +
        "өткізетініңді көру үшін статистикаға рұқсат бер. Деректер телефонда қалады."
    override val enableAccess = "Рұқсат беру"
    override val collecting = "Статистика жиналуда…"
    override val chooseAppsHint = "Жоғарыдан статистикаға қолданбаларды таңда — немесе «Барлығын қосу» опциясын қос."
    override val tileBefore = "Бұрын, күніне"
    override val tileAfter = "Кейін, күніне"
    override val tileMore = "Артық, күніне"
    override val tileSaved = "Үнемделді, күніне"
    override fun totalSaved(time: String, schoolDays: String?) =
        "Барлығы өзіңе $time қайтардың" + if (schoolDays != null) " · $schoolDays" else ""
    override val howWeCount = "Қалай есептейміз"
    override val collapse = "Жасыру"
    override val details = "Толығырақ"
    override fun beforeExplain(days: String, date: String) = "«Бұрын» — $date дейінгі $days бойынша орташа. "
    override fun noHistory(date: String) = "Телефонда $date дейінгі тарих табылмады — салыстыратын ештеңе жоқ. "
    override val afterFirstDay = "«Кейін» — әзірге тек бүгін, түн ортасынан бері: екінші күннен бастап мұнда орташа көрсетіледі."
    override fun afterExplain(days: String) = "«Кейін» — орнатқаннан кейінгі $days бойынша орташа; орнату күні есептелмейді."
    override val screenBefore = "Барлық экран уақыты бұрын, күніне"
    override val screenAfter = "Барлық экран уақыты кейін, күніне"
    override fun milestoneOverline(title: String) = "КЕЗЕҢ · ${title.uppercase(kk)}"
    override val milestoneTitles = listOf(
        "Бір апта", "Екі апта",
        "Бір ай", "Екі ай", "Үш ай", "Төрт ай", "Бес ай", "Жарты жыл",
        "Жеті ай", "Сегіз ай", "Тоғыз ай", "Он ай", "Он бір ай", "Бір жыл",
    )
    override fun milestoneGood(days: String, perDay: String, total: String) =
        "Орнатқаннан бергі $days ішінде — бұрынғыдан күніне $perDay аз. Өзіңе $total қайтардың."
    override fun milestoneBad(days: String, perDay: String) =
        "$days ішінде — орнатуға дейінгіден күніне $perDay көп. Болып тұрады — кестеге аралық қосып көр."

    override val appsAllExceptSystem = "Жүйеліктерден басқасы"
    override val appsNone = "Таңдалмаған"
    override fun appsAsBlockList(n: Int) = "Бұғаттау тізіміндегідей: $n"
    override fun appsChosen(n: Int) = "Таңдалды: $n"
    override val activateAll = "Барлығын қосу"
    override val activateAllSub = "Жүйелік қолданбалардан басқасының бәрін санау"
    override val searchApps = "Қолданбаны тауып, қос"
    override val clearSearch = "Іздеуді тазалау"
    override val loadingApps = "Қолданбалар тізімі жүктелуде…"
    override val nothingChosen = "Әзірге ештеңе таңдалмаған — қолданбаны іздеп тауып, қос"
    override val nothingFound = "Ештеңе табылмады"

    override val noBrowser = "Сілтемені ашатын браузер табылмады"
    override val thankAuthor = "Авторға алғыс айту"
    override fun youSave(time: String) = "Күніне өзіңе $time қайтарып жатырсың"
    override val freeNoAds = "Қолданба тегін және жарнамасыз"
    override val support = "Қолдау ›"

    override val system = "Жүйе"
    override val serviceNotWorking = "қызмет жұмыс істемейді"
    override val serviceTitle = "Бұғаттау қызметі"
    override val serviceRunning = "Қызмет жұмыс істеп тұр"
    override val serviceRunningBody = "Таңдалған қолданбалар кесте мен таймер бойынша бұғатталады."
    override val accessibilitySettings = "Арнайы мүмкіндіктер параметрлері"
    override val serviceStopped = "Қызмет қосулы, бірақ жұмыс істемейді"
    override fun serviceStoppedBody(label: String, autostart: String?) =
        "Оны жүйе тоқтатты — мысалы, қайта жүктелгеннен кейін. Арнайы мүмкіндіктерде «$label» қызметін өшіріп, қайта қос." +
            if (autostart != null) " Қызмет өзі іске қосылуы үшін төмендегі телефонды баптау бөлімінде «$autostart» тармағын тексер." else ""
    override val accessibilityShort = "Арнайы мүмкіндіктер"
    override val appSettings = "Қолданба параметрлері"
    override val serviceOff = "Қызмет өшірулі"
    override fun serviceOffBody(label: String) = "«Арнайы мүмкіндіктер» бөлімінде қызметтер тізімінен «$label» тауып, қос."
    override val openSettings = "Параметрлерді ашу"
}

object HomeUz : HomeStrings {
    override val appName = "Anti-Oblomov"
    override val serviceDownShort = "Bloklash xizmati ishlamayapti"
    override val serviceDownWarning = "Bloklash xizmati ishlamayapti — ilovalar hozir bloklanmaydi."
    override val noAppsWarning = "Bloklash uchun ilovalar tanlanmagan — ularni «Ilovalar» bo‘limida belgila."

    override val freeTime = "Bo‘sh vaqt"
    override fun blockedUntil(at: String) = "Bloklash $at gacha"
    override val bySchedule = "jadval bo‘yicha"
    override val byTimer = "taymer bo‘yicha"
    override val byScheduleAndTimer = "jadval va taymer bo‘yicha"
    override val noBlocksScheduled = "rejalashtirilgan bloklash yo‘q"
    override fun nextBlock(day: String, time: String, tomorrow: Boolean) = when {
        tomorrow -> "keyingi bloklash ertaga soat $time"
        day.isEmpty() -> "keyingi bloklash soat $time"
        else -> "keyingi bloklash $day soat $time"
    }
    override val ringBlocked = "BLOKLASH FAOL"
    override val ringFree = "BO‘SH VAQT"
    override val untilEnd = "bugun tugaydi"
    override val nextBlockLabel = "keyingi bloklash"
    override val nextShort = "keyingisi"

    override val statsTitle = "AVVAL / HOZIR"
    override val collapseAppList = "Ilovalar ro‘yxatini yig‘ish"
    override val statsAppsTitle = "Statistika uchun ilovalar"
    override val accessExplain = "«Anti-Oblomov»dan oldin ijtimoiy tarmoqlarda qancha vaqt o‘tkazganingni va hozir qanchaligini ko‘rish uchun " +
        "foydalanish statistikasiga ruxsat ber. Ma’lumotlar telefonda qoladi."
    override val enableAccess = "Ruxsat berish"
    override val collecting = "Statistika yig‘ilmoqda…"
    override val chooseAppsHint = "Yuqorida statistika uchun ilovalarni tanla — yoki «Hammasini yoqish»ni tanla."
    override val tileBefore = "Oldin, kuniga"
    override val tileAfter = "Keyin, kuniga"
    override val tileMore = "Ortiqcha, kuniga"
    override val tileSaved = "Tejaldi, kuniga"
    override fun totalSaved(time: String, schoolDays: String?) =
        "Jami o‘zingga $time qaytarding" + if (schoolDays != null) " · $schoolDays" else ""
    override val howWeCount = "Qanday hisoblaymiz"
    override val collapse = "Yashirish"
    override val details = "Batafsil"
    override fun beforeExplain(days: String, date: String) = "«Oldin» — $date gacha bo‘lgan $days o‘rtachasi. "
    override fun noHistory(date: String) = "Telefonda $date gacha bo‘lgan tarix topilmadi — solishtiradigan narsa yo‘q. "
    override val afterFirstDay = "«Keyin» — hozircha faqat bugun, yarim tundan beri: ikkinchi kundan boshlab bu yerda o‘rtacha bo‘ladi."
    override fun afterExplain(days: String) = "«Keyin» — o‘rnatilgandan keyingi $days o‘rtachasi; o‘rnatilgan kun hisobga olinmaydi."
    override val screenBefore = "Jami ekran vaqti oldin, kuniga"
    override val screenAfter = "Jami ekran vaqti keyin, kuniga"
    override fun milestoneOverline(title: String) = "BOSQICH · ${title.uppercase(Locale.ROOT)}"
    override val milestoneTitles = listOf(
        "Bir hafta", "Ikki hafta",
        "Bir oy", "Ikki oy", "Uch oy", "To‘rt oy", "Besh oy", "Yarim yil",
        "Yetti oy", "Sakkiz oy", "To‘qqiz oy", "O‘n oy", "O‘n bir oy", "Bir yil",
    )
    override fun milestoneGood(days: String, perDay: String, total: String) =
        "«Anti-Oblomov» bilan $days ichida — o‘rnatishdan oldingiga qaraganda kuniga $perDay kam. O‘zingga $total qaytarding."
    override fun milestoneBad(days: String, perDay: String) =
        "$days ichida — o‘rnatishdan oldingiga qaraganda kuniga $perDay ko‘p. Bo‘lib turadi — jadvalga oraliq qo‘shib ko‘r."

    override val appsAllExceptSystem = "Tizimdan tashqari hammasi"
    override val appsNone = "Tanlanmagan"
    override fun appsAsBlockList(n: Int) = "Bloklash ro‘yxatidagidek: $n"
    override fun appsChosen(n: Int) = "Tanlandi: $n"
    override val activateAll = "Hammasini yoqish"
    override val activateAllSub = "Tizim ilovalaridan tashqari hammasini hisoblash"
    override val searchApps = "Ilovani topib qo‘sh"
    override val clearSearch = "Qidiruvni tozalash"
    override val loadingApps = "Ilovalar ro‘yxati yuklanmoqda…"
    override val nothingChosen = "Hozircha hech narsa tanlanmagan — ilovani qidiruv orqali topib, yoq"
    override val nothingFound = "Hech narsa topilmadi"

    override val noBrowser = "Havolani ochish uchun brauzer topilmadi"
    override val thankAuthor = "Muallifga rahmat aytish"
    override fun youSave(time: String) = "Kuniga o‘zingga $time qaytaryapsan"
    override val freeNoAds = "Ilova bepul va reklamasiz"
    override val support = "Qo‘llab-quvvatlash ›"

    override val system = "Tizim"
    override val serviceNotWorking = "xizmat ishlamayapti"
    override val serviceTitle = "Bloklash xizmati"
    override val serviceRunning = "Xizmat ishlayapti"
    override val serviceRunningBody = "Tanlangan ilovalar jadval va taymer bo‘yicha bloklanadi."
    override val accessibilitySettings = "Maxsus imkoniyatlar sozlamalari"
    override val serviceStopped = "Xizmat yoqilgan, lekin ishlamayapti"
    override fun serviceStoppedBody(label: String, autostart: String?) =
        "Uni tizim to‘xtatdi — masalan, qayta yoqilgandan keyin. Maxsus imkoniyatlarda «$label»ni o‘chirib, qayta yoq." +
            if (autostart != null) " Xizmat o‘zi ishga tushishi uchun quyidagi telefon sozlamasida «$autostart»ni tekshir." else ""
    override val accessibilityShort = "Maxsus imkoniyatlar"
    override val appSettings = "Ilova sozlamalari"
    override val serviceOff = "Xizmat o‘chirilgan"
    override fun serviceOffBody(label: String) = "«Maxsus imkoniyatlar»da xizmatlar ro‘yxatidan «$label»ni topib, yoq."
    override val openSettings = "Sozlamalarni ochish"
}

object HomeZh : HomeStrings {
    override val appName = "反奥勃洛莫夫"
    override val serviceDownShort = "屏蔽服务未运行"
    override val serviceDownWarning = "屏蔽服务未运行——应用现在不会被屏蔽。"
    override val noAppsWarning = "还没有选择要屏蔽的应用——请在“应用”标签页中勾选。"

    override val freeTime = "自由时间"
    override fun blockedUntil(at: String) = "屏蔽至 ${at}"
    override val bySchedule = "按时间表"
    override val byTimer = "按计时器"
    override val byScheduleAndTimer = "按时间表和计时器"
    override val noBlocksScheduled = "没有计划的屏蔽"
    override fun nextBlock(day: String, time: String, tomorrow: Boolean) = when {
        tomorrow -> "下次屏蔽：明天 ${time}"
        day.isEmpty() -> "下次屏蔽：${time}"
        else -> "下次屏蔽：${day} ${time}"
    }
    override val ringBlocked = "正在屏蔽"
    override val ringFree = "自由时间"
    override val untilEnd = "今天结束"
    override val nextBlockLabel = "下次屏蔽"
    override val nextShort = "下次"

    override val statsTitle = "之前 / 现在"
    override val collapseAppList = "收起应用列表"
    override val statsAppsTitle = "参与统计的应用"
    override val accessExplain = "想看看用“反奥勃洛莫夫”之前和现在你在社交网络上花了多少时间，" +
        "请开启使用情况访问权限。数据只保存在手机上。"
    override val enableAccess = "开启权限"
    override val collecting = "正在收集统计…"
    override val chooseAppsHint = "在上方选择要统计的应用——或开启“全部启用”。"
    override val tileBefore = "之前，每天"
    override val tileAfter = "之后，每天"
    override val tileMore = "多用，每天"
    override val tileSaved = "节省，每天"
    override fun totalSaved(time: String, schoolDays: String?) =
        "你一共为自己找回了 ${time}" + if (schoolDays != null) " · ${schoolDays}" else ""
    override val howWeCount = "计算方式"
    override val collapse = "收起"
    override val details = "详情"
    override fun beforeExplain(days: String, date: String) = "“之前”是 ${date} 之前 ${days}的平均值。"
    override fun noHistory(date: String) = "手机里没有 ${date} 之前的记录——无从比较。"
    override val afterFirstDay = "“之后”目前只算今天（从午夜起）；从第二天起这里显示平均值。"
    override fun afterExplain(days: String) = "“之后”是安装后 ${days}的平均值；安装当天不计入。"
    override val screenBefore = "之前总屏幕时间，每天"
    override val screenAfter = "之后总屏幕时间，每天"
    override fun milestoneOverline(title: String) = "里程碑 · ${title}"
    override val milestoneTitles = listOf(
        "一周", "两周",
        "一个月", "两个月", "三个月", "四个月", "五个月", "半年",
        "七个月", "八个月", "九个月", "十个月", "十一个月", "一年",
    )
    override fun milestoneGood(days: String, perDay: String, total: String) =
        "使用“反奥勃洛莫夫”的 ${days}里，你每天比安装前少用 ${perDay}。你为自己找回了 ${total}。"
    override fun milestoneBad(days: String, perDay: String) =
        "在 ${days}里，你每天比安装前多用 ${perDay}。这很正常——试着在时间表里加一个时段。"

    override val appsAllExceptSystem = "除系统应用外全部"
    override val appsNone = "未选择"
    override fun appsAsBlockList(n: Int) = "同屏蔽列表：${n}"
    override fun appsChosen(n: Int) = "已选：${n}"
    override val activateAll = "全部启用"
    override val activateAllSub = "统计除系统应用外的所有应用"
    override val searchApps = "查找并添加应用"
    override val clearSearch = "清除搜索"
    override val loadingApps = "正在加载应用列表…"
    override val nothingChosen = "还没有选择——用搜索找到应用并开启"
    override val nothingFound = "未找到"

    override val noBrowser = "找不到可打开链接的浏览器"
    override val thankAuthor = "感谢作者"
    override fun youSave(time: String) = "你每天为自己找回 ${time}"
    override val freeNoAds = "应用免费且无广告"
    override val support = "支持 ›"

    override val system = "系统"
    override val serviceNotWorking = "服务未运行"
    override val serviceTitle = "屏蔽服务"
    override val serviceRunning = "服务运行中"
    override val serviceRunningBody = "所选应用会按时间表和计时器被屏蔽。"
    override val accessibilitySettings = "无障碍设置"
    override val serviceStopped = "服务已开启，但未运行"
    override fun serviceStoppedBody(label: String, autostart: String?) =
        "系统停止了它——例如在重启之后。请在无障碍中关闭再重新开启“${label}”。" +
            if (autostart != null) "要让服务自动启动，请在下方的手机设置中检查“${autostart}”。" else ""
    override val accessibilityShort = "无障碍"
    override val appSettings = "应用设置"
    override val serviceOff = "服务已关闭"
    override fun serviceOffBody(label: String) = "在“无障碍”的服务列表中找到“${label}”并开启。"
    override val openSettings = "打开设置"
}
