package org.churchpresenter.profiles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.churchpresenter.settings.DEFAULT_HOLY_NAMES
import org.churchpresenter.settings.SongSettings
import org.churchpresenter.strings.generated.resources.Res
import org.churchpresenter.strings.generated.resources.profile_capitalize_holy_names
import org.churchpresenter.strings.generated.resources.profile_capitalize_holy_names_sub
import org.churchpresenter.strings.generated.resources.profile_holy_names_hint
import org.churchpresenter.strings.generated.resources.profile_holy_names_placeholder
import org.churchpresenter.strings.generated.resources.profile_holy_names_reset
import org.jetbrains.compose.resources.stringResource

internal val HOLY_NAMES_PATHS = listOf(
    "songSettings.layoutExtras.capitalizeHolyNames",
    "songSettings.layoutExtras.holyNames",
)

/**
 * The Slides group's holy-names switch and, while it is on, the names it raises.
 *
 * The list is typed as one comma-separated line. What is typed is kept as a draft and only its
 * parsed names are stored, so a space or comma just pressed is not trimmed away before the next
 * letter arrives; the draft is reset whenever the stored list changes from elsewhere.
 */
@Composable
internal fun HolyNamesRows(song: SongSettings, updateSong: ((SongSettings) -> SongSettings) -> Unit) {
    val extras = song.layoutExtras
    SettingsSwitchRow(
        Res.string.profile_capitalize_holy_names,
        extras.capitalizeHolyNames,
        { v -> updateSong { it.copy(layoutExtras = it.layoutExtras.copy(capitalizeHolyNames = v)) } },
        sub = stringResource(Res.string.profile_capitalize_holy_names_sub),
        paths = HOLY_NAMES_PATHS.take(1),
    )
    if (!extras.capitalizeHolyNames) return
    var draft by remember { mutableStateOf(extras.holyNames.joinToString(", ")) }
    if (parseHolyNames(draft) != extras.holyNames) draft = extras.holyNames.joinToString(", ")
    fun write(names: List<String>) = updateSong { it.copy(layoutExtras = it.layoutExtras.copy(holyNames = names)) }
    SettingsWideRow {
        SunkenField(
            value = draft,
            onValueChange = { text ->
                draft = text
                write(parseHolyNames(text))
            },
            placeholder = stringResource(Res.string.profile_holy_names_placeholder),
            modifier = Modifier.fillMaxWidth().height(32.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(Res.string.profile_holy_names_hint),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (extras.holyNames != DEFAULT_HOLY_NAMES) {
                LinkText(stringResource(Res.string.profile_holy_names_reset), { write(DEFAULT_HOLY_NAMES) })
            }
        }
    }
}

/** The names in a comma-separated line, trimmed, blanks dropped. */
internal fun parseHolyNames(text: String): List<String> =
    text.split(',').map { it.trim() }.filter { it.isNotEmpty() }
