package ru.shashkov.antioblomov.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ru.shashkov.antioblomov.data.Language
import ru.shashkov.antioblomov.ui.i18n.LocalStrings
import ru.shashkov.antioblomov.ui.theme.Surface1
import ru.shashkov.antioblomov.ui.theme.TextMuted

/**
 * Открыть страницу доната — но если включён VPN, сначала предупредить (v1.2.1, решение от 02.11.2026).
 *
 * DonationAlerts выбирает способы оплаты по IP-адресу: с VPN пользователю из России страница может
 * не показать оплату в рублях (карта российского банка, СБП). Выключить чужой VPN приложение не
 * может (Android этого не позволяет), поэтому объясняем и даём кнопку в системные настройки VPN.
 *
 * Возвращает действие для кнопки «Поддержать ›»; окно предупреждения рисует сама.
 */
@Composable
fun rememberDonateOpener(url: String = DONATE_URL): () -> Unit {
    val context = LocalContext.current
    var warning by remember { mutableStateOf(false) }

    if (warning) {
        VpnWarningDialog(
            onSettings = { warning = false; openVpnSettings(context) },
            onOpenAnyway = { warning = false; openUrl(context, url) },
            onCancel = { warning = false },
        )
    }
    return remember(url) {
        { if (isVpnActive(context)) warning = true else openUrl(context, url) }
    }
}

/**
 * Идёт ли трафик приложения через VPN. Нужен только ACCESS_NETWORK_STATE (не интернет).
 *
 * Android считает VPN и некоторые блокировщики рекламы / файрволы (они тоже работают как VPN),
 * поэтому в тексте окна — «похоже», и есть «Всё равно открыть».
 */
fun isVpnActive(context: Context): Boolean = try {
    val cm = context.getSystemService(ConnectivityManager::class.java)
    val network = cm?.activeNetwork
    network != null && cm.getNetworkCapabilities(network)?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
} catch (e: SecurityException) {
    false
}

private fun openUrl(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, Language.strings(context).home.noBrowser, Toast.LENGTH_LONG).show()
    }
}

/** Системный экран VPN; если прошивка его не отдаёт — «Сеть и интернет», потом общие настройки. */
private fun openVpnSettings(context: Context) {
    for (action in listOf(Settings.ACTION_VPN_SETTINGS, Settings.ACTION_WIRELESS_SETTINGS, Settings.ACTION_SETTINGS)) {
        try {
            context.startActivity(Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            return
        } catch (e: ActivityNotFoundException) {
            // пробуем следующий экран
        }
    }
}

@Composable
private fun VpnWarningDialog(onSettings: () -> Unit, onOpenAnyway: () -> Unit, onCancel: () -> Unit) {
    val s = LocalStrings.current
    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = Surface1,
        title = { Text(s.home.vpnTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(s.home.vpnBody, style = MaterialTheme.typography.bodyMedium)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(onClick = onSettings, modifier = Modifier.fillMaxWidth()) { Text(s.home.vpnSettings) }
                    OutlinedButton(onClick = onOpenAnyway, modifier = Modifier.fillMaxWidth()) { Text(s.home.vpnOpenAnyway) }
                    TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                        Text(s.common.cancel, color = TextMuted)
                    }
                }
            }
        },
        confirmButton = {},
    )
}
