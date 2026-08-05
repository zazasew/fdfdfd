package com.cozynotes.app.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/**
 * A tiny, dependency-free rich-text engine for the note editor.
 *
 * Notes are stored as plain text with lightweight inline tags — [b]bold[/b],
 * [i]italic[/i], [u]underline[/u], [h]highlight[/h], [c=RRGGBB]color[/c] — so
 * they remain simple, greppable strings in Room with no custom serialization.
 * [visualTransformation] renders those tags as real bold/italic/underline/
 * highlight/color live in the TextField, hiding the bracket syntax as you type.
 *
 * Checklist lines ("☐ " / "☑ ") are plain characters too — a checked line
 * ("☑ ") automatically gets a strikethrough span over the rest of that line.
 */
object RichText {

    private val TAG_REGEX = Regex("\\[(/?)([a-zA-Z]+)(?:=([0-9A-Fa-f]{6}))?]")
    private val NUMBERED_PREFIX_REGEX = Regex("^\\d+\\.\\s")

    private data class ParseResult(
        val plain: String,
        val origToTrans: IntArray,
        val transToOrig: IntArray,
        val spans: List<Triple<Int, Int, SpanStyle>>
    )

    private fun styleForKey(key: String): SpanStyle? = when {
        key == "b" -> SpanStyle(fontWeight = FontWeight.Bold)
        key == "i" -> SpanStyle(fontStyle = FontStyle.Italic)
        key == "u" -> SpanStyle(textDecoration = TextDecoration.Underline)
        key == "h" -> SpanStyle(background = Color(0xFFFFF3C4))
        key.startsWith("c:") -> runCatching {
            SpanStyle(color = Color(("FF" + key.substring(2)).toLong(16).toInt()))
        }.getOrNull()
        else -> null
    }

    private fun parse(raw: String): ParseResult {
        val plainBuilder = StringBuilder()
        val origToTrans = IntArray(raw.length + 1)
        val transToOrigList = ArrayList<Int>(raw.length + 1)
        val spans = ArrayList<Triple<Int, Int, SpanStyle>>()
        val openStack = ArrayDeque<Pair<String, Int>>()

        var i = 0
        while (i < raw.length) {
            val m = TAG_REGEX.matchAt(raw, i)
            if (m != null) {
                val closing = m.groupValues[1] == "/"
                val tagName = m.groupValues[2]
                val colorHex = m.groupValues[3].takeIf { it.isNotEmpty() }
                val tagKey = if (tagName == "c" && colorHex != null) "c:$colorHex" else tagName

                if (!closing) {
                    openStack.addLast(tagKey to plainBuilder.length)
                } else {
                    val stackIdx = openStack.indexOfLast {
                        it.first == tagKey || (tagName == "c" && it.first.startsWith("c:"))
                    }
                    if (stackIdx >= 0) {
                        val (key, start) = openStack[stackIdx]
                        openStack.removeAt(stackIdx)
                        val end = plainBuilder.length
                        styleForKey(key)?.let { style ->
                            if (end > start) spans.add(Triple(start, end, style))
                        }
                    }
                }
                for (k in i..m.range.last) origToTrans[k] = plainBuilder.length
                i = m.range.last + 1
                continue
            }
            origToTrans[i] = plainBuilder.length
            transToOrigList.add(i)
            plainBuilder.append(raw[i])
            i++
        }
        origToTrans[raw.length] = plainBuilder.length
        transToOrigList.add(raw.length)

        val plain = plainBuilder.toString()

        // Checked checklist lines ("☑ ...") get a strikethrough span over the
        // rest of the line (not the checkbox glyph itself), so ticking an
        // item visually crosses it out the way any checklist app would.
        var lineStart = 0
        while (lineStart <= plain.length) {
            val lineEnd = plain.indexOf('\n', lineStart).let { if (it == -1) plain.length else it }
            val line = plain.substring(lineStart, lineEnd)
            if (line.startsWith("\u2611 ") && lineEnd > lineStart + 2) {
                spans.add(Triple(lineStart + 2, lineEnd, SpanStyle(textDecoration = TextDecoration.LineThrough)))
            }
            if (lineEnd == plain.length) break
            lineStart = lineEnd + 1
        }

        return ParseResult(
            plain = plain,
            origToTrans = origToTrans,
            transToOrig = transToOrigList.toIntArray(),
            spans = spans
        )
    }

    /** Plain, tag-free text — used for word/char counts, note-list previews, and file export. */
    fun stripMarkup(raw: String): String = parse(raw).plain

    fun toAnnotatedString(raw: String): AnnotatedString {
        val result = parse(raw)
        return buildAnnotatedString {
            append(result.plain)
            result.spans.forEach { (start, end, style) -> addStyle(style, start, end) }
        }
    }

    /** Live rich-text rendering inside the note editor's TextField. */
    fun visualTransformation(): VisualTransformation = VisualTransformation { text ->
        val result = parse(text.text)
        val annotated = buildAnnotatedString {
            append(result.plain)
            result.spans.forEach { (start, end, style) -> addStyle(style, start, end) }
        }
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                result.origToTrans[offset.coerceIn(0, result.origToTrans.size - 1)]

            override fun transformedToOriginal(offset: Int): Int =
                result.transToOrig[offset.coerceIn(0, result.transToOrig.size - 1)]
        }
        TransformedText(annotated, mapping)
    }

    /** Toggles an inline tag pair around the current selection (or inserts an
     *  empty pair at the cursor). Tapping the same format again on already
     *  wrapped text removes it — matching how Bold/Italic buttons behave in any
     *  standard text editor. */
    fun toggleInlineTag(value: TextFieldValue, tag: String, colorHex: String? = null): TextFieldValue {
        val open = if (colorHex != null) "[$tag=$colorHex]" else "[$tag]"
        val close = "[/$tag]"
        val text = value.text
        val selStart = value.selection.min
        val selEnd = value.selection.max

        val before = text.substring(0, selStart)
        val after = text.substring(selEnd)

        return if (before.endsWith(open) && after.startsWith(close)) {
            val newBefore = before.removeSuffix(open)
            val newAfter = after.removeRange(0, close.length)
            val newText = newBefore + text.substring(selStart, selEnd) + newAfter
            val delta = open.length
            TextFieldValue(newText, TextRange(selStart - delta, selEnd - delta))
        } else {
            val selected = text.substring(selStart, selEnd)
            val newText = before + open + selected + close + after
            val newStart = selStart + open.length
            val newEnd = newStart + selected.length
            TextFieldValue(newText, TextRange(newStart, newEnd))
        }
    }

    private fun currentLine(text: String, cursor: Int): Triple<Int, Int, String> {
        val lineStart = if (cursor == 0) 0 else (text.lastIndexOf('\n', cursor - 1) + 1)
        val lineEnd = text.indexOf('\n', lineStart).let { if (it == -1) text.length else it }
        return Triple(lineStart, lineEnd, text.substring(lineStart, lineEnd))
    }

    /** Whatever list-style prefix (if any) a line currently starts with. */
    private fun existingPrefixOf(line: String): String? = when {
        line.startsWith("\u2022 ") -> "\u2022 "
        line.startsWith("\u2610 ") -> "\u2610 "
        line.startsWith("\u2611 ") -> "\u2611 "
        else -> NUMBERED_PREFIX_REGEX.find(line)?.let { if (it.range.first == 0) it.value else null }
    }

    /** Toggles a list-style prefix ("• " or "1. ") on the current line,
     *  clearing any other list prefix first so lines don't stack markers.
     *  For numbered lists, this recognizes *any* existing "N. " prefix
     *  (not just literally "1. "), so toggling off a continued item like
     *  "5. " works correctly instead of stacking "1. 5. ". */
    fun toggleLinePrefix(value: TextFieldValue, prefix: String): TextFieldValue {
        val text = value.text
        val cursor = value.selection.start
        val (lineStart, lineEnd, line) = currentLine(text, cursor)

        val isTargetNumbered = NUMBERED_PREFIX_REGEX.matches(prefix)
        val existingPrefix = existingPrefixOf(line)
        val alreadyHasTargetKind = if (isTargetNumbered) {
            existingPrefix != null && NUMBERED_PREFIX_REGEX.matches(existingPrefix)
        } else {
            existingPrefix == prefix
        }

        val stripped = if (existingPrefix != null) line.removePrefix(existingPrefix) else line
        val finalLine = if (alreadyHasTargetKind) stripped else prefix + stripped

        val newText = text.substring(0, lineStart) + finalLine + text.substring(lineEnd)
        val delta = finalLine.length - line.length
        val newCursor = (cursor + delta).coerceIn(lineStart, lineStart + finalLine.length)
        return value.copy(text = newText, selection = TextRange(newCursor))
    }

    /** Checkbox button behavior: if the current line has no checkbox yet,
     *  add an unchecked one. If it already has one, flip it between
     *  unchecked ↔ checked — checking it on triggers the strikethrough
     *  rendering above. This is the "tap the line, then tap the checkbox
     *  icon to mark it done" flow. */
    fun toggleCheckbox(value: TextFieldValue): TextFieldValue {
        val text = value.text
        val cursor = value.selection.start
        val (lineStart, lineEnd, line) = currentLine(text, cursor)

        val newLine = when {
            line.startsWith("\u2610 ") -> "\u2611 " + line.removePrefix("\u2610 ")
            line.startsWith("\u2611 ") -> "\u2610 " + line.removePrefix("\u2611 ")
            existingPrefixOf(line) != null -> "\u2610 " + line.removePrefix(existingPrefixOf(line)!!)
            else -> "\u2610 $line"
        }
        val newText = text.substring(0, lineStart) + newLine + text.substring(lineEnd)
        val delta = newLine.length - line.length
        val newCursor = (cursor + delta).coerceIn(lineStart, lineStart + newLine.length)
        return value.copy(text = newText, selection = TextRange(newCursor))
    }

    /**
     * Called on every content change. If the change was exactly "the user
     * pressed Enter" (one '\n' inserted at the cursor, nothing else changed),
     * this auto-continues the list the cursor was in:
     *  - "• " continues as "• "
     *  - "☐ "/"☑ " continues as a fresh unchecked "☐ "
     *  - "N. " continues as "(N+1). "
     * Pressing Enter on an already-empty list item (just the bare prefix,
     * nothing typed after it) exits the list instead of continuing it —
     * the same convention every note app uses.
     * Any other kind of edit (typing, pasting, deleting) passes through
     * unchanged.
     */
    fun continueListOnEnter(old: TextFieldValue, new: TextFieldValue): TextFieldValue {
        if (!new.selection.collapsed) return new
        val oldText = old.text
        val newText = new.text
        val cursor = new.selection.start
        if (newText.length != oldText.length + 1) return new
        if (cursor < 1 || cursor > newText.length || newText[cursor - 1] != '\n') return new

        val before = newText.substring(0, cursor - 1)
        val after = newText.substring(cursor)
        if (before + after != oldText) return new

        val prevLineStart = before.lastIndexOf('\n').let { if (it == -1) 0 else it + 1 }
        val prevLine = before.substring(prevLineStart)
        val prefix = existingPrefixOf(prevLine) ?: return new

        return if (prevLine == prefix) {
            // The list item was left empty — Enter here exits the list by
            // clearing the dangling prefix from that now-finished line.
            val strippedBefore = before.substring(0, prevLineStart)
            new.copy(text = strippedBefore + after, selection = TextRange(prevLineStart))
        } else {
            val continuation = when {
                prefix == "\u2022 " -> "\u2022 "
                prefix == "\u2610 " || prefix == "\u2611 " -> "\u2610 "
                NUMBERED_PREFIX_REGEX.matches(prefix) -> {
                    val num = prefix.trim().removeSuffix(".").toIntOrNull() ?: return new
                    "${num + 1}. "
                }
                else -> return new
            }
            new.copy(text = before + continuation + after, selection = TextRange(cursor + continuation.length))
        }
    }
}
