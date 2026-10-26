package ru.shashkov.antioblomov.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ru.shashkov.antioblomov.blocking.ServiceState
import ru.shashkov.antioblomov.rules.SetupChecklist
import ru.shashkov.antioblomov.rules.SetupState
import ru.shashkov.antioblomov.ui.i18n.LocalStrings
import ru.shashkov.antioblomov.ui.theme.Calm
import ru.shashkov.antioblomov.ui.theme.TextMuted
import ru.shashkov.antioblomov.ui.theme.Warn

/**
 * Блок «Система» на главной (редизайн R2): служба блокировки и «Настройка телефона»
 * свёрнуты в одну строку «Система · всё в порядке». Если что-то не так, строка янтарная,
 * а блок раскрыт сам (главная тогда ставит его сразу под кольцом).
 */
@Composable
fun SystemBlock(
    serviceState: ServiceState,
    setup: SetupState,
    onOpenWizard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = LocalStrings.current
    val problem = serviceState != ServiceState.RUNNING || SetupChecklist.missing(setup).isNotEmpty()
    // Проблема появилась — раскрываем; пропала — сворачиваем
    var open by rememberSaveable(problem) { mutableStateOf(problem) }

    AppCard(modifier = modifier, padding = androidx.compose.foundation.layout.PaddingValues(0.dp), spacing = 0.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { open = !open }
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                Modifier
                    .padding(horizontal = 6.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (problem) Warn else Calm)
            )
            Text(
                text = "${s.home.system} · " + if (serviceState != ServiceState.RUNNING) s.home.serviceNotWorking else SetupChecklist.summary(setup, s.setup),
                style = MaterialTheme.typography.bodyMedium,
                color = if (problem) Warn else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Icon(
                if (open) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = if (open) s.home.collapse else s.home.details,
                tint = TextMuted,
            )
        }
        AnimatedVisibility(visible = open) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                HorizontalDivider()
                ServiceStatusCard(state = serviceState)
                HorizontalDivider()
                SetupCard(setup = setup, onOpenWizard = onOpenWizard)
            }
        }
    }
}
