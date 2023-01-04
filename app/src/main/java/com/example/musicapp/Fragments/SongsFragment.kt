package com.example.musicapp.Fragments

import android.content.Context
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.Adapter.MusicAdapter
import com.example.musicapp.MainActivity
import com.example.musicapp.Models.MusicModel
import com.example.musicapp.R

class SongsFragment : Fragment() {

    lateinit var recyclerView: RecyclerView
    lateinit var musicAdapter: MusicAdapter

    companion object{
        lateinit var musicFiles : ArrayList<MusicModel>
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v=  inflater.inflate(R.layout.fragment_songs, container, false)

        recyclerView = v.findViewById(R.id.recycler)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        musicFiles = getAudio(context!!)

        if (musicFiles.size > 0) {
            musicAdapter = MusicAdapter(context!!, musicFiles)
            recyclerView.adapter = musicAdapter
        }


        return v
    }

    private fun getAudio(context: Context): ArrayList<MusicModel> {
        val audioList : ArrayList<MusicModel> = ArrayList()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ARTIST
        )
        val cursor = context.contentResolver.query(
            uri, projection, null,
            null, null
        )
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val album = cursor.getString(0)
                val title = cursor.getString(1)
                val duration = cursor.getString(2)
                val path = cursor.getString(3)
                val artist = cursor.getString(4)

                val musicModel = MusicModel(path, title, artist, album, duration)
                audioList.add(musicModel)
            }
            cursor.close()
        }
        return audioList
    }

}