package ru.shashkov.antioblomov.blocking

import ru.shashkov.antioblomov.rules.i18n.Lang
import ru.shashkov.antioblomov.blocking.Scene.DAWN
import ru.shashkov.antioblomov.blocking.Scene.MOUNTAINS
import ru.shashkov.antioblomov.blocking.Scene.NIGHT
import ru.shashkov.antioblomov.blocking.Scene.ROAD
import ru.shashkov.antioblomov.blocking.Scene.STORM
import ru.shashkov.antioblomov.blocking.Scene.STUDY
import ru.shashkov.antioblomov.blocking.Scene.WATER
import ru.shashkov.antioblomov.blocking.Scene.WINTER

/**
 * Цитата для экрана блокировки и сюжет фона под неё.
 */
data class Quote(val text: String, val source: String, val scene: Scene)

/**
 * Цитаты, мотивирующие к действию: к делу, учёбе, усилию.
 *
 * Русская классика — ревизия 24.10.2026 (отвлечённые убраны).
 * Иностранные авторы — переводы наши, сделаны с оригиналов, чтобы не зависеть
 * от чужих переводов: формулировки могут отличаться от привычных.
 *
 * ВАЖНО: перед публикацией сверить каждую цитату с источником.
 * Отмеченные «проверить» — особенно. «Выдавливать из себя по капле раба» —
 * пересказ мысли Чехова из письма, а не дословная цитата.
 */
object Quotes {

    val all: List<Quote> = listOf(
        // ---------- Русская классика ----------

        // К действию — сейчас
        Quote("Теперь или никогда!", "И. А. Гончаров, «Обломов»", DAWN),
        Quote(
            "Не успокаивайтесь, не давайте усыплять себя! Пока молоды, сильны, бодры, не уставайте делать добро!",
            "А. П. Чехов, «Крыжовник»", NIGHT, // проверить
        ),
        Quote("Делу время и потехе час.", "Царь Алексей Михайлович, «Урядник сокольничья пути»", DAWN),
        Quote(
            "Тут нужны беспрерывный дневной и ночной труд, вечное чтение… Тут дорог каждый час.",
            "А. П. Чехов, письмо брату Николаю", STUDY, // проверить
        ),
        Quote(
            "Забирайте же с собою в путь… все человеческие движения, не оставляйте их на дороге: не подымете потом!",
            "Н. В. Гоголь, «Мёртвые души»", ROAD, // проверить
        ),
        Quote(
            "Жизнь даётся человеку один раз, и прожить её надо так, чтобы не было мучительно больно за бесцельно прожитые годы.",
            "Н. А. Островский, «Как закалялась сталь»", NIGHT,
        ),
        Quote("Пусть сильнее грянет буря!", "М. Горький, «Песня о Буревестнике»", STORM),
        Quote("И вечный бой! Покой нам только снится.", "А. А. Блок, «На поле Куликовом»", STORM),
        Quote("Безумству храбрых поём мы песню!", "М. Горький, «Песня о Соколе»", STORM),
        Quote("Рождённый ползать — летать не может!", "М. Горький, «Песня о Соколе»", MOUNTAINS),
        Quote("Светить всегда,\nсветить везде…\nВот лозунг мой —\nи солнца!", "В. В. Маяковский", DAWN),
        Quote("Бди!", "Козьма Прутков", NIGHT),
        Quote("Надо жить, надо любить, надо верить.", "Л. Н. Толстой, «Война и мир»", DAWN), // проверить

        // Труд и учёба
        Quote("Труд — образ, содержание, стихия и цель жизни.", "И. А. Гончаров, «Обломов»", STUDY),
        Quote(
            "Человек должен трудиться, работать в поте лица, кто бы он ни был, и в этом одном заключается смысл и цель его жизни.",
            "А. П. Чехов, «Три сестры»", STUDY, // проверить
        ),
        Quote("Всякий человек сам себя воспитать должен.", "И. С. Тургенев, «Отцы и дети»", STUDY),
        Quote("Усердие всё превозмогает!", "Козьма Прутков", WATER),
        Quote(
            "Усильным, напряжённым постоянством\nЯ наконец в искусстве безграничном\nДостигнул степени высокой.",
            "А. С. Пушкин, «Моцарт и Сальери»", MOUNTAINS,
        ),
        Quote("Науки юношей питают,\nОтраду старым подают.", "М. В. Ломоносов", STUDY),
        Quote("Выдавливать из себя по капле раба.", "По мотивам письма А. П. Чехова", STUDY),

        // Цель и характер
        Quote("Я знал одной лишь думы власть,\nОдну — но пламенную страсть.", "М. Ю. Лермонтов, «Мцыри»", MOUNTAINS),
        Quote("Зри в корень!", "Козьма Прутков", STUDY),
        Quote("Если хочешь быть счастливым, будь им.", "Козьма Прутков", DAWN),
        Quote("Поэтом можешь ты не быть,\nНо гражданином быть обязан.", "Н. А. Некрасов, «Поэт и гражданин»", ROAD),
        Quote("Человек — это звучит гордо!", "М. Горький, «На дне»", MOUNTAINS),

        // С иронией — подталкивают к делу от противного
        Quote(
            "Попрыгунья Стрекоза\nЛето красное пропела;\nОглянуться не успела,\nКак зима катит в глаза.",
            "И. А. Крылов, «Стрекоза и Муравей»", WINTER,
        ),
        Quote("Ты всё пела? это дело:\nТак пойди же, попляши!", "И. А. Крылов, «Стрекоза и Муравей»", WINTER),
        Quote("Хотел писать — но труд упорный\nЕму был тошен.", "А. С. Пушкин, «Евгений Онегин»", WINTER),
        Quote("Не хочу учиться, хочу жениться.", "Д. И. Фонвизин, «Недоросль»", STUDY),
        Quote(
            "Чем кумушек считать трудиться,\nНе лучше ль на себя, кума, оборотиться?",
            "И. А. Крылов, «Зеркало и Обезьяна»", WATER,
        ),
        Quote("Мы ленивы и нелюбопытны.", "А. С. Пушкин, «Путешествие в Арзрум»", NIGHT),

        // ---------- Иностранные писатели и философы (переводы наши) ----------

        Quote("Пока мы откладываем, жизнь проходит.", "Сенека, «Нравственные письма к Луцилию»", NIGHT),
        Quote("Всё, Луцилий, чужое, одно лишь время наше.", "Сенека, «Нравственные письма к Луцилию»", NIGHT),
        Quote("Жизнь не коротка — это мы много её теряем.", "Сенека, «О скоротечности жизни»", NIGHT),
        Quote(
            "Не потому мы не решаемся, что дело трудно, — оно трудно потому, что мы не решаемся.",
            "Сенека, «Нравственные письма к Луцилию»", MOUNTAINS,
        ),
        Quote("Кто начал — сделал половину дела.", "Гораций, «Послания»", MOUNTAINS),
        Quote("Лови день.", "Гораций, «Оды»", DAWN),
        Quote("Путь в тысячу ли начинается с первого шага.", "Лао-цзы, «Дао дэ цзин»", ROAD),
        Quote(
            "Сначала скажи себе, каким ты хочешь быть, а потом делай то, что нужно делать.",
            "Эпиктет, «Беседы»", ROAD,
        ),
        Quote(
            "Делай каждое дело так, будто оно последнее в твоей жизни.",
            "Марк Аврелий, «Наедине с собой»", DAWN,
        ),
        Quote("Ни дня без строчки.", "Плиний Старший о художнике Апеллесе", STUDY),
        Quote("Капля камень точит.", "Овидий, «Письма с Понта»", WATER),
        Quote("Жизнь коротка, искусство долговечно.", "Гиппократ, «Афоризмы»", NIGHT),
        Quote("Потерянное время никогда не найдёшь снова.", "Бенджамин Франклин", NIGHT),
        Quote("Есть дело на завтра — сделай его сегодня.", "Бенджамин Франклин", DAWN), // проверить
        Quote("Промедление — вор времени.", "Эдвард Юнг, «Ночные думы»", NIGHT),
        Quote("Я тратил время, теперь время тратит меня.", "У. Шекспир, «Ричард II»", WINTER),
    )

    /**
     * Цитаты на других языках (v1.2, решение от 26.10.2026: меньше, но достоверно).
     * Берём только:
     * - известные переводы классики (Сенека, Гораций, Гиппократ — по устоявшимся формулировкам;
     *   английские авторы — в оригинале; Островский — в хрестоматийных переводах en и zh);
     * - оригиналы на самом языке: Лао-цзы и классика для zh, Гёте для de, Мачадо и Сервантес для es,
     *   Пессоа для pt, Юнус Эмре для tr, Абай для kk, а также известные пословицы.
     * kk и uz — перед публикацией показать носителю языка.
     */
    private val byLang: Map<Lang, List<Quote>> = mapOf(
        Lang.RU to all,
        Lang.EN to listOf(
            Quote("Lost time is never found again.", "Benjamin Franklin, Poor Richard’s Almanack", NIGHT),
            Quote("Procrastination is the thief of time.", "Edward Young, Night Thoughts", NIGHT),
            Quote("I wasted time, and now doth time waste me.", "William Shakespeare, Richard II", WINTER),
            Quote("While we are postponing, life speeds by.", "Seneca, Letters to Lucilius", NIGHT),
            Quote("Nothing, Lucilius, is ours, except time.", "Seneca, Letters to Lucilius", NIGHT),
            Quote(
                "It is not because things are difficult that we do not dare; it is because we do not dare that they are difficult.",
                "Seneca, Letters to Lucilius", MOUNTAINS,
            ),
            Quote(
                "It is not that we have a short time to live, but that we waste a lot of it.",
                "Seneca, On the Shortness of Life", NIGHT,
            ),
            Quote("He who has begun has half done.", "Horace, Epistles", MOUNTAINS),
            Quote("Seize the day.", "Horace, Odes", DAWN),
            Quote("A journey of a thousand miles begins with a single step.", "Lao Tzu, Tao Te Ching", ROAD),
            Quote("No day without a line.", "Pliny the Elder, on the painter Apelles", STUDY),
            Quote("Dripping water hollows out stone.", "Ovid, Epistles from Pontus", WATER),
            Quote("Life is short, the art long.", "Hippocrates, Aphorisms", NIGHT),
            Quote(
                "First say to yourself what you would be; and then do what you have to do.",
                "Epictetus, Discourses", ROAD,
            ),
            Quote(
                "Man’s dearest possession is life. It is given to him but once, and he must live it so as to feel no torturing regrets for wasted years.",
                "Nikolai Ostrovsky, How the Steel Was Tempered", NIGHT,
            ),
        ),
        Lang.ES to listOf(
            Quote("Caminante, no hay camino,\nse hace camino al andar.", "Antonio Machado, «Proverbios y cantares»", ROAD),
            Quote(
                "El que lee mucho y anda mucho, ve mucho y sabe mucho.",
                "Miguel de Cervantes, «Don Quijote de la Mancha»", STUDY,
            ),
            Quote("No dejes para mañana lo que puedas hacer hoy.", "Refrán", DAWN),
            Quote("Poco a poco se va lejos.", "Refrán", MOUNTAINS),
            Quote("La gota horada la piedra.", "Ovidio, «Pónticas»", WATER),
            Quote("Un viaje de mil millas comienza con un solo paso.", "Lao Tse, «Tao Te Ching»", ROAD),
            Quote("Aprovecha el día.", "Horacio, «Odas»", DAWN),
            Quote("Ningún día sin una línea.", "Plinio el Viejo, sobre el pintor Apeles", STUDY),
            Quote("La vida es breve; el arte, largo.", "Hipócrates, «Aforismos»", NIGHT),
        ),
        Lang.PT to listOf(
            Quote("Tudo vale a pena\nSe a alma não é pequena.", "Fernando Pessoa, «Mar Português»", MOUNTAINS),
            Quote("Não deixe para amanhã o que você pode fazer hoje.", "Provérbio", DAWN),
            Quote("Água mole em pedra dura, tanto bate até que fura.", "Provérbio", WATER),
            Quote("Devagar se vai ao longe.", "Provérbio", ROAD),
            Quote("Uma jornada de mil milhas começa com um único passo.", "Lao-Tsé, «Tao Te Ching»", ROAD),
            Quote("Aproveite o dia.", "Horácio, «Odes»", DAWN),
            Quote("Nenhum dia sem uma linha.", "Plínio, o Velho, sobre o pintor Apeles", STUDY),
            Quote("A vida é breve, a arte é longa.", "Hipócrates, «Aforismos»", NIGHT),
        ),
        Lang.DE to listOf(
            Quote(
                "Es ist nicht genug, zu wissen, man muss auch anwenden; es ist nicht genug, zu wollen, man muss auch tun.",
                "J. W. von Goethe, „Wilhelm Meisters Wanderjahre“", STUDY,
            ),
            Quote("Ach Gott! die Kunst ist lang;\nUnd kurz ist unser Leben.", "J. W. von Goethe, „Faust I“", NIGHT),
            Quote("Steter Tropfen höhlt den Stein.", "Sprichwort", WATER),
            Quote("Was du heute kannst besorgen, das verschiebe nicht auf morgen.", "Sprichwort", DAWN),
            Quote("Ohne Fleiß kein Preis.", "Sprichwort", STUDY),
            Quote("Auch eine Reise von tausend Meilen beginnt mit dem ersten Schritt.", "Laozi, „Daodejing“", ROAD),
            Quote("Nutze den Tag.", "Horaz, „Oden“", DAWN),
            Quote("Kein Tag ohne Linie.", "Plinius der Ältere über den Maler Apelles", STUDY),
        ),
        Lang.TR to listOf(
            Quote("İlim ilim bilmektir,\nİlim kendin bilmektir.", "Yunus Emre", STUDY),
            Quote("Bugünün işini yarına bırakma.", "Atasözü", DAWN),
            Quote("Damlaya damlaya göl olur.", "Atasözü", WATER),
            Quote("Ağaç yaşken eğilir.", "Atasözü", STUDY),
            Quote("Emek olmadan yemek olmaz.", "Atasözü", STUDY),
            Quote("Vakit nakittir.", "Atasözü", NIGHT),
            Quote("Bin millik bir yolculuk tek bir adımla başlar.", "Lao Tzu, «Tao Te Ching»", ROAD),
        ),
        Lang.KK to listOf(
            Quote("Ғылым таппай мақтанба,\nОрын таппай баптанба.", "Абай Құнанбайұлы", STUDY),
            Quote("Еңбек етсең — емерсің.", "Мақал", STUDY),
            Quote("Оқу — инемен құдық қазғандай.", "Мақал", STUDY),
            Quote("Бүгінгі істі ертеңге қалдырма.", "Мақал", DAWN),
        ),
        Lang.UZ to listOf(
            Quote("Bugungi ishni ertaga qoldirma.", "Maqol", DAWN),
            Quote("Tomchi-tomchi ko‘l bo‘lur.", "Maqol", WATER),
            Quote("Ilm olish — igna bilan quduq qazish.", "Maqol", STUDY),
            Quote("Mehnatning tagi — rohat.", "Maqol", STUDY),
        ),
        Lang.ZH to listOf(
            Quote("千里之行，始于足下。", "老子《道德经》", ROAD),
            Quote("天行健，君子以自强不息。", "《周易》", MOUNTAINS),
            Quote("少壮不努力，老大徒伤悲。", "汉乐府《长歌行》", NIGHT),
            Quote("业精于勤，荒于嬉；行成于思，毁于随。", "韩愈《进学解》", STUDY),
            Quote("黑发不知勤学早，白首方悔读书迟。", "颜真卿《劝学》", STUDY),
            Quote("锲而不舍，金石可镂。", "荀子《劝学》", WATER),
            Quote("一寸光阴一寸金，寸金难买寸光阴。", "谚语", NIGHT),
            Quote(
                "人最宝贵的东西是生命。生命对于我们只有一次。一个人的生命应当这样度过：当他回首往事的时候，他不会因为虚度年华而悔恨。",
                "奥斯特洛夫斯基《钢铁是怎样炼成的》", NIGHT,
            ),
        ),
    )

    /** Цитаты на языке [lang]. */
    fun forLang(lang: Lang): List<Quote> = byLang[lang] ?: all

    private var lastIndex = -1
    private var lastLang: Lang? = null

    /** Случайная цитата на языке [lang], не повторяющая предыдущую. */
    fun random(lang: Lang = Lang.RU): Quote {
        val list = forLang(lang)
        if (lang != lastLang) { lastIndex = -1; lastLang = lang }
        var i = list.indices.random()
        if (i == lastIndex && list.size > 1) i = (i + 1) % list.size
        lastIndex = i
        return list[i]
    }
}
