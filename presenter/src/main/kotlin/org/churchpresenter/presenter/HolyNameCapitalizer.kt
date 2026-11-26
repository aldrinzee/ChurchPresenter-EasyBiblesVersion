package org.churchpresenter.presenter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.churchpresenter.core.models.songs.LyricSection
import org.churchpresenter.settings.SongSettings

/**
 * Raises the holy names a song wrote in lowercase -- "god" to "God", "holy spirit" to "Holy Spirit"
 * -- as the slide is drawn ([org.churchpresenter.settings.SongLayoutExtras.capitalizeHolyNames]).
 *
 * Each name is matched as whole words, in any case, with any run of spaces between its words, so
 * "godly" and "goddess" are left alone while "god's" is raised. A match only ever has letters
 * *raised* to the name's own capitals, never lowered: a line set in capitals keeps "LORD", and the
 * small words of a phrase ("Lamb of God") stay as the name writes them.
 */
internal class HolyNameCapitalizer(names: List<String>) {
    private val patterns: List<Pair<List<String>, Regex>> = names
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .distinctBy { it.lowercase() }
        .map { name ->
            val words = name.split(WHITESPACE)
            val body = words.joinToString("""\s+""") { Regex.escape(it) }
            words to Regex("""(?<![\p{L}\p{N}])$body(?![\p{L}\p{N}])""", RegexOption.IGNORE_CASE)
        }

    /** [text] with every name in it raised to the name's own capitals. */
    fun apply(text: String): String = patterns.fold(text) { current, (words, regex) ->
        regex.replace(current) { match -> raised(match.value, words) }
    }

    private fun raised(found: String, words: List<String>): String {
        val out = StringBuilder(found.length)
        var word = 0
        var inWord = 0
        for (ch in found) {
            if (ch.isWhitespace()) {
                if (inWord > 0) word++
                inWord = 0
                out.append(ch)
                continue
            }
            val model = words.getOrNull(word)?.getOrNull(inWord)
            out.append(if (model != null && model.isUpperCase()) ch.uppercaseChar() else ch)
            inWord++
        }
        return out.toString()
    }

    private companion object {
        val WHITESPACE = Regex("""\s+""")
    }
}

/** This section with [capitalizer] applied to every line the audience or the band reads. */
internal fun LyricSection.withHolyNames(capitalizer: HolyNameCapitalizer): LyricSection = copy(
    lines = lines.map(capitalizer::apply),
    translations = translations.map { it.copy(lines = it.lines.map(capitalizer::apply)) },
    chordLines = chordLines.map(capitalizer::apply),
)

/** The capitalizer [settings] asks for, or null where holy names are left as written. */
@Composable
internal fun rememberHolyNameCapitalizer(settings: SongSettings): HolyNameCapitalizer? {
    val extras = settings.layoutExtras
    return remember(extras.capitalizeHolyNames, extras.holyNames) {
        if (extras.capitalizeHolyNames) HolyNameCapitalizer(extras.holyNames) else null
    }
}
