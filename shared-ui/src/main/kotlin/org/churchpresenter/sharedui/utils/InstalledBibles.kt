package org.churchpresenter.sharedui.utils

import org.churchpresenter.bible.MAX_BIBLE_SCAN_DEPTH
import org.churchpresenter.settings.utils.Constants
import java.io.File

/**
 * Every `.spb` module under [directory], as paths relative to it, sorted.
 *
 * Bible collections ship as nested folders — one directory per language, often one per translation
 * under that — so the walk goes down [MAX_BIBLE_SCAN_DEPTH] levels, bounded so a symlink cycle or a
 * stray deep tree cannot hang the caller. Relative rather than bare names because a collection can
 * carry two files of the same name in different folders, and the returned string is the identity
 * everything else keys on (the translation stack, the display-name map, the engine's file list).
 * Callers resolve with `File(directory, value)`, which handles the embedded separators everywhere.
 *
 * Shared by the Bible settings page and the Bible tab's Bibles list. **Blocking — call it from
 * `Dispatchers.IO`.**
 */
fun installedBibleFiles(directory: String): List<String> {
    if (directory.isEmpty()) return emptyList()
    val dir = File(directory)
    if (!dir.exists() || !dir.isDirectory) return emptyList()
    return dir.walkTopDown().maxDepth(MAX_BIBLE_SCAN_DEPTH)
        .filter { it.isFile && it.extension.lowercase() == Constants.EXTENSION_SPB && !isSystemArtifact(it.name) }
        .map { it.toRelativeString(dir).replace('\\', '/') }
        .sorted()
        .toList()
}
