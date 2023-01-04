package com.example.musicapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.musicapp.Fragments.SongsFragment.Companion.musicFiles
import com.example.musicapp.Models.MusicModel
import kotlinx.android.synthetic.main.activity_player.*
import kotlin.random.Random

class PlayerActivity : AppCompatActivity()  {

    private var position: Int = -1
    private var songList: List<MusicModel> = ArrayList()
    private lateinit var uri: Uri
    companion object{
        private var mediaPlayer: MediaPlayer? = null
    }
    private lateinit var seekBar: SeekBar
    private var handler: Handler = Handler()
    private lateinit var playPauseThread: Thread
    private lateinit var nextBtnThread: Thread
    private lateinit var prevBtnThread: Thread
    private var shuffleOn = false
    private var repeatOn = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        position = intent.getIntExtra("position", -1)

        songList = musicFiles
        uri = Uri.parse(songList[position].path)
        seekBar = findViewById(R.id.seekbar)

        songName.text = songList[position].title
        songArtist.text = songList[position].artist
        musicAlbum(uri)



        if (mediaPlayer != null ) {

            if (mediaPlayer!!.isPlaying){
                mediaPlayer!!.stop()
                mediaPlayer!!.release()
                mediaPlayer = MediaPlayer.create(applicationContext, uri)
                mediaPlayer!!.start()
                pausePlay.setImageResource(R.drawable.ic_pause)
            }

        }
        else {
            mediaPlayer = MediaPlayer.create(this, uri)
            mediaPlayer!!.start()
        }
        seekBar.max = mediaPlayer!!.duration / 1000

        mediaPlayer!!.setOnCompletionListener {
            nextClicked()
            if (mediaPlayer != null){
                mediaPlayer = MediaPlayer.create(this, uri)
                mediaPlayer!!.start()
            }

        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer!!.seekTo(progress * 1000)
                }

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {


            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        runOnUiThread(object : Runnable {
            override fun run() {
                if (mediaPlayer != null) {
                    val mCurrentPosition = mediaPlayer!!.currentPosition / 1000
                    seekBar.progress = mCurrentPosition
                    durationPlayed.text = formattedTime(mCurrentPosition)
                }
                handler.postDelayed(this, 1000)
            }
        })

        shuffle.setOnClickListener {
            if (shuffleOn){
                shuffleOn = false
                shuffle.setImageResource(R.drawable.ic_shuffle_off)
            }
            else{
                shuffleOn = true
                shuffle.setImageResource(R.drawable.ic_shuffle)
            }
        }

        repeat.setOnClickListener {
            if (repeatOn){
                repeatOn = false
                repeat.setImageResource(R.drawable.ic_repeat_off)
            }
            else{
                repeatOn = true
                repeat.setImageResource(R.drawable.ic_repeat)
            }
        }

    }

    private fun formattedTime(mCurrentPosition: Int): String? {
        val totalOut: String
        val totalNew: String
        val seconds = java.lang.String.valueOf(mCurrentPosition % 60)
        val minutes = java.lang.String.valueOf(mCurrentPosition / 60)

        totalOut = "$minutes:$seconds"
        totalNew = "$minutes:0$seconds"

        return if (seconds.length == 1)
            totalNew
        else
            totalOut

    }

    private fun getAudio(context: Context): ArrayList<MusicModel> {
        val audioList: ArrayList<MusicModel> = ArrayList()
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

    private fun musicAlbum(uri: Uri){

        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(songList[position].path)
        val duration : Int = Integer.parseInt(songList[position].duration!!)
        durationTotal.text = formattedTime(duration / 1000)



        if (retriever.embeddedPicture != null){
            val data: ByteArray = retriever.embeddedPicture
            //val inputStream :InputStream = ByteArrayInputStream(retriever.embeddedPicture)
            val bitmap: Bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
            imageAnimation(this, musicPhoto, bitmap)
            //musicPhoto.setImageBitmap(bitmap)
        }
        else{
            musicPhoto.setImageResource(R.drawable.music_image)
        }
    }

    private fun imageAnimation(context: Context, imageView: ImageView, bitmap: Bitmap){

        val animOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out)
        val animIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)

        animOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {


            }

            override fun onAnimationRepeat(animation: Animation?) {


            }

            override fun onAnimationEnd(animation: Animation?) {

                Glide.with(context).load(bitmap).into(imageView)
                animIn.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationEnd(animation: Animation?) {


                    }

                    override fun onAnimationRepeat(animation: Animation?) {


                    }

                    override fun onAnimationStart(animation: Animation?) {

                    }
                })
                imageView.startAnimation(animIn)
            }
        })
        imageView.startAnimation(animOut)
    }

    override fun onResume() {

        playThreadBtn()
        nextThreadBtn()
        prevThreadBtn()


        super.onResume()
    }

    private fun prevThreadBtn() {

        prevBtnThread = Thread {
            previous.setOnClickListener {
                if (mediaPlayer!!.isPlaying) {
                    mediaPlayer!!.stop()
                    mediaPlayer!!.release()

                    if (shuffleOn  && !repeatOn){
                        position = getRandom(songList.size)
                    }
                    else if (!shuffleOn && !repeatOn){
                        position = if (position - 1 < 0) songList.size else position - 1
                    }

                    uri = Uri.parse(songList[position].path)
                    mediaPlayer = MediaPlayer.create(applicationContext, uri)
                    musicAlbum(uri)
                    songName.text = songList[position].title
                    songArtist.text = songList[position].artist
                    seekBar.max = mediaPlayer!!.duration / 1000

                    runOnUiThread(object : Runnable {
                        override fun run() {
                            if (mediaPlayer != null) {
                                val mCurrentPosition = mediaPlayer!!.currentPosition / 1000
                                seekBar.progress = mCurrentPosition

                            }
                            handler.postDelayed(this, 1000)
                        }
                    })
                    mediaPlayer!!.start()
                    pausePlay.setImageResource(R.drawable.ic_pause)

                }
                else {
                    pausePlay.setImageResource(R.drawable.ic_play)
                    mediaPlayer!!.stop()
                    mediaPlayer!!.release()

                    if (shuffleOn  && !repeatOn){
                        position = getRandom(songList.size)
                    }
                    else if (!shuffleOn && !repeatOn){
                        position = if (position - 1 < 0) songList.size else position - 1
                    }
                    uri = Uri.parse(songList[position].path)
                    mediaPlayer = MediaPlayer.create(applicationContext, uri)
                    musicAlbum(uri)
                    songName.text = songList[position].title
                    songArtist.text = songList[position].artist
                    seekBar.max = mediaPlayer!!.duration / 1000

                    runOnUiThread(object : Runnable {
                        override fun run() {
                            if (mediaPlayer != null) {
                                val mCurrentPosition = mediaPlayer!!.currentPosition / 1000
                                seekBar.progress = mCurrentPosition

                            }
                            handler.postDelayed(this, 1000)
                        }
                    })
                }
            }
        }
        prevBtnThread.start()


    }

    private fun nextThreadBtn() {

        nextBtnThread = Thread {
            nextMusic.setOnClickListener {
                nextClicked()
            }
        }
        nextBtnThread.start()

    }

    private fun playThreadBtn() {

        playPauseThread = Thread {
            pausePlay.setOnClickListener {
                if (mediaPlayer!!.isPlaying) {
                    pausePlay.setImageResource(R.drawable.ic_play)
                    mediaPlayer!!.pause()
                    seekBar.max = mediaPlayer!!.duration/1000

                    runOnUiThread(object : Runnable {
                        override fun run() {
                            if (mediaPlayer != null) {
                                val mCurrentPosition = mediaPlayer!!.currentPosition / 1000
                                seekBar.progress = mCurrentPosition

                            }
                            handler.postDelayed(this, 1000)
                        }
                    })

                } else {
                    pausePlay.setImageResource(R.drawable.ic_pause)
                    mediaPlayer!!.start()
                    seekBar.max = mediaPlayer!!.duration / 1000

                    runOnUiThread(object : Runnable {
                        override fun run() {
                            if (mediaPlayer != null) {
                                val mCurrentPosition = mediaPlayer!!.currentPosition / 1000
                                seekBar.progress = mCurrentPosition

                            }
                            handler.postDelayed(this, 1000)
                        }
                    })
                }
            }
        }
        playPauseThread.start()

    }

    private fun nextClicked(){
        if (mediaPlayer!!.isPlaying) {
            mediaPlayer!!.stop()
            mediaPlayer!!.release()

            if (shuffleOn  && !repeatOn){
                position = getRandom(songList.size)
            }
            else if (!shuffleOn && !repeatOn){
                position = (position + 1) % songList.size
            }

            uri = Uri.parse(songList[position].path)
            mediaPlayer = MediaPlayer.create(applicationContext, uri)
            musicAlbum(uri)
            songName.text = songList[position].title
            songArtist.text = songList[position].artist
            seekBar.max = mediaPlayer!!.duration / 1000

            runOnUiThread(object : Runnable {
                override fun run() {
                    if (mediaPlayer != null) {
                        val mCurrentPosition = mediaPlayer!!.currentPosition / 1000
                        seekBar.progress = mCurrentPosition

                    }
                    handler.postDelayed(this, 1000)
                }
            })
            mp()
            mediaPlayer!!.start()
            pausePlay.setImageResource(R.drawable.ic_pause)

        }
        else {
            pausePlay.setImageResource(R.drawable.ic_play)
            mediaPlayer!!.stop()
            mediaPlayer!!.release()
            mp()

            if (shuffleOn  && !repeatOn){
                position = getRandom(songList.size)
            }
            else if (!shuffleOn && !repeatOn){
                position = (position + 1) % songList.size
            }
            uri = Uri.parse(songList[position].path)
            mediaPlayer = MediaPlayer.create(applicationContext, uri)
            musicAlbum(uri)
            songName.text = songList[position].title
            songArtist.text = songList[position].artist
            seekBar.max = mediaPlayer!!.duration / 1000

            runOnUiThread(object : Runnable {
                override fun run() {
                    if (mediaPlayer != null) {
                        val mCurrentPosition = mediaPlayer!!.currentPosition / 1000
                        seekBar.progress = mCurrentPosition

                    }
                    handler.postDelayed(this, 1000)
                }
            })
        }
    }

    private fun getRandom(size: Int): Int {
        val random = Random
        return random.nextInt(size - 1)
    }

    private fun mp(){
        mediaPlayer!!.setOnCompletionListener {
            nextClicked()
            if (mediaPlayer != null){
                mediaPlayer = MediaPlayer.create(this, uri)
                mediaPlayer!!.start()
            }

        }

    }

}

