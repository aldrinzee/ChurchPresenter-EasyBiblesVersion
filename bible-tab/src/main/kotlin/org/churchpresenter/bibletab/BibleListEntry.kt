package org.churchpresenter.bibletab

import org.churchpresenter.bible.displayNamesFor
import org.churchpresenter.settings.BibleTranslationSettings

/** One installed module as the Bibles list shows it. */
internal data class BibleListEntry(
    val fileName: String,
    val name: String,
    /** Its place in the translation stack (0 = primary), or null when it is not shown. */
    val place: Int?,
)

/**
 * The Bibles list's rows: every module in the folder, named as the pickers name them, with the
 * place each one of the stack holds. A module the stack names but the folder lacks is left out --
 * the tab's load-error banner already reports it.
 */
internal fun bibleListEntries(
    files: List<String>,
    titles: Map<String, String>,
    stack: List<BibleTranslationSettings>,
): List<BibleListEntry> {
    val renames = stack.associate { it.fileName to it.customName.trim() }.filterValues { it.isNotBlank() }
    val names = displayNamesFor(titles.filterKeys { it in files }, renames)
    val places = stack.withIndex().associate { (index, translation) -> translation.fileName to index }
    return files
        .map { BibleListEntry(it, names[it] ?: it.substringBeforeLast('.'), places[it]) }
        .sortedBy { it.name.lowercase() }
}
