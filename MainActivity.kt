package com.example.mymusicapp

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerControlView
import java.io.BufferedReader

class MainActivity : AppCompatActivity() {
    private lateinit var player: ExoPlayer
    private lateinit var tvNow: TextView
    private lateinit var tvLyrics: TextView
    private lateinit var lyricsSync: LyricsSync

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvNow = findViewById(R.id.tvNow)
        tvLyrics = findViewById(R.id.tvLyrics)
        val playerView: PlayerControlView = findViewById(R.id.playerView)
        val rv: RecyclerView = findViewById(R.id.rvTracks)
        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        lyricsSync = LyricsSync(player) { line ->
            runOnUiThread { tvLyrics.text = line }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED) {
            loadLocalTracks(rv)
        } else {
            val request = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (granted) loadLocalTracks(rv)
            }
            request.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun loadLocalTracks(rv: RecyclerView) {
        val tracks = mutableListOf<Pair<String, Uri>>()
        val projection = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE)
        val selection = "${MediaStore.Audio.Media.IS_MUSIC}=1"
        val cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, "${MediaStore.Audio.Media.TITLE} ASC")
        cursor?.use {
            val idIdx = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleIdx = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            while (it.moveToNext()) {
                val id = it.getLong(idIdx)
                val title = it.getString(titleIdx)
                val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                tracks.add(title to contentUri)
            }
        }

        rv.layoutManager = LinearLayoutManager(this)
        val adapter = SimpleAdapter(tracks) { title, uri -> playTrack(title, uri) }
        rv.adapter = adapter
    }

    private fun playTrack(title: String, uri: Uri) {
        tvNow.text = title
        player.setMediaItem(MediaItem.fromUri(uri))
        player.prepare()
        player.play()

        lyricsSync.stop()
        tvLyrics.text = "No synced lyrics found."
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
        lyricsSync.stop()
    }
}
