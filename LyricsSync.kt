package com.example.mymusicapp

import android.os.Handler
import android.os.Looper
import com.google.android.exoplayer2.Player
import java.util.regex.Pattern

data class LrcLine(val timeMs: Long, val text: String)

class LrcParser {
    companion object {
        private val timePattern = Pattern.compile("\\[(\\d{1,2}):(\\d{2})(?:\\.(\\d{1,3}))?]")
        fun parse(lrcText: String): List<LrcLine> {
            val lines = mutableListOf<LrcLine>()
            lrcText.lines().forEach { raw ->
                val matcher = timePattern.matcher(raw)
                val texts = mutableListOf<Long>()
                var lastMatchEnd = 0
                while (matcher.find()) {
                    val min = matcher.group(1).toLong()
                    val sec = matcher.group(2).toLong()
                    val ms = matcher.group(3)?.padEnd(3,'0')?.toLong() ?: 0L
                    val timeMs = min*60_000 + sec*1000 + ms
                    texts.add(timeMs)
                    lastMatchEnd = matcher.end()
                }
                if (texts.isNotEmpty()) {
                    val content = if (lastMatchEnd < raw.length) raw.substring(lastMatchEnd).trim() else ""
                    texts.forEach { t -> lines.add(LrcLine(t, content)) }
                }
            }
            return lines.sortedBy { it.timeMs }
        }
    }
}

class LyricsSync(private val player: Player, private val onShowLine: (String) -> Unit) {
    private val handler = Handler(Looper.getMainLooper())
    private var lines: List<LrcLine> = emptyList()
    private var runnable: Runnable? = null

    fun setLyrics(lines: List<LrcLine>) { this.lines = lines }

    fun start() {
        stop()
        runnable = object : Runnable {
            override fun run() {
                val pos = player.currentPosition
                val idx = lines.indexOfLast { it.timeMs <= pos }
                if (idx >= 0) { onShowLine(lines[idx].text) }
                handler.postDelayed(this, 200)
            }
        }
        handler.post(runnable!!)
    }

    fun stop() { runnable?.let { handler.removeCallbacks(it) }; runnable = null }
}
