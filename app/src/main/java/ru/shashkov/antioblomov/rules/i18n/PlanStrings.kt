package ru.shashkov.antioblomov.rules.i18n

/** Расписание, таймер, окно паузы, ввод времени, уведомление таймера. */
interface PlanStrings {
    // ---- Общее для расписания и таймера ----
    /** «Приложения для блокировки не выбраны — отметь их на вкладке «Приложения»» */
    val noAppsSelected: String

    // ---- Расписание ----
    val scheduleTitle: String
    val scheduleHint: String
    /** Тост после копирования */
    val copiedToWeekdays: String
    /** Подпись кнопки «⋯» для экранного диктора */
    val moreCopyToWeekdays: String
    /** Пункт меню «Копировать на будни (Пн–Пт)» */
    val copyToWeekdaysMenu: String
    /** Заголовок окна паузы при копировании на будни */
    val changeTodayScheduleQ: String
    val noBlockingThisDay: String
    /** «Удалить интервал 09:00–13:00?» — вопрос окна паузы */
    fun deleteIntervalQ(interval: String): String
    /** Подпись кнопки удаления для экранного диктора: «Удалить интервал 09:00–13:00» */
    fun deleteIntervalLabel(interval: String): String
    /** «Понедельник занят до полуночи — измени существующий интервал» */
    fun dayFullUntilMidnight(day: String): String
    /** Кнопка «+ Интервал» и заголовок диалога правки интервала */
    val interval: String
    val newInterval: String
    fun addIntervalQ(interval: String): String
    fun changeIntervalQ(interval: String): String
    val copyToWeekdaysQ: String
    /**
     * «Расписание на вторник, среда будет заменено расписанием дня «Понедельник».»
     * [days] — полные названия дней (как в CommonStrings.dayFull), [from] — тоже.
     */
    fun copyWillReplace(days: List<String>, from: String): String
    val replace: String
    /** Полоса суток — для экранного диктора */
    val dayBarEmpty: String
    fun dayBar(intervals: String): String
    /** «12:00–13:00 · перерыв, всё открыто» */
    fun breakRow(gap: String): String
    val enterStartTime: String
    val enterEndTime: String
    val start: String
    val end: String
    val timeInputHint: String
    /** «Блокировка 09:00–13:00» */
    fun blockingInterval(interval: String): String
    /** Ошибки проверки интервала (ScheduleEditor.check) */
    val timeOutOfDay: String
    val endAfterStart: String

    // ---- Таймер ----
    /** «Таймер» — заголовок экрана и название канала уведомлений */
    val timerTitle: String
    val timerHint: String
    /** «до 18:30», «до завтра 01:30» — [time] уже из TimeText.at */
    fun until(time: String): String
    val extend: String
    /** «Остановить · через 15 с» */
    fun stopIn(seconds: Int): String
    /** Надпись над чипами, заглавными */
    val howLong: String
    /** Чип «Своё» — узкий, коротко */
    val custom: String
    val customTitle: String
    val startTimer: String
    val extendBy: String
    /** Тост: «Продлено на 30 мин» */
    fun extendedBy(duration: String): String
    val stopTimerQ: String
    /** Циферблат для экранного диктора: «Таймер: осталось 1:59:59, до 18:30» */
    fun dialRunning(countdown: String, until: String): String
    val dialIdle: String
    /** Циферблат, таймер не идёт: заглавными */
    val notRunning: String
    val pickBelow: String
    val notificationsOff: String
    val enableNotifications: String

    // ---- Окно «на сколько» ----
    val hours: String
    val minutes: String
    val durationHint: String
    /** Ошибки проверки длительности (TimerRules.parseDuration) */
    val durationDigitsOnly: String
    val durationRange: String

    // ---- Окно паузы ----
    /** «Подумай 15 секунд — точно нужно?» */
    fun thinkSeconds(seconds: Int): String
    /** Цитата в кавычках языка: «…», “…”, „…“ */
    fun quoted(text: String): String
    val keepAsIs: String
    fun confirmIn(seconds: Int): String
    val confirm: String
    /** Кольцо отсчёта — для экранного диктора */
    val canConfirm: String
    fun secondsLeft(seconds: Int): String

    // ---- Уведомление таймера ----
    /** Название канала уведомлений о блокировке (по расписанию и таймеру) */
    val notificationTitle: String
    val channelDescription: String
}

object PlanRu : PlanStrings {
    override val noAppsSelected = "Приложения для блокировки не выбраны — отметь их на вкладке «Приложения»"
    override val scheduleTitle = "Расписание"
    override val scheduleHint = "Когда блокировать выбранные приложения. Между интервалами — перерыв, всё открыто."
    override val copiedToWeekdays = "Скопировано на будни"
    override val moreCopyToWeekdays = "Ещё: копировать на будни"
    override val copyToWeekdaysMenu = "Копировать на будни (Пн–Пт)"
    override val changeTodayScheduleQ = "Изменить расписание на сегодня?"
    override val noBlockingThisDay = "В этот день блокировки нет"
    override fun deleteIntervalQ(interval: String) = "Удалить интервал $interval?"
    override fun deleteIntervalLabel(interval: String) = "Удалить интервал $interval"
    override fun dayFullUntilMidnight(day: String) = "$day занят до полуночи — измени существующий интервал"
    override val interval = "Интервал"
    override val newInterval = "Новый интервал"
    override fun addIntervalQ(interval: String) = "Добавить интервал $interval?"
    override fun changeIntervalQ(interval: String) = "Изменить интервал на $interval?"
    override val copyToWeekdaysQ = "Копировать на будни?"
    override fun copyWillReplace(days: List<String>, from: String) =
        // Винительный падеж: «на среду, пятницу» (Среда → среду)
        "Расписание на " + days.joinToString(", ") { d -> d.lowercase().let { if (it.endsWith("а")) it.dropLast(1) + "у" else it } } +
            " будет заменено расписанием дня «$from»."
    override val replace = "Заменить"
    override val dayBarEmpty = "Полоса суток: блокировки нет"
    override fun dayBar(intervals: String) = "Полоса суток: $intervals"
    override fun breakRow(gap: String) = "$gap · перерыв, всё открыто"
    override val enterStartTime = "Введите время начала, например 930 или 0930"
    override val enterEndTime = "Введите время конца, например 1400"
    override val start = "Начало"
    override val end = "Конец"
    override val timeInputHint = "Только цифры: 930 → 9:30, 14 → 14:00. Конец 2400 или 0000 — до полуночи."
    override fun blockingInterval(interval: String) = "Блокировка $interval"
    override val timeOutOfDay = "Время вне суток"
    override val endAfterStart = "Конец должен быть позже начала"

    override val timerTitle = "Таймер"
    override val timerHint = "Разовая блокировка выбранных приложений. Действует поверх расписания и не меняет его."
    override fun until(time: String) = "до $time"
    override val extend = "Продлить"
    override fun stopIn(seconds: Int) = "Остановить · через $seconds с"
    override val howLong = "НА СКОЛЬКО"
    override val custom = "Своё"
    override val customTitle = "Своё время"
    override val startTimer = "Запустить"
    override val extendBy = "Продлить на"
    override fun extendedBy(duration: String) = "Продлено на $duration"
    override val stopTimerQ = "Остановить таймер?"
    override fun dialRunning(countdown: String, until: String) = "Таймер: осталось $countdown, $until"
    override val dialIdle = "Таймер не запущен"
    override val notRunning = "НЕ ЗАПУЩЕН"
    override val pickBelow = "выбери ниже, на сколько"
    override val notificationsOff = "Уведомление с обратным отсчётом выключено — таймер работает и без него."
    override val enableNotifications = "Включить уведомления"

    override val hours = "Часы"
    override val minutes = "Минуты"
    override val durationHint = "От 1 минуты до 12 часов. Пустое поле — 0."
    override val durationDigitsOnly = "Введите часы и минуты цифрами"
    override val durationRange = "От 1 минуты до 12 часов"

    override fun thinkSeconds(seconds: Int) = "Подумай $seconds секунд — точно нужно?"
    override fun quoted(text: String) = "«$text»"
    override val keepAsIs = "Не менять"
    override fun confirmIn(seconds: Int) = "Подтвердить через $seconds с"
    override val confirm = "Подтвердить"
    override val canConfirm = "Можно подтвердить"
    override fun secondsLeft(seconds: Int) = "Ещё $seconds " + when {
        seconds % 100 in 11..14 -> "секунд"
        seconds % 10 == 1 -> "секунду"
        seconds % 10 in 2..4 -> "секунды"
        else -> "секунд"
    }

    override val notificationTitle = "Блокировка"
    override val channelDescription = "Сколько осталось до конца блокировки — по расписанию и таймеру"
}

object PlanEn : PlanStrings {
    override val noAppsSelected = "No apps selected for blocking — pick them on the “Apps” tab"
    override val scheduleTitle = "Schedule"
    override val scheduleHint = "When to block the selected apps. Between intervals there’s a break, everything is open."
    override val copiedToWeekdays = "Copied to weekdays"
    override val moreCopyToWeekdays = "More: copy to weekdays"
    override val copyToWeekdaysMenu = "Copy to weekdays (Mon–Fri)"
    override val changeTodayScheduleQ = "Change today’s schedule?"
    override val noBlockingThisDay = "No blocking on this day"
    override fun deleteIntervalQ(interval: String) = "Delete interval $interval?"
    override fun deleteIntervalLabel(interval: String) = "Delete interval $interval"
    override fun dayFullUntilMidnight(day: String) = "$day is full until midnight — edit an existing interval"
    override val interval = "Interval"
    override val newInterval = "New interval"
    override fun addIntervalQ(interval: String) = "Add interval $interval?"
    override fun changeIntervalQ(interval: String) = "Change interval to $interval?"
    override val copyToWeekdaysQ = "Copy to weekdays?"
    override fun copyWillReplace(days: List<String>, from: String) =
        "The schedule for " + days.joinToString(", ") + " will be replaced with the “$from” schedule."
    override val replace = "Replace"
    override val dayBarEmpty = "Day bar: no blocking"
    override fun dayBar(intervals: String) = "Day bar: $intervals"
    override fun breakRow(gap: String) = "$gap · break, everything open"
    override val enterStartTime = "Enter a start time, e.g. 930 or 0930"
    override val enterEndTime = "Enter an end time, e.g. 1400"
    override val start = "Start"
    override val end = "End"
    override val timeInputHint = "Digits only: 930 → 9:30, 14 → 14:00. End 2400 or 0000 means until midnight."
    override fun blockingInterval(interval: String) = "Blocking $interval"
    override val timeOutOfDay = "Time is outside the day"
    override val endAfterStart = "End must be after start"

    override val timerTitle = "Timer"
    override val timerHint = "One-time blocking of the selected apps. Works on top of the schedule and doesn’t change it."
    override fun until(time: String) = "until $time"
    override val extend = "Extend"
    override fun stopIn(seconds: Int) = "Stop · in $seconds s"
    override val howLong = "HOW LONG"
    override val custom = "Custom"
    override val customTitle = "Custom time"
    override val startTimer = "Start"
    override val extendBy = "Extend by"
    override fun extendedBy(duration: String) = "Extended by $duration"
    override val stopTimerQ = "Stop the timer?"
    override fun dialRunning(countdown: String, until: String) = "Timer: $countdown left, $until"
    override val dialIdle = "Timer not running"
    override val notRunning = "NOT RUNNING"
    override val pickBelow = "choose how long below"
    override val notificationsOff = "The countdown notification is off — the timer works without it too."
    override val enableNotifications = "Turn on notifications"

    override val hours = "Hours"
    override val minutes = "Minutes"
    override val durationHint = "From 1 minute to 12 hours. Empty field = 0."
    override val durationDigitsOnly = "Enter hours and minutes as digits"
    override val durationRange = "From 1 minute to 12 hours"

    override fun thinkSeconds(seconds: Int) =
        "Think for $seconds " + (if (seconds == 1) "second" else "seconds") + " — do you really need this?"
    override fun quoted(text: String) = "“$text”"
    override val keepAsIs = "Keep as is"
    override fun confirmIn(seconds: Int) = "Confirm in $seconds s"
    override val confirm = "Confirm"
    override val canConfirm = "You can confirm now"
    override fun secondsLeft(seconds: Int) = if (seconds == 1) "1 second left" else "$seconds seconds left"

    override val notificationTitle = "Blocking"
    override val channelDescription = "Time left until blocking ends — schedule and timer"
}

object PlanEs : PlanStrings {
    override val noAppsSelected = "No hay apps elegidas para bloquear: márcalas en la pestaña «Apps»"
    override val scheduleTitle = "Horario"
    override val scheduleHint = "Cuándo bloquear las apps elegidas. Entre intervalos hay un descanso, todo abierto."
    override val copiedToWeekdays = "Copiado a los días laborables"
    override val moreCopyToWeekdays = "Más: copiar a días laborables"
    override val copyToWeekdaysMenu = "Copiar a días laborables (Lun–Vie)"
    override val changeTodayScheduleQ = "¿Cambiar el horario de hoy?"
    override val noBlockingThisDay = "Este día no hay bloqueo"
    override fun deleteIntervalQ(interval: String) = "¿Eliminar el intervalo $interval?"
    override fun deleteIntervalLabel(interval: String) = "Eliminar el intervalo $interval"
    override fun dayFullUntilMidnight(day: String) = "$day está ocupado hasta medianoche: cambia un intervalo existente"
    override val interval = "Intervalo"
    override val newInterval = "Nuevo intervalo"
    override fun addIntervalQ(interval: String) = "¿Añadir el intervalo $interval?"
    override fun changeIntervalQ(interval: String) = "¿Cambiar el intervalo a $interval?"
    override val copyToWeekdaysQ = "¿Copiar a días laborables?"
    override fun copyWillReplace(days: List<String>, from: String) =
        "El horario de " + days.joinToString(", ") { it.lowercase() } + " se sustituirá por el del «${from.lowercase()}»."
    override val replace = "Reemplazar"
    override val dayBarEmpty = "Barra del día: sin bloqueo"
    override fun dayBar(intervals: String) = "Barra del día: $intervals"
    override fun breakRow(gap: String) = "$gap · descanso, todo abierto"
    override val enterStartTime = "Escribe la hora de inicio, p. ej. 930 o 0930"
    override val enterEndTime = "Escribe la hora de fin, p. ej. 1400"
    override val start = "Inicio"
    override val end = "Fin"
    override val timeInputHint = "Solo dígitos: 930 → 9:30, 14 → 14:00. Fin 2400 o 0000: hasta medianoche."
    override fun blockingInterval(interval: String) = "Bloqueo $interval"
    override val timeOutOfDay = "Hora fuera del día"
    override val endAfterStart = "El fin debe ser posterior al inicio"

    override val timerTitle = "Temporizador"
    override val timerHint = "Bloqueo puntual de las apps elegidas. Va por encima del horario y no lo cambia."
    override fun until(time: String) = "hasta $time"
    override val extend = "Prolongar"
    override fun stopIn(seconds: Int) = "Detener · en $seconds s"
    override val howLong = "DURACIÓN"
    override val custom = "Otro"
    override val customTitle = "Otra duración"
    override val startTimer = "Iniciar"
    override val extendBy = "Prolongar"
    override fun extendedBy(duration: String) = "Prolongado $duration"
    override val stopTimerQ = "¿Detener el temporizador?"
    override fun dialRunning(countdown: String, until: String) = "Temporizador: quedan $countdown, $until"
    override val dialIdle = "Temporizador sin iniciar"
    override val notRunning = "SIN INICIAR"
    override val pickBelow = "elige abajo cuánto tiempo"
    override val notificationsOff = "La notificación con cuenta atrás está desactivada: el temporizador funciona igual."
    override val enableNotifications = "Activar notificaciones"

    override val hours = "Horas"
    override val minutes = "Minutos"
    override val durationHint = "De 1 minuto a 12 horas. Campo vacío = 0."
    override val durationDigitsOnly = "Escribe las horas y los minutos con cifras"
    override val durationRange = "De 1 minuto a 12 horas"

    override fun thinkSeconds(seconds: Int) =
        "Piénsalo $seconds " + (if (seconds == 1) "segundo" else "segundos") + ": ¿de verdad lo necesitas?"
    override fun quoted(text: String) = "«$text»"
    override val keepAsIs = "No cambiar"
    override fun confirmIn(seconds: Int) = "Confirmar en $seconds s"
    override val confirm = "Confirmar"
    override val canConfirm = "Ya puedes confirmar"
    override fun secondsLeft(seconds: Int) = if (seconds == 1) "Queda 1 segundo" else "Quedan $seconds segundos"

    override val notificationTitle = "Bloqueo"
    override val channelDescription = "Tiempo hasta que termine el bloqueo, por horario y temporizador"
}

object PlanPt : PlanStrings {
    override val noAppsSelected = "Nenhum app escolhido para bloquear — marque-os na aba “Apps”"
    override val scheduleTitle = "Agenda"
    override val scheduleHint = "Quando bloquear os apps escolhidos. Entre os intervalos há uma pausa, tudo liberado."
    override val copiedToWeekdays = "Copiado para os dias úteis"
    override val moreCopyToWeekdays = "Mais: copiar para dias úteis"
    override val copyToWeekdaysMenu = "Copiar para dias úteis (Seg–Sex)"
    override val changeTodayScheduleQ = "Mudar a agenda de hoje?"
    override val noBlockingThisDay = "Nenhum bloqueio neste dia"
    override fun deleteIntervalQ(interval: String) = "Excluir o intervalo $interval?"
    override fun deleteIntervalLabel(interval: String) = "Excluir o intervalo $interval"
    override fun dayFullUntilMidnight(day: String) = "$day: sem horário livre até a meia-noite — altere um intervalo existente"
    override val interval = "Intervalo"
    override val newInterval = "Novo intervalo"
    override fun addIntervalQ(interval: String) = "Adicionar o intervalo $interval?"
    override fun changeIntervalQ(interval: String) = "Mudar o intervalo para $interval?"
    override val copyToWeekdaysQ = "Copiar para dias úteis?"
    override fun copyWillReplace(days: List<String>, from: String) =
        "A agenda de " + days.joinToString(", ") { it.lowercase() } + " será substituída pela de “${from.lowercase()}”."
    override val replace = "Substituir"
    override val dayBarEmpty = "Barra do dia: sem bloqueio"
    override fun dayBar(intervals: String) = "Barra do dia: $intervals"
    override fun breakRow(gap: String) = "$gap · pausa, tudo liberado"
    override val enterStartTime = "Digite o horário de início, ex.: 930 ou 0930"
    override val enterEndTime = "Digite o horário de término, ex.: 1400"
    override val start = "Início"
    override val end = "Fim"
    override val timeInputHint = "Só números: 930 → 9:30, 14 → 14:00. Fim 2400 ou 0000 — até a meia-noite."
    override fun blockingInterval(interval: String) = "Bloqueio $interval"
    override val timeOutOfDay = "Horário fora do dia"
    override val endAfterStart = "O fim deve ser depois do início"

    override val timerTitle = "Timer"
    override val timerHint = "Bloqueio avulso dos apps escolhidos. Vale por cima da agenda e não a altera."
    override fun until(time: String) = "até $time"
    override val extend = "Estender"
    override fun stopIn(seconds: Int) = "Parar · em $seconds s"
    override val howLong = "QUANTO TEMPO"
    override val custom = "Outro"
    override val customTitle = "Outro tempo"
    override val startTimer = "Iniciar"
    override val extendBy = "Estender por"
    override fun extendedBy(duration: String) = "Estendido por $duration"
    override val stopTimerQ = "Parar o timer?"
    override fun dialRunning(countdown: String, until: String) = "Timer: faltam $countdown, $until"
    override val dialIdle = "Timer não iniciado"
    override val notRunning = "NÃO INICIADO"
    override val pickBelow = "escolha abaixo quanto tempo"
    override val notificationsOff = "A notificação com contagem regressiva está desativada — o timer funciona sem ela."
    override val enableNotifications = "Ativar notificações"

    override val hours = "Horas"
    override val minutes = "Minutos"
    override val durationHint = "De 1 minuto a 12 horas. Campo vazio = 0."
    override val durationDigitsOnly = "Digite horas e minutos em números"
    override val durationRange = "De 1 minuto a 12 horas"

    override fun thinkSeconds(seconds: Int) =
        "Pense por $seconds " + (if (seconds == 1) "segundo" else "segundos") + " — precisa mesmo?"
    override fun quoted(text: String) = "“$text”"
    override val keepAsIs = "Não mudar"
    override fun confirmIn(seconds: Int) = "Confirmar em $seconds s"
    override val confirm = "Confirmar"
    override val canConfirm = "Já pode confirmar"
    override fun secondsLeft(seconds: Int) = if (seconds == 1) "Falta 1 segundo" else "Faltam $seconds segundos"

    override val notificationTitle = "Bloqueio"
    override val channelDescription = "Tempo até o fim do bloqueio, pela agenda e pelo timer"
}

object PlanDe : PlanStrings {
    override val noAppsSelected = "Keine Apps zum Sperren ausgewählt — markiere sie im Tab „Apps“"
    override val scheduleTitle = "Zeitplan"
    override val scheduleHint = "Wann die ausgewählten Apps gesperrt werden. Zwischen den Intervallen ist Pause, alles offen."
    override val copiedToWeekdays = "Auf Werktage kopiert"
    override val moreCopyToWeekdays = "Mehr: auf Werktage kopieren"
    override val copyToWeekdaysMenu = "Auf Werktage kopieren (Mo–Fr)"
    override val changeTodayScheduleQ = "Heutigen Zeitplan ändern?"
    override val noBlockingThisDay = "An diesem Tag keine Sperre"
    override fun deleteIntervalQ(interval: String) = "Intervall $interval löschen?"
    override fun deleteIntervalLabel(interval: String) = "Intervall $interval löschen"
    override fun dayFullUntilMidnight(day: String) = "$day ist bis Mitternacht belegt — ändere ein bestehendes Intervall"
    override val interval = "Intervall"
    override val newInterval = "Neues Intervall"
    override fun addIntervalQ(interval: String) = "Intervall $interval hinzufügen?"
    override fun changeIntervalQ(interval: String) = "Intervall auf $interval ändern?"
    override val copyToWeekdaysQ = "Auf Werktage kopieren?"
    override fun copyWillReplace(days: List<String>, from: String) =
        "Der Zeitplan für " + days.joinToString(", ") + " wird durch den von „$from“ ersetzt."
    override val replace = "Ersetzen"
    override val dayBarEmpty = "Tagesleiste: keine Sperre"
    override fun dayBar(intervals: String) = "Tagesleiste: $intervals"
    override fun breakRow(gap: String) = "$gap · Pause, alles offen"
    override val enterStartTime = "Gib die Startzeit ein, z. B. 930 oder 0930"
    override val enterEndTime = "Gib die Endzeit ein, z. B. 1400"
    override val start = "Beginn"
    override val end = "Ende"
    override val timeInputHint = "Nur Ziffern: 930 → 9:30, 14 → 14:00. Ende 2400 oder 0000 — bis Mitternacht."
    override fun blockingInterval(interval: String) = "Sperre $interval"
    override val timeOutOfDay = "Zeit außerhalb des Tages"
    override val endAfterStart = "Das Ende muss nach dem Beginn liegen"

    override val timerTitle = "Timer"
    override val timerHint = "Einmalige Sperre der ausgewählten Apps. Gilt zusätzlich zum Zeitplan und ändert ihn nicht."
    override fun until(time: String) = "bis $time"
    override val extend = "Verlängern"
    override fun stopIn(seconds: Int) = "Stoppen · in $seconds s"
    override val howLong = "WIE LANGE"
    override val custom = "Andere"
    override val customTitle = "Eigene Dauer"
    override val startTimer = "Starten"
    override val extendBy = "Verlängern um"
    override fun extendedBy(duration: String) = "Um $duration verlängert"
    override val stopTimerQ = "Timer stoppen?"
    override fun dialRunning(countdown: String, until: String) = "Timer: noch $countdown, $until"
    override val dialIdle = "Timer läuft nicht"
    override val notRunning = "NICHT GESTARTET"
    override val pickBelow = "wähle unten, wie lange"
    override val notificationsOff = "Die Benachrichtigung mit Countdown ist aus — der Timer läuft auch ohne sie."
    override val enableNotifications = "Benachrichtigungen aktivieren"

    override val hours = "Stunden"
    override val minutes = "Minuten"
    override val durationHint = "Von 1 Minute bis 12 Stunden. Leeres Feld = 0."
    override val durationDigitsOnly = "Gib Stunden und Minuten als Ziffern ein"
    override val durationRange = "Von 1 Minute bis 12 Stunden"

    override fun thinkSeconds(seconds: Int) =
        "Denk $seconds " + (if (seconds == 1) "Sekunde" else "Sekunden") + " nach — wirklich nötig?"
    override fun quoted(text: String) = "„$text“"
    override val keepAsIs = "Nicht ändern"
    override fun confirmIn(seconds: Int) = "Bestätigen in $seconds s"
    override val confirm = "Bestätigen"
    override val canConfirm = "Du kannst jetzt bestätigen"
    override fun secondsLeft(seconds: Int) = if (seconds == 1) "Noch 1 Sekunde" else "Noch $seconds Sekunden"

    override val notificationTitle = "Sperre"
    override val channelDescription = "Zeit bis zum Ende der Sperre – nach Zeitplan und Timer"
}

object PlanTr : PlanStrings {
    override val noAppsSelected = "Engellenecek uygulama seçilmedi — “Uygulamalar” sekmesinden işaretle"
    override val scheduleTitle = "Program"
    override val scheduleHint = "Seçilen uygulamalar ne zaman engellensin. Aralıkların arası mola, her şey açık."
    override val copiedToWeekdays = "Hafta içine kopyalandı"
    override val moreCopyToWeekdays = "Diğer: hafta içine kopyala"
    override val copyToWeekdaysMenu = "Hafta içine kopyala (Pzt–Cum)"
    override val changeTodayScheduleQ = "Bugünün programı değişsin mi?"
    override val noBlockingThisDay = "Bu gün engelleme yok"
    override fun deleteIntervalQ(interval: String) = "$interval aralığı silinsin mi?"
    override fun deleteIntervalLabel(interval: String) = "$interval aralığını sil"
    override fun dayFullUntilMidnight(day: String) = "$day gece yarısına kadar dolu — mevcut bir aralığı değiştir"
    override val interval = "Aralık"
    override val newInterval = "Yeni aralık"
    override fun addIntervalQ(interval: String) = "$interval aralığı eklensin mi?"
    override fun changeIntervalQ(interval: String) = "Aralık $interval olarak değiştirilsin mi?"
    override val copyToWeekdaysQ = "Hafta içine kopyalansın mı?"
    override fun copyWillReplace(days: List<String>, from: String) =
        days.joinToString(", ") { it.lowercase() }.replaceFirstChar { it.uppercase() } +
            " programı, “${from.lowercase()}” gününün programıyla değiştirilecek."
    override val replace = "Değiştir"
    override val dayBarEmpty = "Gün çubuğu: engelleme yok"
    override fun dayBar(intervals: String) = "Gün çubuğu: $intervals"
    override fun breakRow(gap: String) = "$gap · mola, her şey açık"
    override val enterStartTime = "Başlangıç saatini gir, ör. 930 veya 0930"
    override val enterEndTime = "Bitiş saatini gir, ör. 1400"
    override val start = "Başlangıç"
    override val end = "Bitiş"
    override val timeInputHint = "Sadece rakam: 930 → 9:30, 14 → 14:00. Bitiş 2400 veya 0000 — gece yarısına kadar."
    override fun blockingInterval(interval: String) = "Engelleme $interval"
    override val timeOutOfDay = "Saat gün dışında"
    override val endAfterStart = "Bitiş, başlangıçtan sonra olmalı"

    override val timerTitle = "Zamanlayıcı"
    override val timerHint = "Seçilen uygulamalar için tek seferlik engelleme. Programın üstüne eklenir, onu değiştirmez."
    // «18:30'a kadar» — ek saate göre değişir, bu yüzden nötr «bitiş: 18:30»
    override fun until(time: String) = "bitiş: $time"
    override val extend = "Uzat"
    override fun stopIn(seconds: Int) = "Durdur · $seconds sn sonra"
    override val howLong = "NE KADAR"
    override val custom = "Özel"
    override val customTitle = "Özel süre"
    override val startTimer = "Başlat"
    override val extendBy = "Uzatma süresi"
    override fun extendedBy(duration: String) = "$duration uzatıldı"
    override val stopTimerQ = "Zamanlayıcı durdurulsun mu?"
    override fun dialRunning(countdown: String, until: String) = "Zamanlayıcı: $countdown kaldı, $until"
    override val dialIdle = "Zamanlayıcı çalışmıyor"
    override val notRunning = "BAŞLATILMADI"
    override val pickBelow = "süreyi aşağıdan seç"
    override val notificationsOff = "Geri sayım bildirimi kapalı — zamanlayıcı onsuz da çalışır."
    override val enableNotifications = "Bildirimleri aç"

    override val hours = "Saat"
    override val minutes = "Dakika"
    override val durationHint = "1 dakikadan 12 saate kadar. Boş alan = 0."
    override val durationDigitsOnly = "Saati ve dakikayı rakamla gir"
    override val durationRange = "1 dakikadan 12 saate kadar"

    override fun thinkSeconds(seconds: Int) = "$seconds saniye düşün — gerçekten gerekli mi?"
    override fun quoted(text: String) = "“$text”"
    override val keepAsIs = "Değiştirme"
    override fun confirmIn(seconds: Int) = "$seconds sn sonra onayla"
    override val confirm = "Onayla"
    override val canConfirm = "Artık onaylayabilirsin"
    override fun secondsLeft(seconds: Int) = "$seconds saniye kaldı"

    override val notificationTitle = "Engelleme"
    override val channelDescription = "Engellemenin bitmesine kalan süre — program ve zamanlayıcı"
}

object PlanKk : PlanStrings {
    override val noAppsSelected = "Бұғаттайтын қолданбалар таңдалмаған — оларды «Қолданбалар» қойындысында белгіле"
    override val scheduleTitle = "Кесте"
    override val scheduleHint = "Таңдалған қолданбаларды қашан бұғаттау керек. Аралықтардың арасы — үзіліс, бәрі ашық."
    override val copiedToWeekdays = "Жұмыс күндеріне көшірілді"
    override val moreCopyToWeekdays = "Тағы: жұмыс күндеріне көшіру"
    override val copyToWeekdaysMenu = "Жұмыс күндеріне көшіру (Дс–Жм)"
    override val changeTodayScheduleQ = "Бүгінгі кестені өзгерту керек пе?"
    override val noBlockingThisDay = "Бұл күні бұғаттау жоқ"
    override fun deleteIntervalQ(interval: String) = "$interval аралығын жою керек пе?"
    override fun deleteIntervalLabel(interval: String) = "$interval аралығын жою"
    override fun dayFullUntilMidnight(day: String) = "$day түн ортасына дейін бос емес — бар аралықты өзгерт"
    override val interval = "Аралық"
    override val newInterval = "Жаңа аралық"
    override fun addIntervalQ(interval: String) = "$interval аралығын қосу керек пе?"
    override fun changeIntervalQ(interval: String) = "Аралықты $interval етіп өзгерту керек пе?"
    override val copyToWeekdaysQ = "Жұмыс күндеріне көшіру керек пе?"
    override fun copyWillReplace(days: List<String>, from: String) =
        days.joinToString(", ") { it.lowercase() }.replaceFirstChar { it.uppercase() } +
            " кестесі «${from.lowercase()}» күнінің кестесімен ауыстырылады."
    override val replace = "Ауыстыру"
    override val dayBarEmpty = "Тәулік жолағы: бұғаттау жоқ"
    override fun dayBar(intervals: String) = "Тәулік жолағы: $intervals"
    override fun breakRow(gap: String) = "$gap · үзіліс, бәрі ашық"
    override val enterStartTime = "Басталу уақытын енгіз, мысалы 930 не 0930"
    override val enterEndTime = "Аяқталу уақытын енгіз, мысалы 1400"
    override val start = "Басы"
    override val end = "Соңы"
    override val timeInputHint = "Тек сандар: 930 → 9:30, 14 → 14:00. Соңы 2400 не 0000 — түн ортасына дейін."
    override fun blockingInterval(interval: String) = "Бұғаттау $interval"
    override val timeOutOfDay = "Уақыт тәуліктен тыс"
    override val endAfterStart = "Соңы басынан кейін болуы керек"

    override val timerTitle = "Таймер"
    override val timerHint = "Таңдалған қолданбаларды бір реттік бұғаттау. Кестенің үстінен әрекет етеді, оны өзгертпейді."
    // «18:30-ға дейін» — жалғау уақытқа қарай өзгереді, сондықтан бейтарап «аяқталуы: 18:30»
    override fun until(time: String) = "аяқталуы: $time"
    override val extend = "Ұзарту"
    override fun stopIn(seconds: Int) = "Тоқтату · $seconds с кейін"
    override val howLong = "ҚАНША УАҚЫТ"
    override val custom = "Басқа"
    override val customTitle = "Өз уақытың"
    override val startTimer = "Бастау"
    override val extendBy = "Қаншаға ұзарту"
    override fun extendedBy(duration: String) = "$duration ұзартылды"
    override val stopTimerQ = "Таймерді тоқтату керек пе?"
    override fun dialRunning(countdown: String, until: String) = "Таймер: $countdown қалды, $until"
    override val dialIdle = "Таймер іске қосылмаған"
    override val notRunning = "ҚОСЫЛМАҒАН"
    override val pickBelow = "төменнен уақытты таңда"
    override val notificationsOff = "Кері санақ хабарландыруы өшірулі — таймер онсыз да жұмыс істейді."
    override val enableNotifications = "Хабарландыруларды қосу"

    override val hours = "Сағат"
    override val minutes = "Минут"
    override val durationHint = "1 минуттан 12 сағатқа дейін. Бос өріс — 0."
    override val durationDigitsOnly = "Сағат пен минутты сандармен енгіз"
    override val durationRange = "1 минуттан 12 сағатқа дейін"

    override fun thinkSeconds(seconds: Int) = "$seconds секунд ойлан — шынымен керек пе?"
    override fun quoted(text: String) = "«$text»"
    override val keepAsIs = "Өзгертпеу"
    override fun confirmIn(seconds: Int) = "$seconds с кейін растау"
    override val confirm = "Растау"
    override val canConfirm = "Енді растауға болады"
    override fun secondsLeft(seconds: Int) = "$seconds секунд қалды"

    override val notificationTitle = "Бұғаттау"
    override val channelDescription = "Бұғаттау аяқталғанша қалған уақыт — кесте мен таймер бойынша"
}

object PlanUz : PlanStrings {
    override val noAppsSelected = "Bloklash uchun ilovalar tanlanmagan — ularni «Ilovalar» bo‘limida belgila"
    override val scheduleTitle = "Jadval"
    override val scheduleHint = "Tanlangan ilovalarni qachon bloklash kerak. Oraliqlar orasida tanaffus — hammasi ochiq."
    override val copiedToWeekdays = "Ish kunlariga nusxalandi"
    override val moreCopyToWeekdays = "Yana: ish kunlariga nusxalash"
    override val copyToWeekdaysMenu = "Ish kunlariga nusxalash (Du–Ju)"
    override val changeTodayScheduleQ = "Bugungi jadval o‘zgartirilsinmi?"
    override val noBlockingThisDay = "Bu kunda bloklash yo‘q"
    override fun deleteIntervalQ(interval: String) = "$interval oralig‘i o‘chirilsinmi?"
    override fun deleteIntervalLabel(interval: String) = "$interval oralig‘ini o‘chirish"
    override fun dayFullUntilMidnight(day: String) = "$day yarim tungacha band — mavjud oraliqni o‘zgartir"
    override val interval = "Oraliq"
    override val newInterval = "Yangi oraliq"
    override fun addIntervalQ(interval: String) = "$interval oralig‘i qo‘shilsinmi?"
    override fun changeIntervalQ(interval: String) = "Oraliq $interval ga o‘zgartirilsinmi?"
    override val copyToWeekdaysQ = "Ish kunlariga nusxalansinmi?"
    override fun copyWillReplace(days: List<String>, from: String) =
        days.joinToString(", ") { it.lowercase() }.replaceFirstChar { it.uppercase() } +
            " jadvali «${from.lowercase()}» kuni jadvali bilan almashtiriladi."
    override val replace = "Almashtirish"
    override val dayBarEmpty = "Sutka chizig‘i: bloklash yo‘q"
    override fun dayBar(intervals: String) = "Sutka chizig‘i: $intervals"
    override fun breakRow(gap: String) = "$gap · tanaffus, hammasi ochiq"
    override val enterStartTime = "Boshlanish vaqtini kirit, masalan 930 yoki 0930"
    override val enterEndTime = "Tugash vaqtini kirit, masalan 1400"
    override val start = "Boshi"
    override val end = "Oxiri"
    override val timeInputHint = "Faqat raqamlar: 930 → 9:30, 14 → 14:00. Oxiri 2400 yoki 0000 — yarim tungacha."
    override fun blockingInterval(interval: String) = "Bloklash $interval"
    override val timeOutOfDay = "Vaqt sutkadan tashqarida"
    override val endAfterStart = "Oxiri boshidan keyin bo‘lishi kerak"

    override val timerTitle = "Taymer"
    override val timerHint = "Tanlangan ilovalarni bir martalik bloklash. Jadval ustidan ishlaydi va uni o‘zgartirmaydi."
    override fun until(time: String) = "$time gacha"
    override val extend = "Uzaytirish"
    override fun stopIn(seconds: Int) = "To‘xtatish · $seconds s dan keyin"
    override val howLong = "QANCHA VAQT"
    override val custom = "Boshqa"
    override val customTitle = "O‘z vaqting"
    override val startTimer = "Boshlash"
    override val extendBy = "Qanchaga uzaytirish"
    override fun extendedBy(duration: String) = "$duration ga uzaytirildi"
    override val stopTimerQ = "Taymer to‘xtatilsinmi?"
    override fun dialRunning(countdown: String, until: String) = "Taymer: $countdown qoldi, $until"
    override val dialIdle = "Taymer ishga tushmagan"
    override val notRunning = "ISHGA TUSHMAGAN"
    override val pickBelow = "muddatni pastdan tanla"
    override val notificationsOff = "Teskari sanoq bildirishnomasi o‘chiq — taymer usiz ham ishlaydi."
    override val enableNotifications = "Bildirishnomalarni yoqish"

    override val hours = "Soat"
    override val minutes = "Daqiqa"
    override val durationHint = "1 daqiqadan 12 soatgacha. Bo‘sh maydon — 0."
    override val durationDigitsOnly = "Soat va daqiqani raqamlarda kirit"
    override val durationRange = "1 daqiqadan 12 soatgacha"

    override fun thinkSeconds(seconds: Int) = "$seconds soniya o‘ylab ko‘r — rostdan kerakmi?"
    override fun quoted(text: String) = "«$text»"
    override val keepAsIs = "O‘zgartirmaslik"
    override fun confirmIn(seconds: Int) = "$seconds s dan keyin tasdiqlash"
    override val confirm = "Tasdiqlash"
    override val canConfirm = "Endi tasdiqlash mumkin"
    override fun secondsLeft(seconds: Int) = "$seconds soniya qoldi"

    override val notificationTitle = "Bloklash"
    override val channelDescription = "Bloklash tugashiga qolgan vaqt — jadval va taymer bo‘yicha"
}

object PlanZh : PlanStrings {
    override val noAppsSelected = "还没有选择要屏蔽的应用——在“应用”标签页中勾选"
    override val scheduleTitle = "时间表"
    override val scheduleHint = "何时屏蔽所选应用。时段之间是休息，一切开放。"
    override val copiedToWeekdays = "已复制到工作日"
    override val moreCopyToWeekdays = "更多：复制到工作日"
    override val copyToWeekdaysMenu = "复制到工作日（周一至周五）"
    override val changeTodayScheduleQ = "要修改今天的时间表吗？"
    override val noBlockingThisDay = "这一天没有屏蔽"
    override fun deleteIntervalQ(interval: String) = "删除时段 $interval？"
    override fun deleteIntervalLabel(interval: String) = "删除时段 $interval"
    override fun dayFullUntilMidnight(day: String) = "${day}已排满到午夜——请修改已有时段"
    override val interval = "时段"
    override val newInterval = "新时段"
    override fun addIntervalQ(interval: String) = "添加时段 $interval？"
    override fun changeIntervalQ(interval: String) = "将时段改为 $interval？"
    override val copyToWeekdaysQ = "复制到工作日？"
    override fun copyWillReplace(days: List<String>, from: String) =
        days.joinToString("、") + "的时间表将被替换为“$from”的时间表。"
    override val replace = "替换"
    override val dayBarEmpty = "全天时间条：没有屏蔽"
    override fun dayBar(intervals: String) = "全天时间条：$intervals"
    override fun breakRow(gap: String) = "$gap · 休息，一切开放"
    override val enterStartTime = "输入开始时间，例如 930 或 0930"
    override val enterEndTime = "输入结束时间，例如 1400"
    override val start = "开始"
    override val end = "结束"
    override val timeInputHint = "只输入数字：930 → 9:30，14 → 14:00。结束填 2400 或 0000 表示到午夜。"
    override fun blockingInterval(interval: String) = "屏蔽 $interval"
    override val timeOutOfDay = "时间超出一天范围"
    override val endAfterStart = "结束时间必须晚于开始时间"

    override val timerTitle = "计时器"
    override val timerHint = "一次性屏蔽所选应用。叠加在时间表之上，不会改动它。"
    override fun until(time: String) = "至 $time"
    override val extend = "延长"
    override fun stopIn(seconds: Int) = "停止 · ${seconds} 秒后"
    override val howLong = "时长"
    override val custom = "自定义"
    override val customTitle = "自定义时长"
    override val startTimer = "开始"
    override val extendBy = "延长多久"
    override fun extendedBy(duration: String) = "已延长 $duration"
    override val stopTimerQ = "停止计时器？"
    override fun dialRunning(countdown: String, until: String) = "计时器：剩余 $countdown，$until"
    override val dialIdle = "计时器未启动"
    override val notRunning = "未启动"
    override val pickBelow = "在下方选择时长"
    override val notificationsOff = "倒计时通知已关闭——没有它计时器也照常运行。"
    override val enableNotifications = "开启通知"

    override val hours = "小时"
    override val minutes = "分钟"
    override val durationHint = "1 分钟到 12 小时。留空即为 0。"
    override val durationDigitsOnly = "用数字输入小时和分钟"
    override val durationRange = "1 分钟到 12 小时"

    override fun thinkSeconds(seconds: Int) = "先想 $seconds 秒——真的需要吗？"
    override fun quoted(text: String) = "“$text”"
    override val keepAsIs = "不修改"
    override fun confirmIn(seconds: Int) = "${seconds} 秒后可确认"
    override val confirm = "确认"
    override val canConfirm = "现在可以确认"
    override fun secondsLeft(seconds: Int) = "还剩 $seconds 秒"

    override val notificationTitle = "屏蔽"
    override val channelDescription = "屏蔽结束前的剩余时间——按时间表和计时器"
}
