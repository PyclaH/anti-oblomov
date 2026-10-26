package ru.shashkov.antioblomov.rules.i18n

/** Текст инструкции оболочки: заголовок, зачем, шаги, подпись кнопки (экраны — в SetupGuide). */
class GuideText(val title: String, val why: String, val steps: List<String>, val button: String)

/**
 * Мастер настройки, подсказки по маркам телефонов, окно согласия.
 *
 * Названия системных пунктов — как в прошивке на этом языке, если известны точно;
 * иначе — описательно, без кавычек (см. docs/i18n.md).
 */
interface SetupStrings {
    // ---- пункты «Настройка телефона» (SetupItem) и строка «Система» ----
    val itemService: String
    val itemNotifications: String
    val itemBattery: String
    val itemAutostart: String
    val itemUsage: String
    /** «служба не работает» */
    val summaryServiceOff: String
    /** «всё в порядке» */
    val summaryAllOk: String
    /** «2 настройки пропущены» */
    fun summaryMissing(n: Int): String

    // ---- инструкции оболочек (SetupGuide) ----
    val autostartXiaomi: GuideText
    val autostartSamsung: GuideText
    val autostartOppo: GuideText
    val autostartHuawei: GuideText
    val autostartHonor: GuideText
    val autostartVivo: GuideText
    val autostartTranssion: GuideText
    val batteryXiaomi: GuideText
    /** Серый переключатель «Ограниченная настройка» (Android 13+) */
    val restrictedHelp: List<String>

    // ---- мастер: приветствие ----
    val welcomeTitle: String
    val welcomeIntro: String
    val welcomeHow: String
    /** Кнопка первой страницы */
    val start: String

    // ---- служба блокировки ----
    val serviceTitle: String
    val serviceWhy: String
    val serviceDoesTitle: String
    val serviceDoes: List<String>
    val serviceDoesNotTitle: String
    val serviceDoesNot: List<String>
    val serviceSteps: List<String>
    val serviceOn: String
    val serviceOff: String
    /** Кнопка «Открыть спецвозможности» — на неё ссылаются [serviceSteps] и [restrictedHelp] */
    val openAccessibility: String
    val restrictedQuestion: String
    val openAppInfo: String

    // ---- уведомления ----
    val notificationsTitle: String
    val notificationsWhy: String
    val notificationsOn: String
    val notificationsOff: String
    val allowNotifications: String
    val openNotificationSettings: String

    // ---- батарея ----
    val batteryTitle: String
    val batteryWhy: String
    val batteryOn: String
    val batteryOff: String
    val batteryHint: String
    val allowBackground: String

    // ---- статистика «Было / стало» ----
    val usageTitle: String
    val usageWhy: String
    val usageReadsTitle: String
    val usageReads: List<String>
    val usageSteps: List<String>
    val usageOn: String
    val usageOff: String
    /** Кнопка «Открыть настройки» — на неё ссылается [usageSteps] */
    val openSettings: String

    // ---- финиш ----
    val finishDone: String
    val finishAlmost: String
    val finishSkipped: String
    val finishRest: String
    val chooseApps: String
    val toHome: String

    // ---- общие куски мастера ----
    /** «Если открылась страница „О приложении“ — ищи эту настройку там.» */
    val guideFallback: String
    /** «Сделано» — отметка и подпись значка */
    val markDone: String
    val notDone: String

    // ---- карточка «Настройка телефона» на главной ----
    val cardTitle: String
    val cardHint: String
    val cardSetUp: String
    val cardRedo: String

    // ---- окно согласия (Google Play / RuStore) ----
    val consentTitle: String
    val consentIntro: String
    val consentData: String
    val consentWhy: String
    val consentNot: String
    val consentOff: String
    val consentAccept: String
    val consentLater: String
}

// =====================================================================================
// Русский — основной
// =====================================================================================

object SetupRu : SetupStrings {
    override val itemService = "Служба блокировки"
    override val itemNotifications = "Уведомления таймера"
    override val itemBattery = "Батарея без ограничений"
    override val itemAutostart = "Автозапуск"
    override val itemUsage = "Доступ к статистике"
    override val summaryServiceOff = "служба не работает"
    override val summaryAllOk = "всё в порядке"
    override fun summaryMissing(n: Int) = when {
        n % 100 in 11..14 -> "$n настроек пропущено"
        n % 10 == 1 -> "$n настройка пропущена"
        n % 10 in 2..4 -> "$n настройки пропущены"
        else -> "$n настроек пропущено"
    }

    override val autostartXiaomi = GuideText(
        title = "Автозапуск",
        why = "Без него HyperOS и MIUI не запускают службу блокировки после перезагрузки.",
        steps = listOf(
            "Откроется список «Автозапуск» приложения «Безопасность»",
            "Найдите «Анти-Обломов» и включите переключатель",
        ),
        button = "Открыть «Автозапуск»",
    )
    override val autostartSamsung = GuideText(
        title = "Не усыплять приложение",
        why = "Samsung усыпляет приложения, которыми давно не пользовались, — служба блокировки перестаёт работать.",
        steps = listOf(
            "Настройки → Обслуживание устройства (или «Батарея») → Ограничения фонового использования",
            "«Приложения, которые никогда не переходят в спящий режим» → «+» → «Анти-Обломов»",
            "Если «Анти-Обломов» есть в списке «Спящие приложения» — уберите оттуда",
        ),
        button = "Открыть настройки батареи",
    )
    override val autostartOppo = GuideText(
        title = "Автозапуск",
        why = "ColorOS и Realme UI не запускают службу после перезагрузки без этого разрешения.",
        steps = listOf(
            "Найдите «Анти-Обломов» в списке автозапуска и включите",
            "Если списка нет: Настройки → Приложения → Анти-Обломов → «Использование батареи» → разрешить «Автозапуск» и «Работу в фоне»",
        ),
        button = "Открыть автозапуск",
    )
    private fun appLaunch(os: String) = GuideText(
        title = "Запуск приложений",
        why = "$os сама решает, каким приложениям работать в фоне, и может остановить службу блокировки.",
        steps = listOf(
            "Найдите «Анти-Обломов» и выключите «Управлять автоматически»",
            "В появившемся окне включите все три: «Автозапуск», «Косвенный запуск», «Работа в фоне»",
        ),
        button = "Открыть «Запуск приложений»",
    )
    override val autostartHuawei = appLaunch("EMUI")
    override val autostartHonor = appLaunch("MagicOS")
    override val autostartVivo = GuideText(
        title = "Автозапуск",
        why = "Funtouch OS и OriginOS не запускают службу после перезагрузки без этого разрешения.",
        steps = listOf(
            "Найдите «Анти-Обломов» в списке автозапуска и включите",
            "Ещё: Настройки → Батарея → «Высокое энергопотребление в фоне» → разрешить «Анти-Обломову»",
        ),
        button = "Открыть автозапуск",
    )
    override val autostartTranssion = GuideText(
        title = "Автозапуск",
        why = "HiOS и XOS не запускают службу после перезагрузки без этого разрешения.",
        steps = listOf(
            "Откроется «Phone Master» → «Автозапуск» — включите «Анти-Обломов»",
            "Если не открылось: Phone Master → Управление приложениями → Автозапуск",
        ),
        button = "Открыть автозапуск",
    )
    override val batteryXiaomi = GuideText(
        title = "Контроль активности",
        why = "У HyperOS своя экономия батареи — она может останавливать службу блокировки.",
        steps = listOf(
            "Откроется «Контроль активности» для «Анти-Обломова»",
            "Выберите «Нет ограничений»",
        ),
        button = "Открыть «Контроль активности»",
    )
    override val restrictedHelp = listOf(
        "Настройки → Приложения → Анти-Обломов",
        "⋮ вверху справа → «Разрешить ограниченные настройки»",
        "Вернитесь сюда и снова нажмите «Открыть спецвозможности»",
    )

    override val welcomeTitle = "Настроим «Анти-Обломов»"
    override val welcomeIntro =
        "Приложение закрывает выбранные соцсети и игры в учебное время — по расписанию или таймеру — " +
            "и показывает вместо них цитату."
    override val welcomeHow =
        "Чтобы блокировка работала всегда, телефону нужно разрешить несколько вещей. " +
            "Это займёт 2–3 минуты. Любой шаг можно пропустить и вернуться к нему с главного экрана."
    override val start = "Начать"

    override val serviceTitle = "Служба блокировки"
    override val serviceWhy = "Чтобы закрывать выбранные приложения, «Анти-Обломову» нужна служба специальных возможностей Android."
    override val serviceDoesTitle = "Что делает служба"
    override val serviceDoes = listOf(
        "Узнаёт название приложения, которое открылось на экране",
        "Если для него сейчас время блокировки — показывает экран с цитатой",
    )
    override val serviceDoesNotTitle = "Чего служба не делает"
    override val serviceDoesNot = listOf(
        "Не читает текст на экране, сообщения и пароли",
        "Ничего не отправляет в интернет — всё остаётся на телефоне",
    )
    override val serviceSteps = listOf(
        "Нажмите «Открыть спецвозможности»",
        "Найдите «Анти-Обломов: блокировка» — бывает в разделе «Скачанные приложения» или «Установленные службы»",
        "Включите и подтвердите. Телефон может попросить подождать несколько секунд — это нормально",
    )
    override val serviceOn = "Служба работает"
    override val serviceOff = "Служба пока не включена"
    override val openAccessibility = "Открыть спецвозможности"
    override val restrictedQuestion = "Переключатель серый и не включается?"
    override val openAppInfo = "Открыть «О приложении»"

    override val notificationsTitle = "Уведомления"
    override val notificationsWhy = "Пока идёт блокировка — по расписанию или таймеру, — в шторке видно, до какого времени и сколько осталось. Больше уведомлений приложение не присылает."
    override val notificationsOn = "Уведомления разрешены"
    override val notificationsOff = "Уведомления выключены"
    override val allowNotifications = "Разрешить уведомления"
    override val openNotificationSettings = "Открыть настройки уведомлений"

    override val batteryTitle = "Батарея"
    override val batteryWhy = "Ради экономии заряда система останавливает приложения, которые работают в фоне, — и блокировка тихо перестаёт работать."
    override val batteryOn = "Работа в фоне разрешена"
    override val batteryOff = "Работа в фоне ограничена"
    override val batteryHint = "Нажмите кнопку и в окне выберите «Разрешить». Если вместо окна открылся список — найдите «Анти-Обломов» и выберите «Не экономить» или «Без ограничений»."
    override val allowBackground = "Разрешить работу в фоне"

    override val usageTitle = "Статистика «Было / стало»"
    override val usageWhy =
        "На главной будет видно, сколько времени вы проводили в выбранных приложениях до «Анти-Обломова» " +
            "и сколько теперь — и сколько времени вернули себе."
    override val usageReadsTitle = "Что для этого читается"
    override val usageReads = listOf(
        "Сколько минут каждое приложение было на экране — та же статистика, что в «Цифровом благополучии»",
        "Данные остаются на телефоне и никуда не отправляются",
    )
    override val usageSteps = listOf(
        "Нажмите «Открыть настройки»",
        "Найдите «Анти-Обломов» и включите «Доступ к истории использования»",
    )
    override val usageOn = "Доступ к статистике есть"
    override val usageOff = "Доступа к статистике пока нет"
    override val openSettings = "Открыть настройки"

    override val finishDone = "Телефон настроен"
    override val finishAlmost = "Почти готово"
    override val finishSkipped = "Пропущенное можно настроить позже: на главной — карточка «Настройка телефона»."
    override val finishRest = "Осталось выбрать, что блокировать, и задать расписание или запустить таймер."
    override val chooseApps = "Выбрать приложения"
    override val toHome = "На главную"

    override val guideFallback = "Если открылась страница «О приложении» — ищи эту настройку там."
    override val markDone = "Сделано"
    override val notDone = "Не сделано"

    override val cardTitle = "Настройка телефона"
    override val cardHint = "Без этих настроек блокировка может отключаться сама, а «Было / стало» — не считаться."
    override val cardSetUp = "Настроить"
    override val cardRedo = "Пройти настройку заново"

    override val consentTitle = "Служба блокировки: что она видит"
    override val consentIntro =
        "«Анти-Обломов» использует службу специальных возможностей Android (Accessibility Service), " +
            "чтобы закрывать выбранные приложения во время блокировки."
    override val consentData = "Какие данные: служба узнаёт, какое приложение сейчас открыто на экране (его системное название)."
    override val consentWhy =
        "Зачем: если это приложение в вашем списке блокировки и сейчас время блокировки, " +
            "поверх него показывается экран с цитатой."
    override val consentNot =
        "Служба не читает текст на экране, сообщения и пароли, ничего не сохраняет о ваших действиях " +
            "и ничего не передаёт: у приложения нет доступа к интернету."
    override val consentOff = "Выключить службу можно в любой момент в настройках спецвозможностей телефона."
    override val consentAccept = "Принимаю"
    override val consentLater = "Не сейчас"
}

// =====================================================================================
// English
// =====================================================================================

object SetupEn : SetupStrings {
    override val itemService = "Blocking service"
    override val itemNotifications = "Timer notifications"
    override val itemBattery = "Unrestricted battery"
    override val itemAutostart = "Autostart"
    override val itemUsage = "Usage access"
    override val summaryServiceOff = "service not running"
    override val summaryAllOk = "all good"
    override fun summaryMissing(n: Int) = if (n == 1) "1 setting skipped" else "$n settings skipped"

    override val autostartXiaomi = GuideText(
        title = "Autostart",
        why = "Without it, HyperOS and MIUI won't start the blocking service after a reboot.",
        steps = listOf(
            "The “Autostart” list of the “Security” app will open",
            "Find “Anti-Oblomov” and turn the switch on",
        ),
        button = "Open “Autostart”",
    )
    override val autostartSamsung = GuideText(
        title = "Keep the app awake",
        why = "Samsung puts apps you haven't used for a while to sleep, and the blocking service stops working.",
        steps = listOf(
            "Settings → Device care (or “Battery”) → Background usage limits",
            "“Never sleeping apps” → “+” → “Anti-Oblomov”",
            "If “Anti-Oblomov” is in “Sleeping apps”, remove it from there",
        ),
        button = "Open battery settings",
    )
    override val autostartOppo = GuideText(
        title = "Auto launch",
        why = "Without this permission, ColorOS and Realme UI won't start the service after a reboot.",
        steps = listOf(
            "Find “Anti-Oblomov” in the auto launch list and turn it on",
            "No list? Settings → Apps → Anti-Oblomov → “Battery usage” → turn on “Allow auto launch” and “Allow background activity”",
        ),
        button = "Open auto launch",
    )
    private fun appLaunch(os: String) = GuideText(
        title = "App launch",
        why = "$os decides by itself which apps may run in the background and can stop the blocking service.",
        steps = listOf(
            "Find “Anti-Oblomov” and turn off “Manage automatically”",
            "In the window that appears, turn on all three: “Auto-launch”, “Secondary launch”, “Run in background”",
        ),
        button = "Open “App launch”",
    )
    override val autostartHuawei = appLaunch("EMUI")
    override val autostartHonor = appLaunch("MagicOS")
    override val autostartVivo = GuideText(
        title = "Autostart",
        why = "Without this permission, Funtouch OS and OriginOS won't start the service after a reboot.",
        steps = listOf(
            "Find “Anti-Oblomov” in the autostart list and turn it on",
            "Also: Settings → Battery → high background power consumption → allow it for “Anti-Oblomov”",
        ),
        button = "Open autostart",
    )
    override val autostartTranssion = GuideText(
        title = "Autostart",
        why = "Without this permission, HiOS and XOS won't start the service after a reboot.",
        steps = listOf(
            "“Phone Master” will open its autostart list — turn on “Anti-Oblomov”",
            "If it didn't open: Phone Master → app management → autostart",
        ),
        button = "Open autostart",
    )
    override val batteryXiaomi = GuideText(
        title = "Battery saver",
        why = "HyperOS has its own battery saving, and it can stop the blocking service.",
        steps = listOf(
            "“Battery saver” for “Anti-Oblomov” will open",
            "Choose “No restrictions”",
        ),
        button = "Open “Battery saver”",
    )
    override val restrictedHelp = listOf(
        "Settings → Apps → Anti-Oblomov",
        "⋮ in the top right → “Allow restricted settings”",
        "Come back here and tap “Open Accessibility” again",
    )

    override val welcomeTitle = "Let's set up Anti-Oblomov"
    override val welcomeIntro =
        "The app closes the social networks and games you choose during study time — on a schedule or a timer — " +
            "and shows a quote instead."
    override val welcomeHow =
        "To keep blocking working at all times, your phone needs to allow a few things. " +
            "It takes 2–3 minutes. You can skip any step and come back to it from the home screen."
    override val start = "Start"

    override val serviceTitle = "Blocking service"
    override val serviceWhy = "To close the chosen apps, Anti-Oblomov needs an Android accessibility service."
    override val serviceDoesTitle = "What the service does"
    override val serviceDoes = listOf(
        "Learns the name of the app that has opened on the screen",
        "If it's blocking time for that app, shows a screen with a quote",
    )
    override val serviceDoesNotTitle = "What the service doesn't do"
    override val serviceDoesNot = listOf(
        "Doesn't read on-screen text, messages or passwords",
        "Sends nothing to the internet — everything stays on the phone",
    )
    override val serviceSteps = listOf(
        "Tap “Open Accessibility”",
        "Find “Anti-Oblomov: blocking” — usually under “Downloaded apps” or “Installed apps”",
        "Turn it on and confirm. The phone may ask you to wait a few seconds — that's normal",
    )
    override val serviceOn = "Service is running"
    override val serviceOff = "Service isn't on yet"
    override val openAccessibility = "Open Accessibility"
    override val restrictedQuestion = "Switch is grey and won't turn on?"
    override val openAppInfo = "Open “App info”"

    override val notificationsTitle = "Notifications"
    override val notificationsWhy = "While blocking is on — by schedule or timer — the notification shade shows when it ends and how much time is left. The app sends no other notifications."
    override val notificationsOn = "Notifications allowed"
    override val notificationsOff = "Notifications are off"
    override val allowNotifications = "Allow notifications"
    override val openNotificationSettings = "Open notification settings"

    override val batteryTitle = "Battery"
    override val batteryWhy = "To save power, the system stops apps running in the background — and blocking quietly stops working."
    override val batteryOn = "Background activity allowed"
    override val batteryOff = "Background activity restricted"
    override val batteryHint = "Tap the button and choose “Allow” in the dialog. If a list opens instead, find “Anti-Oblomov” and choose “Don't optimize” or “Unrestricted”."
    override val allowBackground = "Allow background activity"

    override val usageTitle = "“Before / after” stats"
    override val usageWhy =
        "The home screen will show how much time you spent in the chosen apps before Anti-Oblomov, " +
            "how much you spend now — and how much time you got back."
    override val usageReadsTitle = "What is read for this"
    override val usageReads = listOf(
        "How many minutes each app was on screen — the same stats as in “Digital Wellbeing”",
        "The data stays on the phone and isn't sent anywhere",
    )
    override val usageSteps = listOf(
        "Tap “Open settings”",
        "Find “Anti-Oblomov” and turn on “Permit usage access”",
    )
    override val usageOn = "Usage access granted"
    override val usageOff = "No usage access yet"
    override val openSettings = "Open settings"

    override val finishDone = "Phone is set up"
    override val finishAlmost = "Almost done"
    override val finishSkipped = "You can set up what you skipped later: see the “Phone setup” card on the home screen."
    override val finishRest = "All that's left is to choose what to block and set a schedule or start a timer."
    override val chooseApps = "Choose apps"
    override val toHome = "Go to home"

    override val guideFallback = "If the “App info” page opened, look for this setting there."
    override val markDone = "Done"
    override val notDone = "Not done"

    override val cardTitle = "Phone setup"
    override val cardHint = "Without these settings, blocking may turn itself off and “Before / after” may not be counted."
    override val cardSetUp = "Set up"
    override val cardRedo = "Run setup again"

    override val consentTitle = "Blocking service: what it sees"
    override val consentIntro =
        "Anti-Oblomov uses Android's accessibility service (Accessibility Service) " +
            "to close the chosen apps during blocking."
    override val consentData = "What data: the service learns which app is open on the screen right now (its system name)."
    override val consentWhy =
        "Why: if that app is on your block list and it's blocking time, " +
            "a screen with a quote is shown over it."
    override val consentNot =
        "The service doesn't read on-screen text, messages or passwords, doesn't store anything about your actions " +
            "and doesn't send anything: the app has no internet access."
    override val consentOff = "You can turn the service off at any time in your phone's accessibility settings."
    override val consentAccept = "Accept"
    override val consentLater = "Not now"
}

// =====================================================================================
// Español (tú)
// =====================================================================================

object SetupEs : SetupStrings {
    override val itemService = "Servicio de bloqueo"
    override val itemNotifications = "Avisos del temporizador"
    override val itemBattery = "Batería sin restricciones"
    override val itemAutostart = "Inicio automático"
    override val itemUsage = "Acceso a estadísticas"
    override val summaryServiceOff = "el servicio no funciona"
    override val summaryAllOk = "todo en orden"
    override fun summaryMissing(n: Int) = if (n == 1) "1 ajuste omitido" else "$n ajustes omitidos"

    override val autostartXiaomi = GuideText(
        title = "Inicio automático",
        why = "Sin él, HyperOS y MIUI no inician el servicio de bloqueo después de reiniciar.",
        steps = listOf(
            "Se abrirá la lista de inicio automático de la app “Seguridad”",
            "Busca “Anti-Oblomov” y activa el interruptor",
        ),
        button = "Abrir inicio automático",
    )
    override val autostartSamsung = GuideText(
        title = "Que no se suspenda",
        why = "Samsung suspende las apps que no usas desde hace tiempo, y el servicio de bloqueo deja de funcionar.",
        steps = listOf(
            "Ajustes → Batería (o Mantenimiento del dispositivo) → límites de uso en segundo plano",
            "Apps que nunca se suspenden → “+” → “Anti-Oblomov”",
            "Si “Anti-Oblomov” está en la lista de apps en suspensión, quítala de ahí",
        ),
        button = "Abrir ajustes de batería",
    )
    override val autostartOppo = GuideText(
        title = "Inicio automático",
        why = "Sin este permiso, ColorOS y Realme UI no inician el servicio después de reiniciar.",
        steps = listOf(
            "Busca “Anti-Oblomov” en la lista de inicio automático y actívala",
            "Si no hay lista: Ajustes → Aplicaciones → Anti-Oblomov → uso de batería → permite el inicio automático y la actividad en segundo plano",
        ),
        button = "Abrir inicio automático",
    )
    private fun appLaunch(os: String) = GuideText(
        title = "Inicio de aplicaciones",
        why = "$os decide por sí sola qué apps funcionan en segundo plano y puede detener el servicio de bloqueo.",
        steps = listOf(
            "Busca “Anti-Oblomov” y desactiva la gestión automática",
            "En la ventana que aparece, activa las tres opciones: inicio automático, inicio secundario y ejecución en segundo plano",
        ),
        button = "Abrir inicio de aplicaciones",
    )
    override val autostartHuawei = appLaunch("EMUI")
    override val autostartHonor = appLaunch("MagicOS")
    override val autostartVivo = GuideText(
        title = "Inicio automático",
        why = "Sin este permiso, Funtouch OS y OriginOS no inician el servicio después de reiniciar.",
        steps = listOf(
            "Busca “Anti-Oblomov” en la lista de inicio automático y actívala",
            "Además: Ajustes → Batería → alto consumo en segundo plano → permíteselo a “Anti-Oblomov”",
        ),
        button = "Abrir inicio automático",
    )
    override val autostartTranssion = GuideText(
        title = "Inicio automático",
        why = "Sin este permiso, HiOS y XOS no inician el servicio después de reiniciar.",
        steps = listOf(
            "Se abrirá la lista de inicio automático de “Phone Master”: activa “Anti-Oblomov”",
            "Si no se abrió: Phone Master → gestión de aplicaciones → inicio automático",
        ),
        button = "Abrir inicio automático",
    )
    override val batteryXiaomi = GuideText(
        title = "Ahorro de batería",
        why = "HyperOS tiene su propio ahorro de batería, que puede detener el servicio de bloqueo.",
        steps = listOf(
            "Se abrirá el ahorro de batería de “Anti-Oblomov”",
            "Elige la opción sin restricciones",
        ),
        button = "Abrir ahorro de batería",
    )
    override val restrictedHelp = listOf(
        "Ajustes → Aplicaciones → Anti-Oblomov",
        "⋮ arriba a la derecha → “Permitir ajustes restringidos”",
        "Vuelve aquí y toca otra vez “Abrir accesibilidad”",
    )

    override val welcomeTitle = "Configuremos Anti-Oblomov"
    override val welcomeIntro =
        "La app cierra las redes sociales y los juegos que elijas en horario de estudio —según un horario o un temporizador— " +
            "y muestra una cita en su lugar."
    override val welcomeHow =
        "Para que el bloqueo funcione siempre, el teléfono tiene que permitir algunas cosas. " +
            "Te llevará 2–3 minutos. Puedes omitir cualquier paso y volver a él desde la pantalla principal."
    override val start = "Empezar"

    override val serviceTitle = "Servicio de bloqueo"
    override val serviceWhy = "Para cerrar las apps elegidas, Anti-Oblomov necesita un servicio de accesibilidad de Android."
    override val serviceDoesTitle = "Qué hace el servicio"
    override val serviceDoes = listOf(
        "Detecta el nombre de la app que se ha abierto en pantalla",
        "Si es hora de bloqueo para esa app, muestra una pantalla con una cita",
    )
    override val serviceDoesNotTitle = "Qué no hace el servicio"
    override val serviceDoesNot = listOf(
        "No lee el texto de la pantalla, los mensajes ni las contraseñas",
        "No envía nada a internet: todo se queda en el teléfono",
    )
    override val serviceSteps = listOf(
        "Toca “Abrir accesibilidad”",
        "Busca “Anti-Oblomov: bloqueo”; suele estar en “Aplicaciones descargadas” o “Servicios instalados”",
        "Actívalo y confirma. El teléfono puede pedirte que esperes unos segundos; es normal",
    )
    override val serviceOn = "El servicio funciona"
    override val serviceOff = "El servicio aún no está activado"
    override val openAccessibility = "Abrir accesibilidad"
    override val restrictedQuestion = "¿El interruptor está gris y no se activa?"
    override val openAppInfo = "Abrir información de la app"

    override val notificationsTitle = "Notificaciones"
    override val notificationsWhy = "Mientras dura un bloqueo, por horario o temporizador, en el panel de notificaciones verás cuándo termina y cuánto falta. La app no envía otras notificaciones."
    override val notificationsOn = "Notificaciones permitidas"
    override val notificationsOff = "Notificaciones desactivadas"
    override val allowNotifications = "Permitir notificaciones"
    override val openNotificationSettings = "Abrir ajustes de notificaciones"

    override val batteryTitle = "Batería"
    override val batteryWhy = "Para ahorrar batería, el sistema detiene las apps que funcionan en segundo plano, y el bloqueo deja de funcionar sin avisar."
    override val batteryOn = "Segundo plano permitido"
    override val batteryOff = "Segundo plano restringido"
    override val batteryHint = "Toca el botón y elige “Permitir” en la ventana. Si en su lugar se abre una lista, busca “Anti-Oblomov” y elige “No optimizar” o “Sin restricciones”."
    override val allowBackground = "Permitir segundo plano"

    override val usageTitle = "Estadísticas “Antes / después”"
    override val usageWhy =
        "En la pantalla principal verás cuánto tiempo pasabas en las apps elegidas antes de Anti-Oblomov, " +
            "cuánto pasas ahora y cuánto tiempo has recuperado."
    override val usageReadsTitle = "Qué se lee para esto"
    override val usageReads = listOf(
        "Cuántos minutos estuvo cada app en pantalla: las mismas estadísticas que en “Bienestar digital”",
        "Los datos se quedan en el teléfono y no se envían a ningún sitio",
    )
    override val usageSteps = listOf(
        "Toca “Abrir ajustes”",
        "Busca “Anti-Oblomov” y activa el acceso a los datos de uso",
    )
    override val usageOn = "Acceso a estadísticas concedido"
    override val usageOff = "Aún no hay acceso a estadísticas"
    override val openSettings = "Abrir ajustes"

    override val finishDone = "Teléfono configurado"
    override val finishAlmost = "Casi listo"
    override val finishSkipped = "Lo omitido puedes configurarlo más tarde: en la pantalla principal, tarjeta “Configuración del teléfono”."
    override val finishRest = "Solo falta elegir qué bloquear y fijar un horario o iniciar un temporizador."
    override val chooseApps = "Elegir apps"
    override val toHome = "Ir al inicio"

    override val guideFallback = "Si se abrió la página de información de la app, busca este ajuste allí."
    override val markDone = "Hecho"
    override val notDone = "Sin hacer"

    override val cardTitle = "Configuración del teléfono"
    override val cardHint = "Sin estos ajustes, el bloqueo puede desactivarse solo y “Antes / después” puede no contarse."
    override val cardSetUp = "Configurar"
    override val cardRedo = "Repetir la configuración"

    override val consentTitle = "Servicio de bloqueo: qué ve"
    override val consentIntro =
        "Anti-Oblomov usa el servicio de accesibilidad de Android (Accessibility Service) " +
            "para cerrar las apps elegidas durante el bloqueo."
    override val consentData = "Qué datos: el servicio detecta qué app está abierta en pantalla en este momento (su nombre de sistema)."
    override val consentWhy =
        "Para qué: si esa app está en tu lista de bloqueo y es hora de bloqueo, " +
            "se muestra encima una pantalla con una cita."
    override val consentNot =
        "El servicio no lee el texto de la pantalla, los mensajes ni las contraseñas, no guarda nada sobre lo que haces " +
            "y no envía nada: la app no tiene acceso a internet."
    override val consentOff = "Puedes desactivar el servicio cuando quieras en los ajustes de accesibilidad del teléfono."
    override val consentAccept = "Acepto"
    override val consentLater = "Ahora no"
}

// =====================================================================================
// Português (Brasil, você)
// =====================================================================================

object SetupPt : SetupStrings {
    override val itemService = "Serviço de bloqueio"
    override val itemNotifications = "Notificações do timer"
    override val itemBattery = "Bateria sem restrições"
    override val itemAutostart = "Inicialização automática"
    override val itemUsage = "Acesso às estatísticas"
    override val summaryServiceOff = "o serviço não está funcionando"
    override val summaryAllOk = "tudo certo"
    override fun summaryMissing(n: Int) = if (n == 1) "1 configuração pulada" else "$n configurações puladas"

    override val autostartXiaomi = GuideText(
        title = "Inicialização automática",
        why = "Sem ela, o HyperOS e o MIUI não iniciam o serviço de bloqueio depois de reiniciar.",
        steps = listOf(
            "Vai abrir a lista de inicialização automática do app “Segurança”",
            "Encontre “Anti-Oblomov” e ative a chave",
        ),
        button = "Abrir inicialização automática",
    )
    override val autostartSamsung = GuideText(
        title = "Não deixar o app dormir",
        why = "A Samsung coloca em suspensão os apps que você não usa há tempo, e o serviço de bloqueio para de funcionar.",
        steps = listOf(
            "Configurações → Bateria (ou Manutenção do dispositivo) → limites de uso em segundo plano",
            "Apps que nunca entram em suspensão → “+” → “Anti-Oblomov”",
            "Se “Anti-Oblomov” estiver na lista de apps em suspensão, tire-o de lá",
        ),
        button = "Abrir configurações da bateria",
    )
    override val autostartOppo = GuideText(
        title = "Inicialização automática",
        why = "Sem esta permissão, o ColorOS e o Realme UI não iniciam o serviço depois de reiniciar.",
        steps = listOf(
            "Encontre “Anti-Oblomov” na lista de inicialização automática e ative",
            "Se não houver lista: Configurações → Apps → Anti-Oblomov → uso da bateria → permita a inicialização automática e a atividade em segundo plano",
        ),
        button = "Abrir inicialização automática",
    )
    private fun appLaunch(os: String) = GuideText(
        title = "Inicialização de apps",
        why = "O $os decide sozinho quais apps funcionam em segundo plano e pode parar o serviço de bloqueio.",
        steps = listOf(
            "Encontre “Anti-Oblomov” e desative o gerenciamento automático",
            "Na janela que aparecer, ative as três opções: inicialização automática, inicialização secundária e execução em segundo plano",
        ),
        button = "Abrir inicialização de apps",
    )
    override val autostartHuawei = appLaunch("EMUI")
    override val autostartHonor = appLaunch("MagicOS")
    override val autostartVivo = GuideText(
        title = "Inicialização automática",
        why = "Sem esta permissão, o Funtouch OS e o OriginOS não iniciam o serviço depois de reiniciar.",
        steps = listOf(
            "Encontre “Anti-Oblomov” na lista de inicialização automática e ative",
            "Também: Configurações → Bateria → alto consumo em segundo plano → permita para o “Anti-Oblomov”",
        ),
        button = "Abrir inicialização automática",
    )
    override val autostartTranssion = GuideText(
        title = "Inicialização automática",
        why = "Sem esta permissão, o HiOS e o XOS não iniciam o serviço depois de reiniciar.",
        steps = listOf(
            "Vai abrir a lista de inicialização automática do “Phone Master” — ative o “Anti-Oblomov”",
            "Se não abrir: Phone Master → gerenciamento de apps → inicialização automática",
        ),
        button = "Abrir inicialização automática",
    )
    override val batteryXiaomi = GuideText(
        title = "Economia de bateria",
        why = "O HyperOS tem a própria economia de bateria, que pode parar o serviço de bloqueio.",
        steps = listOf(
            "Vai abrir a economia de bateria do “Anti-Oblomov”",
            "Escolha a opção sem restrições",
        ),
        button = "Abrir economia de bateria",
    )
    override val restrictedHelp = listOf(
        "Configurações → Apps → Anti-Oblomov",
        "⋮ no canto superior direito → “Permitir configurações restritas”",
        "Volte aqui e toque de novo em “Abrir acessibilidade”",
    )

    override val welcomeTitle = "Vamos configurar o Anti-Oblomov"
    override val welcomeIntro =
        "O app fecha as redes sociais e os jogos escolhidos no horário de estudo — por agenda ou timer — " +
            "e mostra uma citação no lugar."
    override val welcomeHow =
        "Para o bloqueio funcionar sempre, o celular precisa permitir algumas coisas. " +
            "Leva 2–3 minutos. Você pode pular qualquer etapa e voltar a ela pela tela inicial."
    override val start = "Começar"

    override val serviceTitle = "Serviço de bloqueio"
    override val serviceWhy = "Para fechar os apps escolhidos, o Anti-Oblomov precisa de um serviço de acessibilidade do Android."
    override val serviceDoesTitle = "O que o serviço faz"
    override val serviceDoes = listOf(
        "Descobre o nome do app que abriu na tela",
        "Se for hora de bloqueio para ele, mostra uma tela com uma citação",
    )
    override val serviceDoesNotTitle = "O que o serviço não faz"
    override val serviceDoesNot = listOf(
        "Não lê o texto da tela, mensagens nem senhas",
        "Não envia nada para a internet — tudo fica no celular",
    )
    override val serviceSteps = listOf(
        "Toque em “Abrir acessibilidade”",
        "Encontre “Anti-Oblomov: bloqueio” — costuma ficar em “Apps baixados” ou “Serviços instalados”",
        "Ative e confirme. O celular pode pedir para esperar alguns segundos — é normal",
    )
    override val serviceOn = "O serviço está funcionando"
    override val serviceOff = "O serviço ainda não foi ativado"
    override val openAccessibility = "Abrir acessibilidade"
    override val restrictedQuestion = "A chave está cinza e não ativa?"
    override val openAppInfo = "Abrir informações do app"

    override val notificationsTitle = "Notificações"
    override val notificationsWhy = "Durante um bloqueio, pela agenda ou pelo timer, a barra de notificações mostra quando ele termina e quanto tempo falta. O app não envia outras notificações."
    override val notificationsOn = "Notificações permitidas"
    override val notificationsOff = "Notificações desativadas"
    override val allowNotifications = "Permitir notificações"
    override val openNotificationSettings = "Abrir configurações de notificação"

    override val batteryTitle = "Bateria"
    override val batteryWhy = "Para economizar bateria, o sistema para os apps que funcionam em segundo plano — e o bloqueio deixa de funcionar sem aviso."
    override val batteryOn = "Segundo plano permitido"
    override val batteryOff = "Segundo plano restrito"
    override val batteryHint = "Toque no botão e escolha “Permitir” na janela. Se abrir uma lista, encontre “Anti-Oblomov” e escolha “Não otimizar” ou “Sem restrições”."
    override val allowBackground = "Permitir segundo plano"

    override val usageTitle = "Estatística “Antes / depois”"
    override val usageWhy =
        "Na tela inicial você verá quanto tempo passava nos apps escolhidos antes do Anti-Oblomov, " +
            "quanto passa agora — e quanto tempo recuperou."
    override val usageReadsTitle = "O que é lido para isso"
    override val usageReads = listOf(
        "Quantos minutos cada app ficou na tela — a mesma estatística do “Bem-estar digital”",
        "Os dados ficam no celular e não são enviados para lugar nenhum",
    )
    override val usageSteps = listOf(
        "Toque em “Abrir configurações”",
        "Encontre “Anti-Oblomov” e ative o acesso ao uso",
    )
    override val usageOn = "Acesso às estatísticas concedido"
    override val usageOff = "Ainda sem acesso às estatísticas"
    override val openSettings = "Abrir configurações"

    override val finishDone = "Celular configurado"
    override val finishAlmost = "Quase pronto"
    override val finishSkipped = "O que foi pulado pode ser configurado depois: na tela inicial, no cartão “Configuração do celular”."
    override val finishRest = "Falta escolher o que bloquear e definir uma agenda ou iniciar um timer."
    override val chooseApps = "Escolher apps"
    override val toHome = "Ir para o início"

    override val guideFallback = "Se abriu a página de informações do app, procure esta configuração lá."
    override val markDone = "Feito"
    override val notDone = "Não feito"

    override val cardTitle = "Configuração do celular"
    override val cardHint = "Sem essas configurações, o bloqueio pode se desligar sozinho e o “Antes / depois” pode não ser contado."
    override val cardSetUp = "Configurar"
    override val cardRedo = "Refazer a configuração"

    override val consentTitle = "Serviço de bloqueio: o que ele vê"
    override val consentIntro =
        "O Anti-Oblomov usa o serviço de acessibilidade do Android (Accessibility Service) " +
            "para fechar os apps escolhidos durante o bloqueio."
    override val consentData = "Quais dados: o serviço descobre qual app está aberto na tela agora (o nome de sistema dele)."
    override val consentWhy =
        "Para quê: se esse app estiver na sua lista de bloqueio e for hora de bloqueio, " +
            "uma tela com uma citação aparece por cima dele."
    override val consentNot =
        "O serviço não lê o texto da tela, mensagens nem senhas, não salva nada sobre suas ações " +
            "e não envia nada: o app não tem acesso à internet."
    override val consentOff = "Você pode desligar o serviço a qualquer momento nas configurações de acessibilidade do celular."
    override val consentAccept = "Aceito"
    override val consentLater = "Agora não"
}

// =====================================================================================
// Deutsch (du)
// =====================================================================================

object SetupDe : SetupStrings {
    override val itemService = "Sperrdienst"
    override val itemNotifications = "Timer-Benachrichtigungen"
    override val itemBattery = "Akku ohne Einschränkungen"
    override val itemAutostart = "Autostart"
    override val itemUsage = "Zugriff auf Statistik"
    override val summaryServiceOff = "Dienst läuft nicht"
    override val summaryAllOk = "alles in Ordnung"
    override fun summaryMissing(n: Int) = if (n == 1) "1 Einstellung übersprungen" else "$n Einstellungen übersprungen"

    override val autostartXiaomi = GuideText(
        title = "Autostart",
        why = "Ohne Autostart starten HyperOS und MIUI den Sperrdienst nach einem Neustart nicht.",
        steps = listOf(
            "Die Autostart-Liste der App „Sicherheit“ öffnet sich",
            "Suche „Anti-Oblomov“ und schalte den Schalter ein",
        ),
        button = "„Autostart“ öffnen",
    )
    override val autostartSamsung = GuideText(
        title = "App nicht schlafen legen",
        why = "Samsung legt lange nicht genutzte Apps schlafen — dann funktioniert der Sperrdienst nicht mehr.",
        steps = listOf(
            "Einstellungen → Gerätewartung (oder Akku) → Limits für die Hintergrundnutzung",
            "Apps, die nie in den Standby gehen → „+“ → „Anti-Oblomov“",
            "Steht „Anti-Oblomov“ in der Liste der Apps im Standby, entferne sie dort",
        ),
        button = "Akku-Einstellungen öffnen",
    )
    override val autostartOppo = GuideText(
        title = "Autostart",
        why = "Ohne diese Berechtigung starten ColorOS und Realme UI den Dienst nach einem Neustart nicht.",
        steps = listOf(
            "Suche „Anti-Oblomov“ in der Autostart-Liste und schalte es ein",
            "Keine Liste? Einstellungen → Apps → Anti-Oblomov → Akkunutzung → Autostart und Hintergrundaktivität erlauben",
        ),
        button = "Autostart öffnen",
    )
    private fun appLaunch(os: String) = GuideText(
        title = "App-Start",
        why = "$os entscheidet selbst, welche Apps im Hintergrund laufen dürfen, und kann den Sperrdienst beenden.",
        steps = listOf(
            "Suche „Anti-Oblomov“ und schalte die automatische Verwaltung aus",
            "Schalte im Fenster alle drei ein: automatischer Start, sekundärer Start, Ausführung im Hintergrund",
        ),
        button = "App-Start öffnen",
    )
    override val autostartHuawei = appLaunch("EMUI")
    override val autostartHonor = appLaunch("MagicOS")
    override val autostartVivo = GuideText(
        title = "Autostart",
        why = "Ohne diese Berechtigung starten Funtouch OS und OriginOS den Dienst nach einem Neustart nicht.",
        steps = listOf(
            "Suche „Anti-Oblomov“ in der Autostart-Liste und schalte es ein",
            "Außerdem: Einstellungen → Akku → hoher Verbrauch im Hintergrund → für „Anti-Oblomov“ erlauben",
        ),
        button = "Autostart öffnen",
    )
    override val autostartTranssion = GuideText(
        title = "Autostart",
        why = "Ohne diese Berechtigung starten HiOS und XOS den Dienst nach einem Neustart nicht.",
        steps = listOf(
            "„Phone Master“ öffnet die Autostart-Liste — schalte „Anti-Oblomov“ ein",
            "Falls nicht: Phone Master → App-Verwaltung → Autostart",
        ),
        button = "Autostart öffnen",
    )
    override val batteryXiaomi = GuideText(
        title = "Energiesparen der App",
        why = "HyperOS hat ein eigenes Energiesparen — es kann den Sperrdienst beenden.",
        steps = listOf(
            "Die Energiespar-Einstellung für „Anti-Oblomov“ öffnet sich",
            "Wähle die Option ohne Einschränkungen",
        ),
        button = "Energiesparen öffnen",
    )
    override val restrictedHelp = listOf(
        "Einstellungen → Apps → Anti-Oblomov",
        "⋮ oben rechts → „Eingeschränkte Einstellungen zulassen“",
        "Komm hierher zurück und tippe wieder auf „Bedienungshilfen öffnen“",
    )

    override val welcomeTitle = "Anti-Oblomov einrichten"
    override val welcomeIntro =
        "Die App schließt ausgewählte soziale Netzwerke und Spiele während der Lernzeit — nach Zeitplan oder Timer — " +
            "und zeigt stattdessen ein Zitat."
    override val welcomeHow =
        "Damit die Sperre immer funktioniert, muss dein Handy ein paar Dinge erlauben. " +
            "Das dauert 2–3 Minuten. Du kannst jeden Schritt überspringen und später vom Startbildschirm aus nachholen."
    override val start = "Starten"

    override val serviceTitle = "Sperrdienst"
    override val serviceWhy = "Um ausgewählte Apps zu schließen, braucht Anti-Oblomov einen Android-Dienst für Bedienungshilfen."
    override val serviceDoesTitle = "Was der Dienst tut"
    override val serviceDoes = listOf(
        "Erkennt den Namen der App, die auf dem Bildschirm geöffnet wurde",
        "Ist für sie gerade Sperrzeit, zeigt er einen Bildschirm mit einem Zitat",
    )
    override val serviceDoesNotTitle = "Was der Dienst nicht tut"
    override val serviceDoesNot = listOf(
        "Liest keine Texte auf dem Bildschirm, keine Nachrichten und keine Passwörter",
        "Sendet nichts ins Internet — alles bleibt auf dem Handy",
    )
    override val serviceSteps = listOf(
        "Tippe auf „Bedienungshilfen öffnen“",
        "Suche „Anti-Oblomov: Sperre“ — meist unter „Heruntergeladene Apps“ oder „Installierte Dienste“",
        "Schalte ihn ein und bestätige. Das Handy bittet evtl., ein paar Sekunden zu warten — das ist normal",
    )
    override val serviceOn = "Dienst läuft"
    override val serviceOff = "Dienst ist noch nicht eingeschaltet"
    override val openAccessibility = "Bedienungshilfen öffnen"
    override val restrictedQuestion = "Schalter ist grau und lässt sich nicht einschalten?"
    override val openAppInfo = "„App-Info“ öffnen"

    override val notificationsTitle = "Benachrichtigungen"
    override val notificationsWhy = "Während einer Sperre – nach Zeitplan oder Timer – siehst du in der Benachrichtigungsleiste, wann sie endet und wie viel Zeit noch bleibt. Andere Benachrichtigungen schickt die App nicht."
    override val notificationsOn = "Benachrichtigungen erlaubt"
    override val notificationsOff = "Benachrichtigungen sind aus"
    override val allowNotifications = "Benachrichtigungen erlauben"
    override val openNotificationSettings = "Benachrichtigungseinstellungen öffnen"

    override val batteryTitle = "Akku"
    override val batteryWhy = "Um Akku zu sparen, stoppt das System Apps im Hintergrund — und die Sperre hört unbemerkt auf zu funktionieren."
    override val batteryOn = "Hintergrundaktivität erlaubt"
    override val batteryOff = "Hintergrundaktivität eingeschränkt"
    override val batteryHint = "Tippe auf die Schaltfläche und wähle im Fenster „Zulassen“. Öffnet sich stattdessen eine Liste, suche „Anti-Oblomov“ und wähle „Nicht optimieren“ oder „Nicht eingeschränkt“."
    override val allowBackground = "Hintergrundaktivität erlauben"

    override val usageTitle = "Statistik „Vorher / nachher“"
    override val usageWhy =
        "Auf dem Startbildschirm siehst du, wie viel Zeit du vor Anti-Oblomov in den ausgewählten Apps verbracht hast, " +
            "wie viel jetzt — und wie viel Zeit du zurückgewonnen hast."
    override val usageReadsTitle = "Was dafür gelesen wird"
    override val usageReads = listOf(
        "Wie viele Minuten jede App auf dem Bildschirm war — dieselbe Statistik wie in „Digital Wellbeing“",
        "Die Daten bleiben auf dem Handy und werden nirgendwohin gesendet",
    )
    override val usageSteps = listOf(
        "Tippe auf „Einstellungen öffnen“",
        "Suche „Anti-Oblomov“ und erlaube den Zugriff auf Nutzungsdaten",
    )
    override val usageOn = "Zugriff auf Statistik erteilt"
    override val usageOff = "Noch kein Zugriff auf Statistik"
    override val openSettings = "Einstellungen öffnen"

    override val finishDone = "Handy ist eingerichtet"
    override val finishAlmost = "Fast fertig"
    override val finishSkipped = "Übersprungenes kannst du später einrichten: auf dem Startbildschirm in der Karte „Handy einrichten“."
    override val finishRest = "Jetzt nur noch auswählen, was gesperrt wird, und einen Zeitplan festlegen oder einen Timer starten."
    override val chooseApps = "Apps auswählen"
    override val toHome = "Zum Startbildschirm"

    override val guideFallback = "Wenn sich die Seite „App-Info“ geöffnet hat, such diese Einstellung dort."
    override val markDone = "Erledigt"
    override val notDone = "Nicht erledigt"

    override val cardTitle = "Handy einrichten"
    override val cardHint = "Ohne diese Einstellungen kann sich die Sperre von selbst abschalten und „Vorher / nachher“ wird evtl. nicht gezählt."
    override val cardSetUp = "Einrichten"
    override val cardRedo = "Einrichtung wiederholen"

    override val consentTitle = "Sperrdienst: was er sieht"
    override val consentIntro =
        "Anti-Oblomov nutzt den Android-Dienst für Bedienungshilfen (Accessibility Service), " +
            "um ausgewählte Apps während der Sperre zu schließen."
    override val consentData = "Welche Daten: Der Dienst erkennt, welche App gerade auf dem Bildschirm geöffnet ist (ihren Systemnamen)."
    override val consentWhy =
        "Wozu: Steht diese App auf deiner Sperrliste und ist gerade Sperrzeit, " +
            "wird darüber ein Bildschirm mit einem Zitat angezeigt."
    override val consentNot =
        "Der Dienst liest keine Texte auf dem Bildschirm, keine Nachrichten und keine Passwörter, speichert nichts über deine Aktionen " +
            "und überträgt nichts: Die App hat keinen Internetzugriff."
    override val consentOff = "Du kannst den Dienst jederzeit in den Bedienungshilfen-Einstellungen des Handys ausschalten."
    override val consentAccept = "Akzeptieren"
    override val consentLater = "Nicht jetzt"
}

// =====================================================================================
// Türkçe (sen)
// =====================================================================================

object SetupTr : SetupStrings {
    override val itemService = "Engelleme hizmeti"
    override val itemNotifications = "Zamanlayıcı bildirimleri"
    override val itemBattery = "Kısıtlamasız pil"
    override val itemAutostart = "Otomatik başlatma"
    override val itemUsage = "İstatistik erişimi"
    override val summaryServiceOff = "hizmet çalışmıyor"
    override val summaryAllOk = "her şey yolunda"
    override fun summaryMissing(n: Int) = "$n ayar atlandı"

    override val autostartXiaomi = GuideText(
        title = "Otomatik başlatma",
        why = "Bu olmadan HyperOS ve MIUI, yeniden başlatmadan sonra engelleme hizmetini başlatmaz.",
        steps = listOf(
            "“Güvenlik” uygulamasının otomatik başlatma listesi açılacak",
            "“Anti-Oblomov”u bul ve anahtarı aç",
        ),
        button = "Otomatik başlatmayı aç",
    )
    override val autostartSamsung = GuideText(
        title = "Uygulamayı uyutma",
        why = "Samsung uzun süredir kullanmadığın uygulamaları uyku moduna alır ve engelleme hizmeti çalışmayı bırakır.",
        steps = listOf(
            "Ayarlar → Cihaz bakımı (veya Pil) → arka plan kullanım sınırları",
            "Hiç uyku moduna geçmeyen uygulamalar → “+” → “Anti-Oblomov”",
            "“Anti-Oblomov” uyku modundaki uygulamalar listesindeyse oradan çıkar",
        ),
        button = "Pil ayarlarını aç",
    )
    override val autostartOppo = GuideText(
        title = "Otomatik başlatma",
        why = "Bu izin olmadan ColorOS ve Realme UI, yeniden başlatmadan sonra hizmeti başlatmaz.",
        steps = listOf(
            "Otomatik başlatma listesinde “Anti-Oblomov”u bul ve aç",
            "Liste yoksa: Ayarlar → Uygulamalar → Anti-Oblomov → pil kullanımı → otomatik başlatmaya ve arka plan etkinliğine izin ver",
        ),
        button = "Otomatik başlatmayı aç",
    )
    private fun appLaunch(os: String) = GuideText(
        title = "Uygulama başlatma",
        why = "$os hangi uygulamaların arka planda çalışacağına kendisi karar verir ve engelleme hizmetini durdurabilir.",
        steps = listOf(
            "“Anti-Oblomov”u bul ve otomatik yönetimi kapat",
            "Açılan pencerede üçünü de aç: otomatik başlatma, ikincil başlatma, arka planda çalışma",
        ),
        button = "Uygulama başlatmayı aç",
    )
    override val autostartHuawei = appLaunch("EMUI")
    override val autostartHonor = appLaunch("MagicOS")
    override val autostartVivo = GuideText(
        title = "Otomatik başlatma",
        why = "Bu izin olmadan Funtouch OS ve OriginOS, yeniden başlatmadan sonra hizmeti başlatmaz.",
        steps = listOf(
            "Otomatik başlatma listesinde “Anti-Oblomov”u bul ve aç",
            "Ayrıca: Ayarlar → Pil → arka planda yüksek güç tüketimi → “Anti-Oblomov”a izin ver",
        ),
        button = "Otomatik başlatmayı aç",
    )
    override val autostartTranssion = GuideText(
        title = "Otomatik başlatma",
        why = "Bu izin olmadan HiOS ve XOS, yeniden başlatmadan sonra hizmeti başlatmaz.",
        steps = listOf(
            "“Phone Master”ın otomatik başlatma listesi açılacak — “Anti-Oblomov”u aç",
            "Açılmazsa: Phone Master → uygulama yönetimi → otomatik başlatma",
        ),
        button = "Otomatik başlatmayı aç",
    )
    override val batteryXiaomi = GuideText(
        title = "Pil tasarrufu",
        why = "HyperOS'un kendi pil tasarrufu var ve engelleme hizmetini durdurabilir.",
        steps = listOf(
            "“Anti-Oblomov” için pil tasarrufu ayarı açılacak",
            "Kısıtlamasız seçeneğini seç",
        ),
        button = "Pil tasarrufunu aç",
    )
    override val restrictedHelp = listOf(
        "Ayarlar → Uygulamalar → Anti-Oblomov",
        "Sağ üstte ⋮ → “Kısıtlanmış ayarlara izin ver”",
        "Buraya dön ve yine “Erişilebilirliği aç”a dokun",
    )

    override val welcomeTitle = "Anti-Oblomov'u kuralım"
    override val welcomeIntro =
        "Uygulama, seçtiğin sosyal ağları ve oyunları ders saatlerinde — programa ya da zamanlayıcıya göre — kapatır " +
            "ve yerine bir alıntı gösterir."
    override val welcomeHow =
        "Engellemenin her zaman çalışması için telefonun birkaç şeye izin vermesi gerekiyor. " +
            "2–3 dakika sürer. Her adımı atlayıp daha sonra ana ekrandan ona dönebilirsin."
    override val start = "Başla"

    override val serviceTitle = "Engelleme hizmeti"
    override val serviceWhy = "Seçilen uygulamaları kapatmak için Anti-Oblomov'un bir Android erişilebilirlik hizmetine ihtiyacı var."
    override val serviceDoesTitle = "Hizmet ne yapar"
    override val serviceDoes = listOf(
        "Ekranda açılan uygulamanın adını öğrenir",
        "O uygulama için engelleme zamanıysa alıntı içeren bir ekran gösterir",
    )
    override val serviceDoesNotTitle = "Hizmet ne yapmaz"
    override val serviceDoesNot = listOf(
        "Ekrandaki metni, mesajları ve şifreleri okumaz",
        "İnternete hiçbir şey göndermez — her şey telefonda kalır",
    )
    override val serviceSteps = listOf(
        "“Erişilebilirliği aç”a dokun",
        "“Anti-Oblomov: engelleme”yi bul — genellikle “İndirilen uygulamalar” veya “Yüklü hizmetler” bölümündedir",
        "Aç ve onayla. Telefon birkaç saniye beklemeni isteyebilir — bu normal",
    )
    override val serviceOn = "Hizmet çalışıyor"
    override val serviceOff = "Hizmet henüz açık değil"
    override val openAccessibility = "Erişilebilirliği aç"
    override val restrictedQuestion = "Anahtar gri ve açılmıyor mu?"
    override val openAppInfo = "Uygulama bilgilerini aç"

    override val notificationsTitle = "Bildirimler"
    override val notificationsWhy = "Engelleme sürerken — program ya da zamanlayıcıyla — bildirim panelinde ne zaman biteceği ve ne kadar kaldığı görünür. Uygulama başka bildirim göndermez."
    override val notificationsOn = "Bildirimlere izin verildi"
    override val notificationsOff = "Bildirimler kapalı"
    override val allowNotifications = "Bildirimlere izin ver"
    override val openNotificationSettings = "Bildirim ayarlarını aç"

    override val batteryTitle = "Pil"
    override val batteryWhy = "Pil tasarrufu için sistem arka planda çalışan uygulamaları durdurur — ve engelleme sessizce çalışmayı bırakır."
    override val batteryOn = "Arka planda çalışmaya izin verildi"
    override val batteryOff = "Arka planda çalışma kısıtlı"
    override val batteryHint = "Düğmeye dokun ve pencerede “İzin ver”i seç. Pencere yerine bir liste açılırsa “Anti-Oblomov”u bul ve “Optimize etme” veya “Kısıtlanmamış”ı seç."
    override val allowBackground = "Arka planda çalışmaya izin ver"

    override val usageTitle = "“Önce / sonra” istatistiği"
    override val usageWhy =
        "Ana ekranda, Anti-Oblomov'dan önce seçilen uygulamalarda ne kadar zaman geçirdiğini, şimdi ne kadar geçirdiğini " +
            "ve ne kadar zamanı geri kazandığını göreceksin."
    override val usageReadsTitle = "Bunun için ne okunur"
    override val usageReads = listOf(
        "Her uygulamanın ekranda kaç dakika kaldığı — “Dijital Denge”deki istatistiğin aynısı",
        "Veriler telefonda kalır ve hiçbir yere gönderilmez",
    )
    override val usageSteps = listOf(
        "“Ayarları aç”a dokun",
        "“Anti-Oblomov”u bul ve kullanım erişimine izin ver",
    )
    override val usageOn = "İstatistik erişimi var"
    override val usageOff = "Henüz istatistik erişimi yok"
    override val openSettings = "Ayarları aç"

    override val finishDone = "Telefon ayarlandı"
    override val finishAlmost = "Neredeyse hazır"
    override val finishSkipped = "Atladıklarını daha sonra ayarlayabilirsin: ana ekrandaki “Telefon ayarları” kartından."
    override val finishRest = "Geriye neyin engelleneceğini seçmek ve bir program belirlemek ya da zamanlayıcı başlatmak kaldı."
    override val chooseApps = "Uygulamaları seç"
    override val toHome = "Ana ekrana dön"

    override val guideFallback = "Uygulama bilgileri sayfası açıldıysa bu ayarı orada ara."
    override val markDone = "Yapıldı"
    override val notDone = "Yapılmadı"

    override val cardTitle = "Telefon ayarları"
    override val cardHint = "Bu ayarlar olmadan engelleme kendiliğinden kapanabilir ve “Önce / sonra” sayılmayabilir."
    override val cardSetUp = "Ayarla"
    override val cardRedo = "Kurulumu yeniden yap"

    override val consentTitle = "Engelleme hizmeti: ne görür"
    override val consentIntro =
        "Anti-Oblomov, engelleme sırasında seçilen uygulamaları kapatmak için " +
            "Android erişilebilirlik hizmetini (Accessibility Service) kullanır."
    override val consentData = "Hangi veriler: hizmet, ekranda şu an hangi uygulamanın açık olduğunu (sistem adını) öğrenir."
    override val consentWhy =
        "Neden: bu uygulama engelleme listendeyse ve engelleme zamanıysa, " +
            "üzerinde alıntı içeren bir ekran gösterilir."
    override val consentNot =
        "Hizmet ekrandaki metni, mesajları ve şifreleri okumaz, yaptıkların hakkında hiçbir şey kaydetmez " +
            "ve hiçbir şey aktarmaz: uygulamanın internet erişimi yoktur."
    override val consentOff = "Hizmeti istediğin zaman telefonun erişilebilirlik ayarlarından kapatabilirsin."
    override val consentAccept = "Kabul ediyorum"
    override val consentLater = "Şimdi değil"
}

// =====================================================================================
// Қазақша (сіз — как «вы» в русском мастере)
// =====================================================================================

object SetupKk : SetupStrings {
    override val itemService = "Бұғаттау қызметі"
    override val itemNotifications = "Таймер хабарландырулары"
    override val itemBattery = "Батарея шектеусіз"
    override val itemAutostart = "Автоіске қосу"
    override val itemUsage = "Статистикаға рұқсат"
    override val summaryServiceOff = "қызмет жұмыс істемейді"
    override val summaryAllOk = "бәрі дұрыс"
    override fun summaryMissing(n: Int) = "$n баптау өткізіп жіберілді"

    override val autostartXiaomi = GuideText(
        title = "Автоіске қосу",
        why = "Онсыз HyperOS пен MIUI қайта жүктелгеннен кейін бұғаттау қызметін іске қоспайды.",
        steps = listOf(
            "«Қауіпсіздік» қолданбасындағы автоіске қосу тізімі ашылады",
            "«Анти-Обломов» қолданбасын тауып, қосқышты қосыңыз",
        ),
        button = "Автоіске қосуды ашу",
    )
    override val autostartSamsung = GuideText(
        title = "Қолданбаны ұйықтатпау",
        why = "Samsung ұзақ пайдаланылмаған қолданбаларды ұйқы режиміне жібереді — бұғаттау қызметі жұмысын тоқтатады.",
        steps = listOf(
            "Параметрлер → құрылғыға қызмет көрсету (немесе Батарея) → фондық пайдалану шектеулері",
            "Ешқашан ұйықтамайтын қолданбалар → «+» → «Анти-Обломов»",
            "Егер «Анти-Обломов» ұйықтап тұрған қолданбалар тізімінде болса, сол жерден алып тастаңыз",
        ),
        button = "Батарея параметрлерін ашу",
    )
    override val autostartOppo = GuideText(
        title = "Автоіске қосу",
        why = "Бұл рұқсатсыз ColorOS пен Realme UI қайта жүктелгеннен кейін қызметті іске қоспайды.",
        steps = listOf(
            "Автоіске қосу тізімінен «Анти-Обломов» қолданбасын тауып, қосыңыз",
            "Тізім болмаса: Параметрлер → Қолданбалар → Анти-Обломов → батареяны пайдалану → автоіске қосуға және фондық жұмысқа рұқсат беріңіз",
        ),
        button = "Автоіске қосуды ашу",
    )
    private fun appLaunch(os: String) = GuideText(
        title = "Қолданбаларды іске қосу",
        why = "$os қай қолданбалар фонда жұмыс істейтінін өзі шешеді және бұғаттау қызметін тоқтатуы мүмкін.",
        steps = listOf(
            "«Анти-Обломов» қолданбасын тауып, автоматты басқаруды өшіріңіз",
            "Ашылған терезеде үшеуін де қосыңыз: автоіске қосу, жанама іске қосу, фондық жұмыс",
        ),
        button = "Қолданбаларды іске қосуды ашу",
    )
    override val autostartHuawei = appLaunch("EMUI")
    override val autostartHonor = appLaunch("MagicOS")
    override val autostartVivo = GuideText(
        title = "Автоіске қосу",
        why = "Бұл рұқсатсыз Funtouch OS пен OriginOS қайта жүктелгеннен кейін қызметті іске қоспайды.",
        steps = listOf(
            "Автоіске қосу тізімінен «Анти-Обломов» қолданбасын тауып, қосыңыз",
            "Сондай-ақ: Параметрлер → Батарея → фонда көп энергия тұтыну → «Анти-Обломов» қолданбасына рұқсат беріңіз",
        ),
        button = "Автоіске қосуды ашу",
    )
    override val autostartTranssion = GuideText(
        title = "Автоіске қосу",
        why = "Бұл рұқсатсыз HiOS пен XOS қайта жүктелгеннен кейін қызметті іске қоспайды.",
        steps = listOf(
            "«Phone Master» қолданбасындағы автоіске қосу тізімі ашылады — «Анти-Обломов» қолданбасын қосыңыз",
            "Ашылмаса: Phone Master → қолданбаларды басқару → автоіске қосу",
        ),
        button = "Автоіске қосуды ашу",
    )
    override val batteryXiaomi = GuideText(
        title = "Батареяны үнемдеу",
        why = "HyperOS-тың өз батарея үнемдеуі бар — ол бұғаттау қызметін тоқтатуы мүмкін.",
        steps = listOf(
            "«Анти-Обломов» үшін батареяны үнемдеу параметрі ашылады",
            "Шектеусіз нұсқаны таңдаңыз",
        ),
        button = "Батареяны үнемдеуді ашу",
    )
    override val restrictedHelp = listOf(
        "Параметрлер → Қолданбалар → Анти-Обломов",
        "Жоғарғы оң жақтағы ⋮ → шектелген параметрлерге рұқсат беру",
        "Осында оралып, «Арнайы мүмкіндіктерді ашу» түймесін қайта басыңыз",
    )

    override val welcomeTitle = "«Анти-Обломовты» баптайық"
    override val welcomeIntro =
        "Қолданба таңдалған әлеуметтік желілер мен ойындарды оқу уақытында — кесте немесе таймер бойынша — жабады " +
            "және олардың орнына дәйексөз көрсетеді."
    override val welcomeHow =
        "Бұғаттау әрқашан жұмыс істеуі үшін телефонда бірнеше нәрсеге рұқсат беру керек. " +
            "Бұл 2–3 минут алады. Кез келген қадамды өткізіп жіберіп, кейін оған басты экраннан оралуға болады."
    override val start = "Бастау"

    override val serviceTitle = "Бұғаттау қызметі"
    override val serviceWhy = "Таңдалған қолданбаларды жабу үшін «Анти-Обломовқа» Android арнайы мүмкіндіктер қызметі керек."
    override val serviceDoesTitle = "Қызмет не істейді"
    override val serviceDoes = listOf(
        "Экранда ашылған қолданбаның атауын анықтайды",
        "Егер ол үшін қазір бұғаттау уақыты болса, дәйексөзі бар экранды көрсетеді",
    )
    override val serviceDoesNotTitle = "Қызмет не істемейді"
    override val serviceDoesNot = listOf(
        "Экрандағы мәтінді, хабарларды және құпиясөздерді оқымайды",
        "Интернетке ештеңе жібермейді — бәрі телефонда қалады",
    )
    override val serviceSteps = listOf(
        "«Арнайы мүмкіндіктерді ашу» түймесін басыңыз",
        "«Анти-Обломов: бұғаттау» қызметін табыңыз — ол әдетте жүктеп алынған қолданбалар немесе орнатылған қызметтер бөлімінде болады",
        "Қосып, растаңыз. Телефон бірнеше секунд күтуді сұрауы мүмкін — бұл қалыпты жағдай",
    )
    override val serviceOn = "Қызмет жұмыс істеп тұр"
    override val serviceOff = "Қызмет әлі қосылмаған"
    override val openAccessibility = "Арнайы мүмкіндіктерді ашу"
    override val restrictedQuestion = "Қосқыш сұр түсті және қосылмай ма?"
    override val openAppInfo = "Қолданба туралы ақпаратты ашу"

    override val notificationsTitle = "Хабарландырулар"
    override val notificationsWhy = "Бұғаттау жүріп жатқанда — кесте не таймер бойынша — хабарландыру тақтасында оның қашан аяқталатыны және қанша уақыт қалғаны көрінеді. Қолданба басқа хабарландыру жібермейді."
    override val notificationsOn = "Хабарландыруларға рұқсат берілген"
    override val notificationsOff = "Хабарландырулар өшірулі"
    override val allowNotifications = "Хабарландыруларға рұқсат беру"
    override val openNotificationSettings = "Хабарландыру параметрлерін ашу"

    override val batteryTitle = "Батарея"
    override val batteryWhy = "Зарядты үнемдеу үшін жүйе фонда жұмыс істейтін қолданбаларды тоқтатады — сөйтіп бұғаттау байқатпай жұмысын тоқтатады."
    override val batteryOn = "Фондық жұмысқа рұқсат берілген"
    override val batteryOff = "Фондық жұмыс шектелген"
    override val batteryHint = "Түймені басып, терезеде рұқсат беруді таңдаңыз. Терезенің орнына тізім ашылса, «Анти-Обломов» қолданбасын тауып, оңтайландырмау немесе шектеусіз нұсқасын таңдаңыз."
    override val allowBackground = "Фондық жұмысқа рұқсат беру"

    override val usageTitle = "«Бұрын / қазір» статистикасы"
    override val usageWhy =
        "Басты экранда «Анти-Обломовқа» дейін таңдалған қолданбаларда қанша уақыт өткізгеніңіз, қазір қанша өткізетініңіз " +
            "және қанша уақытты қайтарып алғаныңыз көрінеді."
    override val usageReadsTitle = "Ол үшін не оқылады"
    override val usageReads = listOf(
        "Әр қолданба экранда неше минут болғаны — телефондағы экран уақыты статистикасының дәл өзі",
        "Деректер телефонда қалады және ешқайда жіберілмейді",
    )
    override val usageSteps = listOf(
        "«Параметрлерді ашу» түймесін басыңыз",
        "«Анти-Обломов» қолданбасын тауып, пайдалану тарихына рұқсатты қосыңыз",
    )
    override val usageOn = "Статистикаға рұқсат бар"
    override val usageOff = "Статистикаға рұқсат әлі жоқ"
    override val openSettings = "Параметрлерді ашу"

    override val finishDone = "Телефон бапталды"
    override val finishAlmost = "Дайын болуға аз қалды"
    override val finishSkipped = "Өткізіп жіберілгенді кейін баптауға болады: басты экрандағы «Телефонды баптау» карточкасында."
    override val finishRest = "Енді нені бұғаттау керектігін таңдап, кесте құру немесе таймерді іске қосу қалды."
    override val chooseApps = "Қолданбаларды таңдау"
    override val toHome = "Басты экранға"

    override val guideFallback = "Егер қолданба туралы бет ашылса, бұл параметрді сол жерден іздеңіз."
    override val markDone = "Орындалды"
    override val notDone = "Орындалмады"

    override val cardTitle = "Телефонды баптау"
    override val cardHint = "Бұл баптауларсыз бұғаттау өздігінен өшіп қалуы, ал «Бұрын / қазір» есептелмеуі мүмкін."
    override val cardSetUp = "Баптау"
    override val cardRedo = "Баптауды қайта өту"

    override val consentTitle = "Бұғаттау қызметі: ол нені көреді"
    override val consentIntro =
        "«Анти-Обломов» бұғаттау кезінде таңдалған қолданбаларды жабу үшін " +
            "Android арнайы мүмкіндіктер қызметін (Accessibility Service) пайдаланады."
    override val consentData = "Қандай деректер: қызмет экранда қазір қай қолданба ашық екенін (оның жүйелік атауын) анықтайды."
    override val consentWhy =
        "Не үшін: егер бұл қолданба сіздің бұғаттау тізіміңізде болса және қазір бұғаттау уақыты болса, " +
            "оның үстінен дәйексөзі бар экран көрсетіледі."
    override val consentNot =
        "Қызмет экрандағы мәтінді, хабарларды және құпиясөздерді оқымайды, әрекеттеріңіз туралы ештеңе сақтамайды " +
            "және ештеңе жібермейді: қолданбаның интернетке рұқсаты жоқ."
    override val consentOff = "Қызметті кез келген уақытта телефонның арнайы мүмкіндіктер параметрлерінен өшіруге болады."
    override val consentAccept = "Қабылдаймын"
    override val consentLater = "Қазір емес"
}

// =====================================================================================
// O‘zbekcha (lotin, siz — как «вы» в русском мастере)
// =====================================================================================

object SetupUz : SetupStrings {
    override val itemService = "Bloklash xizmati"
    override val itemNotifications = "Taymer bildirishnomalari"
    override val itemBattery = "Batareya cheklovsiz"
    override val itemAutostart = "Avtoishga tushirish"
    override val itemUsage = "Statistikaga ruxsat"
    override val summaryServiceOff = "xizmat ishlamayapti"
    override val summaryAllOk = "hammasi joyida"
    override fun summaryMissing(n: Int) = "$n ta sozlama o‘tkazib yuborildi"

    override val autostartXiaomi = GuideText(
        title = "Avtoishga tushirish",
        why = "Usiz HyperOS va MIUI qayta yoqilgandan keyin bloklash xizmatini ishga tushirmaydi.",
        steps = listOf(
            "“Xavfsizlik” ilovasidagi avtoishga tushirish ro‘yxati ochiladi",
            "“Anti-Oblomov”ni toping va uni yoqing",
        ),
        button = "Avtoishga tushirishni ochish",
    )
    override val autostartSamsung = GuideText(
        title = "Ilovani uxlatmaslik",
        why = "Samsung uzoq vaqt ishlatilmagan ilovalarni uyqu rejimiga o‘tkazadi — bloklash xizmati ishlashdan to‘xtaydi.",
        steps = listOf(
            "Sozlamalar → qurilmaga xizmat ko‘rsatish (yoki Batareya) → fonda foydalanish cheklovlari",
            "Hech qachon uxlamaydigan ilovalar → “+” → “Anti-Oblomov”",
            "Agar “Anti-Oblomov” uxlayotgan ilovalar ro‘yxatida bo‘lsa, uni u yerdan olib tashlang",
        ),
        button = "Batareya sozlamalarini ochish",
    )
    override val autostartOppo = GuideText(
        title = "Avtoishga tushirish",
        why = "Bu ruxsatsiz ColorOS va Realme UI qayta yoqilgandan keyin xizmatni ishga tushirmaydi.",
        steps = listOf(
            "Avtoishga tushirish ro‘yxatidan “Anti-Oblomov”ni toping va yoqing",
            "Ro‘yxat bo‘lmasa: Sozlamalar → Ilovalar → Anti-Oblomov → batareya sarfi → avtoishga tushirish va fonda ishlashga ruxsat bering",
        ),
        button = "Avtoishga tushirishni ochish",
    )
    private fun appLaunch(os: String) = GuideText(
        title = "Ilovalarni ishga tushirish",
        why = "$os qaysi ilovalar fonda ishlashini o‘zi hal qiladi va bloklash xizmatini to‘xtatib qo‘yishi mumkin.",
        steps = listOf(
            "“Anti-Oblomov”ni toping va avtomatik boshqaruvni o‘chiring",
            "Ochilgan oynada uchalasini ham yoqing: avtoishga tushirish, bilvosita ishga tushirish, fonda ishlash",
        ),
        button = "Ilovalarni ishga tushirishni ochish",
    )
    override val autostartHuawei = appLaunch("EMUI")
    override val autostartHonor = appLaunch("MagicOS")
    override val autostartVivo = GuideText(
        title = "Avtoishga tushirish",
        why = "Bu ruxsatsiz Funtouch OS va OriginOS qayta yoqilgandan keyin xizmatni ishga tushirmaydi.",
        steps = listOf(
            "Avtoishga tushirish ro‘yxatidan “Anti-Oblomov”ni toping va yoqing",
            "Yana: Sozlamalar → Batareya → fonda yuqori quvvat sarfi → “Anti-Oblomov”ga ruxsat bering",
        ),
        button = "Avtoishga tushirishni ochish",
    )
    override val autostartTranssion = GuideText(
        title = "Avtoishga tushirish",
        why = "Bu ruxsatsiz HiOS va XOS qayta yoqilgandan keyin xizmatni ishga tushirmaydi.",
        steps = listOf(
            "“Phone Master”dagi avtoishga tushirish ro‘yxati ochiladi — “Anti-Oblomov”ni yoqing",
            "Ochilmasa: Phone Master → ilovalarni boshqarish → avtoishga tushirish",
        ),
        button = "Avtoishga tushirishni ochish",
    )
    override val batteryXiaomi = GuideText(
        title = "Batareyani tejash",
        why = "HyperOS’ning o‘z batareya tejash tizimi bor — u bloklash xizmatini to‘xtatib qo‘yishi mumkin.",
        steps = listOf(
            "“Anti-Oblomov” uchun batareyani tejash sozlamasi ochiladi",
            "Cheklovsiz variantni tanlang",
        ),
        button = "Batareyani tejashni ochish",
    )
    override val restrictedHelp = listOf(
        "Sozlamalar → Ilovalar → Anti-Oblomov",
        "Yuqori o‘ngdagi ⋮ → cheklangan sozlamalarga ruxsat berish",
        "Bu yerga qayting va yana “Maxsus imkoniyatlarni ochish”ni bosing",
    )

    override val welcomeTitle = "Anti-Oblomov’ni sozlaymiz"
    override val welcomeIntro =
        "Ilova tanlangan ijtimoiy tarmoqlar va o‘yinlarni o‘qish vaqtida — jadval yoki taymer bo‘yicha — yopadi " +
            "va ularning o‘rniga iqtibos ko‘rsatadi."
    override val welcomeHow =
        "Bloklash doim ishlashi uchun telefonda bir nechta narsaga ruxsat berish kerak. " +
            "Bu 2–3 daqiqa oladi. Istalgan qadamni o‘tkazib yuborib, keyin unga bosh ekrandan qaytishingiz mumkin."
    override val start = "Boshlash"

    override val serviceTitle = "Bloklash xizmati"
    override val serviceWhy = "Tanlangan ilovalarni yopish uchun Anti-Oblomov’ga Android maxsus imkoniyatlar xizmati kerak."
    override val serviceDoesTitle = "Xizmat nima qiladi"
    override val serviceDoes = listOf(
        "Ekranda ochilgan ilova nomini aniqlaydi",
        "Agar u uchun hozir bloklash vaqti bo‘lsa, iqtibosli ekranni ko‘rsatadi",
    )
    override val serviceDoesNotTitle = "Xizmat nima qilmaydi"
    override val serviceDoesNot = listOf(
        "Ekrandagi matn, xabarlar va parollarni o‘qimaydi",
        "Internetga hech narsa yubormaydi — hammasi telefonda qoladi",
    )
    override val serviceSteps = listOf(
        "“Maxsus imkoniyatlarni ochish”ni bosing",
        "“Anti-Oblomov: bloklash”ni toping — odatda yuklab olingan ilovalar yoki o‘rnatilgan xizmatlar bo‘limida bo‘ladi",
        "Yoqing va tasdiqlang. Telefon bir necha soniya kutishni so‘rashi mumkin — bu normal holat",
    )
    override val serviceOn = "Xizmat ishlayapti"
    override val serviceOff = "Xizmat hali yoqilmagan"
    override val openAccessibility = "Maxsus imkoniyatlarni ochish"
    override val restrictedQuestion = "Tugma kulrang va yoqilmayaptimi?"
    override val openAppInfo = "Ilova haqida ma’lumotni ochish"

    override val notificationsTitle = "Bildirishnomalar"
    override val notificationsWhy = "Bloklash davom etayotganda — jadval yoki taymer bo‘yicha — bildirishnomalar panelida u qachon tugashi va qancha vaqt qolgani ko‘rinadi. Ilova boshqa bildirishnoma yubormaydi."
    override val notificationsOn = "Bildirishnomalarga ruxsat berilgan"
    override val notificationsOff = "Bildirishnomalar o‘chirilgan"
    override val allowNotifications = "Bildirishnomalarga ruxsat berish"
    override val openNotificationSettings = "Bildirishnoma sozlamalarini ochish"

    override val batteryTitle = "Batareya"
    override val batteryWhy = "Quvvatni tejash uchun tizim fonda ishlayotgan ilovalarni to‘xtatadi — va bloklash sezdirmasdan ishlashdan to‘xtaydi."
    override val batteryOn = "Fonda ishlashga ruxsat berilgan"
    override val batteryOff = "Fonda ishlash cheklangan"
    override val batteryHint = "Tugmani bosing va oynada ruxsat berishni tanlang. Oyna o‘rniga ro‘yxat ochilsa, “Anti-Oblomov”ni toping va optimallashtirmaslik yoki cheklovsiz variantni tanlang."
    override val allowBackground = "Fonda ishlashga ruxsat berish"

    override val usageTitle = "“Avval / hozir” statistikasi"
    override val usageWhy =
        "Bosh ekranda Anti-Oblomov’dan oldin tanlangan ilovalarda qancha vaqt o‘tkazganingiz, hozir qancha o‘tkazayotganingiz " +
            "va qancha vaqtni qaytarib olganingiz ko‘rinadi."
    override val usageReadsTitle = "Buning uchun nima o‘qiladi"
    override val usageReads = listOf(
        "Har bir ilova ekranda necha daqiqa bo‘lgani — telefondagi ekran vaqti statistikasining aynan o‘zi",
        "Ma’lumotlar telefonda qoladi va hech qayerga yuborilmaydi",
    )
    override val usageSteps = listOf(
        "“Sozlamalarni ochish”ni bosing",
        "“Anti-Oblomov”ni toping va foydalanish tarixiga ruxsatni yoqing",
    )
    override val usageOn = "Statistikaga ruxsat bor"
    override val usageOff = "Statistikaga hali ruxsat yo‘q"
    override val openSettings = "Sozlamalarni ochish"

    override val finishDone = "Telefon sozlandi"
    override val finishAlmost = "Deyarli tayyor"
    override val finishSkipped = "O‘tkazib yuborilganlarni keyinroq sozlash mumkin: bosh ekrandagi “Telefonni sozlash” kartasida."
    override val finishRest = "Endi nimani bloklashni tanlash va jadval tuzish yoki taymerni ishga tushirish qoldi."
    override val chooseApps = "Ilovalarni tanlash"
    override val toHome = "Bosh ekranga"

    override val guideFallback = "Agar ilova haqida sahifa ochilsa, bu sozlamani o‘sha yerdan qidiring."
    override val markDone = "Bajarildi"
    override val notDone = "Bajarilmagan"

    override val cardTitle = "Telefonni sozlash"
    override val cardHint = "Bu sozlamalarsiz bloklash o‘z-o‘zidan o‘chib qolishi, “Avval / hozir” esa hisoblanmasligi mumkin."
    override val cardSetUp = "Sozlash"
    override val cardRedo = "Sozlashni qaytadan o‘tish"

    override val consentTitle = "Bloklash xizmati: u nimani ko‘radi"
    override val consentIntro =
        "Anti-Oblomov bloklash vaqtida tanlangan ilovalarni yopish uchun " +
            "Android maxsus imkoniyatlar xizmatidan (Accessibility Service) foydalanadi."
    override val consentData = "Qanday ma’lumotlar: xizmat ekranda hozir qaysi ilova ochiqligini (uning tizimdagi nomini) aniqlaydi."
    override val consentWhy =
        "Nima uchun: agar bu ilova bloklash ro‘yxatingizda bo‘lsa va hozir bloklash vaqti bo‘lsa, " +
            "uning ustida iqtibosli ekran ko‘rsatiladi."
    override val consentNot =
        "Xizmat ekrandagi matn, xabarlar va parollarni o‘qimaydi, harakatlaringiz haqida hech narsa saqlamaydi " +
            "va hech narsa uzatmaydi: ilovada internetga ruxsat yo‘q."
    override val consentOff = "Xizmatni istalgan vaqtda telefonning maxsus imkoniyatlar sozlamalarida o‘chirib qo‘yish mumkin."
    override val consentAccept = "Qabul qilaman"
    override val consentLater = "Hozir emas"
}

// =====================================================================================
// 中文（简体，你）
// =====================================================================================

object SetupZh : SetupStrings {
    override val itemService = "屏蔽服务"
    override val itemNotifications = "计时器通知"
    override val itemBattery = "电池不受限制"
    override val itemAutostart = "自启动"
    override val itemUsage = "使用情况访问权限"
    override val summaryServiceOff = "服务未运行"
    override val summaryAllOk = "一切正常"
    override fun summaryMissing(n: Int) = "已跳过 $n 项设置"

    override val autostartXiaomi = GuideText(
        title = "自启动",
        why = "没有它，HyperOS 和 MIUI 在重启后不会启动屏蔽服务。",
        steps = listOf(
            "将打开“手机管家”（或“安全中心”）中的“自启动”列表",
            "找到“反奥勃洛莫夫”并打开开关",
        ),
        button = "打开“自启动”",
    )
    override val autostartSamsung = GuideText(
        title = "不让应用休眠",
        why = "三星会让长时间未使用的应用进入休眠，屏蔽服务随之停止工作。",
        steps = listOf(
            "设置 → 设备维护（或电池）→ 后台使用限制",
            "从不休眠的应用 → “+” → “反奥勃洛莫夫”",
            "如果“反奥勃洛莫夫”在休眠应用列表中，请将其移除",
        ),
        button = "打开电池设置",
    )
    override val autostartOppo = GuideText(
        title = "自启动",
        why = "没有这项权限，ColorOS 和 realme UI 在重启后不会启动服务。",
        steps = listOf(
            "在自启动列表中找到“反奥勃洛莫夫”并打开",
            "如果没有列表：设置 → 应用 → 反奥勃洛莫夫 → 耗电管理 → 允许自启动和后台运行",
        ),
        button = "打开自启动",
    )
    private fun appLaunch(os: String) = GuideText(
        title = "应用启动管理",
        why = "$os 会自行决定哪些应用可以在后台运行，可能会停止屏蔽服务。",
        steps = listOf(
            "找到“反奥勃洛莫夫”，关闭“自动管理”",
            "在弹出的窗口中打开全部三项：“允许自启动”“允许关联启动”“允许后台活动”",
        ),
        button = "打开“应用启动管理”",
    )
    override val autostartHuawei = appLaunch("EMUI")
    override val autostartHonor = appLaunch("MagicOS")
    override val autostartVivo = GuideText(
        title = "自启动",
        why = "没有这项权限，Funtouch OS 和 OriginOS 在重启后不会启动服务。",
        steps = listOf(
            "在自启动列表中找到“反奥勃洛莫夫”并打开",
            "另外：设置 → 电池 → “后台高耗电” → 允许“反奥勃洛莫夫”",
        ),
        button = "打开自启动",
    )
    override val autostartTranssion = GuideText(
        title = "自启动",
        why = "没有这项权限，HiOS 和 XOS 在重启后不会启动服务。",
        steps = listOf(
            "将打开“Phone Master”的自启动列表，请打开“反奥勃洛莫夫”",
            "如果没有打开：Phone Master → 应用管理 → 自启动",
        ),
        button = "打开自启动",
    )
    override val batteryXiaomi = GuideText(
        title = "省电策略",
        why = "HyperOS 有自己的省电机制，可能会停止屏蔽服务。",
        steps = listOf(
            "将打开“反奥勃洛莫夫”的“省电策略”",
            "选择“无限制”",
        ),
        button = "打开“省电策略”",
    )
    override val restrictedHelp = listOf(
        "设置 → 应用 → 反奥勃洛莫夫",
        "右上角 ⋮ → 允许受限制的设置",
        "回到这里，再次点按“打开无障碍”",
    )

    override val welcomeTitle = "设置“反奥勃洛莫夫”"
    override val welcomeIntro = "本应用会在学习时间（按日程或计时器）关闭你选择的社交网络和游戏，并显示一句名言。"
    override val welcomeHow = "为了让屏蔽始终有效，需要在手机上允许几项权限。只需 2–3 分钟。任何一步都可以跳过，之后再从主屏幕回来设置。"
    override val start = "开始"

    override val serviceTitle = "屏蔽服务"
    override val serviceWhy = "为了关闭所选应用，“反奥勃洛莫夫”需要使用 Android 无障碍服务。"
    override val serviceDoesTitle = "服务会做什么"
    override val serviceDoes = listOf(
        "识别屏幕上打开的应用名称",
        "如果该应用正处于屏蔽时间，显示一个带名言的界面",
    )
    override val serviceDoesNotTitle = "服务不会做什么"
    override val serviceDoesNot = listOf(
        "不读取屏幕上的文字、消息和密码",
        "不向互联网发送任何内容——一切都留在手机上",
    )
    override val serviceSteps = listOf(
        "点按“打开无障碍”",
        "找到“反奥勃洛莫夫：屏蔽”，通常在“已下载的应用”或“已安装的服务”中",
        "打开并确认。手机可能会让你等几秒钟，这是正常的",
    )
    override val serviceOn = "服务正在运行"
    override val serviceOff = "服务尚未开启"
    override val openAccessibility = "打开无障碍"
    override val restrictedQuestion = "开关是灰色的，无法打开？"
    override val openAppInfo = "打开“应用信息”"

    override val notificationsTitle = "通知"
    override val notificationsWhy = "屏蔽期间（按时间表或计时器），通知栏会显示结束时间和剩余时间。应用不会发送其他通知。"
    override val notificationsOn = "已允许通知"
    override val notificationsOff = "通知已关闭"
    override val allowNotifications = "允许通知"
    override val openNotificationSettings = "打开通知设置"

    override val batteryTitle = "电池"
    override val batteryWhy = "为了省电，系统会停止在后台运行的应用，屏蔽也就悄悄失效了。"
    override val batteryOn = "已允许后台运行"
    override val batteryOff = "后台运行受限"
    override val batteryHint = "点按按钮，在弹窗中选择“允许”。如果打开的是列表，请找到“反奥勃洛莫夫”，选择“不优化”或“不受限制”。"
    override val allowBackground = "允许后台运行"

    override val usageTitle = "“之前 / 现在”统计"
    override val usageWhy = "主屏幕会显示：使用“反奥勃洛莫夫”之前你在所选应用上花了多少时间，现在花多少，以及你找回了多少时间。"
    override val usageReadsTitle = "为此会读取什么"
    override val usageReads = listOf(
        "每个应用在屏幕上停留了多少分钟——与手机自带的屏幕使用时间统计相同",
        "数据只保存在手机上，不会发送到任何地方",
    )
    override val usageSteps = listOf(
        "点按“打开设置”",
        "找到“反奥勃洛莫夫”，开启使用情况访问权限",
    )
    override val usageOn = "已获得使用情况访问权限"
    override val usageOff = "尚未获得使用情况访问权限"
    override val openSettings = "打开设置"

    override val finishDone = "手机已设置好"
    override val finishAlmost = "快完成了"
    override val finishSkipped = "跳过的项目可以稍后设置：见主屏幕上的“手机设置”卡片。"
    override val finishRest = "最后只需选择要屏蔽的内容，然后设定日程或启动计时器。"
    override val chooseApps = "选择应用"
    override val toHome = "返回主屏幕"

    override val guideFallback = "如果打开的是“应用信息”页面，请在那里查找此设置。"
    override val markDone = "已完成"
    override val notDone = "未完成"

    override val cardTitle = "手机设置"
    override val cardHint = "没有这些设置，屏蔽可能会自行关闭，“之前 / 现在”也可能无法统计。"
    override val cardSetUp = "去设置"
    override val cardRedo = "重新设置"

    override val consentTitle = "屏蔽服务：它能看到什么"
    override val consentIntro = "“反奥勃洛莫夫”使用 Android 无障碍服务（Accessibility Service），在屏蔽期间关闭所选应用。"
    override val consentData = "哪些数据：服务会识别当前屏幕上打开的是哪个应用（其系统名称）。"
    override val consentWhy = "用途：如果该应用在你的屏蔽列表中且正处于屏蔽时间，就会在它上面显示一个带名言的界面。"
    override val consentNot = "服务不读取屏幕上的文字、消息和密码，不保存任何关于你操作的信息，也不传输任何内容：本应用没有联网权限。"
    override val consentOff = "你可以随时在手机的无障碍设置中关闭该服务。"
    override val consentAccept = "接受"
    override val consentLater = "以后再说"
}
