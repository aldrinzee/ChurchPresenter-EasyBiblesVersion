package org.churchpresenter.profiles

import androidx.compose.runtime.Composable
import org.churchpresenter.settings.utils.Constants
import org.churchpresenter.strings.generated.resources.Res
import org.churchpresenter.strings.generated.resources.customize_translation_label_after_abbreviation
import org.churchpresenter.strings.generated.resources.customize_translation_label_after_name
import org.churchpresenter.strings.generated.resources.customize_translation_label_before
import org.churchpresenter.strings.generated.resources.customize_translation_label_style
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** The three ways a reference can name its translation, each shown as an example of itself. */
private val TRANSLATION_LABEL_STYLES: List<Pair<String, StringResource>> = listOf(
    Constants.TRANSLATION_LABEL_BEFORE to Res.string.customize_translation_label_before,
    Constants.TRANSLATION_LABEL_AFTER_ABBREVIATION to Res.string.customize_translation_label_after_abbreviation,
    Constants.TRANSLATION_LABEL_AFTER_NAME to Res.string.customize_translation_label_after_name,
)

/**
 * Under the Reference tab's Abbreviation switch while it is on: whether the translation goes before
 * the reference ("KJV John 3:16") or after it in brackets, abbreviated ("John 3:16 (KJV)") or by full
 * name ("John 3:16 (King James Version)"). Written like the switch, through [BibleEdit.updateEntry],
 * so under All it reaches every translation that has not chosen for itself.
 */
@Composable
internal fun TranslationLabelStyleRow(edit: BibleEdit) {
    val current = edit.shown.translationLabelStyle
    SettingsRow(Res.string.customize_translation_label_style) {
        SegmentedRow {
            TRANSLATION_LABEL_STYLES.forEach { (style, label) ->
                Segment(stringResource(label), selected = current == style) {
                    edit.updateEntry { it.copy(translationLabelStyle = style) }
                }
            }
        }
    }
}
