package com.cozynotes.app.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.cozynotes.app.model.Note
import java.io.File

/**
 * Exports [note] as a real .txt file (so it opens correctly on whatever
 * app/device receives it — Bluetooth, Gmail, Nearby Share, WhatsApp, etc. —
 * rather than being pasted in as plain text) and launches the system share
 * sheet for it.
 *
 * The file is written to the app's cache dir and shared through a
 * FileProvider content:// URI (raw file:// URIs are blocked by Android for
 * cross-app sharing since API 24).
 */
object ShareUtils {

    fun shareNoteAsFile(context: Context, note: Note) {
        val plainContent = RichText.stripMarkup(note.content)
        val safeName = note.title
            .ifBlank { "Note" }
            .replace(Regex("[^A-Za-z0-9 _-]"), "")
            .trim()
            .ifBlank { "Note" }
            .take(60)

        val exportDir = File(context.cacheDir, "shared_notes").apply { mkdirs() }
        val file = File(exportDir, "$safeName.txt")
        file.writeText(buildString {
            append(note.title.ifBlank { "Untitled" })
            append("\n\n")
            append(plainContent)
        })

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, note.title.ifBlank { "Note" })
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share note"))
    }
}
