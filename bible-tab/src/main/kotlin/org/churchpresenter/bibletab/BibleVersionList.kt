package org.churchpresenter.bibletab

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.churchpresenter.bible.bibleTitles
import org.churchpresenter.settings.BibleTranslationSettings
import org.churchpresenter.sharedui.composables.BibleListRowShape
import org.churchpresenter.sharedui.composables.DragHandle
import org.churchpresenter.sharedui.composables.bibleListCard
import org.churchpresenter.sharedui.composables.bibleRowColors
import org.churchpresenter.sharedui.composables.rememberRowHover
import org.churchpresenter.sharedui.composables.rowPad
import org.churchpresenter.sharedui.composables.rowSpan
import org.churchpresenter.sharedui.utils.installedBibleFiles
import org.churchpresenter.strings.generated.resources.Res
import org.churchpresenter.strings.generated.resources.bible_list_empty
import org.churchpresenter.strings.generated.resources.bible_list_row_hint
import org.churchpresenter.strings.generated.resources.bible_list_scanning
import org.churchpresenter.strings.generated.resources.bible_list_target_hint
import org.churchpresenter.strings.generated.resources.bible_list_target_place
import org.churchpresenter.strings.generated.resources.bible_list_target_primary
import org.churchpresenter.strings.generated.resources.bible_list_target_secondary
import org.churchpresenter.strings.generated.resources.bible_list_title
import org.churchpresenter.theme.components.RaisedFilterChip
import org.jetbrains.compose.resources.stringResource

/**
 * The Bibles card beside the books: every installed Bible, and a double-click puts one on screen.
 *
 * The chips above the list choose which place a double-click fills -- the primary, the secondary,
 * and so on down the stack. Filling it goes through [onShowBible], which writes the stack; the tab
 * reloads on that and sends the verse already selected again, so whatever is live switches to the
 * new Bible straight away.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun BibleVersionsCard(
    storageDirectory: String,
    stack: List<BibleTranslationSettings>,
    onShowBible: (place: Int, fileName: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // The headers are read once per folder; renames and the stack are applied over them in memory.
    val scanned by produceState<Pair<List<String>, Map<String, String>>?>(null, storageDirectory) {
        value = withContext(Dispatchers.IO) {
            val files = installedBibleFiles(storageDirectory)
            files to bibleTitles(storageDirectory, files)
        }
    }
    val entries = remember(scanned, stack) {
        scanned?.let { (files, titles) -> bibleListEntries(files, titles, stack) }
    }
    // Up to two places to fill, or as many as the stack has beyond that; a stack of one still
    // offers the secondary place, which adds it.
    val placeCount = maxOf(2, stack.size)
    var target by rememberSaveable { mutableStateOf(0) }
    if (target >= placeCount) target = 0

    Column(modifier = modifier.bibleListCard()) {
        Box(
            Modifier.fillMaxWidth().height(BIBLE_HEADER_HEIGHT).padding(start = 16.dp, end = 10.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            BibleListHeaderLabel(stringResource(Res.string.bible_list_title))
        }
        PlaceChips(placeCount, target, onTargetChange = { target = it })
        Spacer(Modifier.height(6.dp))
        when {
            entries == null -> ListNotice(stringResource(Res.string.bible_list_scanning))
            entries.isEmpty() -> ListNotice(stringResource(Res.string.bible_list_empty))
            else -> BibleVersionRows(entries, target, placeLabel(target), onShowBible)
        }
    }
}

@Composable
private fun placeLabel(place: Int): String = when (place) {
    0 -> stringResource(Res.string.bible_list_target_primary)
    1 -> stringResource(Res.string.bible_list_target_secondary)
    else -> stringResource(Res.string.bible_list_target_place, place + 1)
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun PlaceChips(placeCount: Int, target: Int, onTargetChange: (Int) -> Unit) {
    TooltipArea(
        tooltip = { ListTooltip(stringResource(Res.string.bible_list_target_hint)) },
        tooltipPlacement = TooltipPlacement.ComponentRect(Alignment.BottomCenter, offset = DpOffset(0.dp, 4.dp)),
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            repeat(placeCount) { place ->
                RaisedFilterChip(
                    selected = place == target,
                    onClick = { onTargetChange(place) },
                    label = {
                        Text(placeLabel(place), style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp))
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BibleVersionRows(
    entries: List<BibleListEntry>,
    target: Int,
    targetLabel: String,
    onShowBible: (place: Int, fileName: String) -> Unit,
) {
    val listState = rememberLazyListState()
    var highlighted by remember { mutableStateOf<String?>(null) }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 6.dp, end = 10.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(rowPad(2.dp)),
        ) {
            items(entries, key = { it.fileName }) { entry ->
                val (hover, hovered) = rememberRowHover()
                val inTarget = entry.place == target
                val colors = bibleRowColors(selected = inTarget || entry.fileName == highlighted, hovered = hovered)
                TooltipArea(
                    tooltip = { ListTooltip(stringResource(Res.string.bible_list_row_hint, entry.name, targetLabel)) },
                    delayMillis = TOOLTIP_DELAY_MS,
                    tooltipPlacement = TooltipPlacement.CursorPoint(offset = DpOffset(0.dp, 16.dp)),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = rowSpan(32.dp))
                            .clip(BibleListRowShape)
                            .background(colors.background)
                            .hoverable(hover)
                            .combinedClickable(
                                interactionSource = hover,
                                indication = null,
                                onClick = { highlighted = entry.fileName },
                                onDoubleClick = {
                                    highlighted = null
                                    onShowBible(target, entry.fileName)
                                },
                            )
                            .padding(horizontal = rowPad(10.dp)),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = entry.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (entry.place != null) FontWeight.SemiBold else FontWeight.Medium,
                            color = colors.ink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        entry.place?.let { PlaceBadge(it) }
                    }
                }
            }
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(listState),
        )
    }
}

/** The place a shown Bible holds -- 1 for the primary, 2 for the secondary. */
@Composable
private fun PlaceBadge(place: Int) {
    Surface(
        shape = CircleShape,
        color = if (place == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
        contentColor = if (place == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onTertiary,
        modifier = Modifier.padding(start = 6.dp),
    ) {
        Text(
            text = (place + 1).toString(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 1.dp),
        )
    }
}

@Composable
private fun ListNotice(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun ListTooltip(text: String) {
    Surface(color = MaterialTheme.colorScheme.inverseSurface, shape = MaterialTheme.shapes.extraSmall) {
        Text(
            text,
            color = MaterialTheme.colorScheme.inverseOnSurface,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

/** The Bibles card's width, and the drag handle beside it, as the book card has. */
@Composable
internal fun BibleVersionsColumn(
    storageDirectory: String,
    stack: List<BibleTranslationSettings>,
    onShowBible: (place: Int, fileName: String) -> Unit,
) {
    val density = LocalDensity.current
    var widthDp by rememberSaveable { mutableStateOf(BIBLE_LIST_WIDTH_DP) }
    BibleVersionsCard(
        storageDirectory = storageDirectory,
        stack = stack,
        onShowBible = onShowBible,
        modifier = Modifier.width(widthDp.dp).fillMaxHeight(),
    )
    DragHandle(onDragEnd = {}) { amountPx ->
        widthDp = (widthDp + amountPx / density.density).coerceIn(BIBLE_LIST_MIN_DP, BIBLE_LIST_MAX_DP)
    }
}

private const val TOOLTIP_DELAY_MS = 900
private const val BIBLE_LIST_WIDTH_DP = 190f
private const val BIBLE_LIST_MIN_DP = 120f
private const val BIBLE_LIST_MAX_DP = 360f
