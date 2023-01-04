package com.example.musicapp.Adapter

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.Models.MusicModel
import com.example.musicapp.PlayerActivity
import com.example.musicapp.R

public class MusicAdapter(private val context: Context, private val songsList: List<MusicModel>) :
    RecyclerView.Adapter<MusicAdapter.ViewHolder>() {

    private var v : View? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
         v = LayoutInflater.from(context).inflate(R.layout.music_item, parent, false)
        return ViewHolder(v!!)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.songName.text = songsList[position].title
        holder.musicArtist.text = songsList[position].artist

        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(songsList[position].path)

        if (retriever.embeddedPicture != null){
            val data: ByteArray = retriever.embeddedPicture
            //val inputStream :InputStream = ByteArrayInputStream(retriever.embeddedPicture)
            val bitmap: Bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
            holder.songImage.setImageBitmap(bitmap)
        }
        else{
            holder.songImage.setImageResource(R.drawable.music_image)
        }

        holder.itemView.setOnClickListener {
            val i = Intent(context, PlayerActivity::class.java)
            i.putExtra("position", position)
                .putExtra("songs", songsList[position].path)
            context.startActivity(i)
        }
        holder.menuMore.setOnClickListener {
            val popupMenu = PopupMenu(context, v)
            popupMenu.menuInflater.inflate(R.menu.menu, popupMenu.menu)
            popupMenu.show()

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.delete -> {
                        deleteMusic(position, v)
                    }
                }
                true
            }

        }

    }

    private fun deleteMusic(position: Int, v: View?) {
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, songsList.size)
        Toast.makeText(context, "deleted", Toast.LENGTH_SHORT).show()


    }

    override fun getItemCount(): Int {
        return songsList.size
    }

    private fun getAlbumArt(uri: String): ByteArray {

        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(uri)
        val art: ByteArray? = retriever.embeddedPicture
        retriever.release()

        return art!!
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val songName = itemView.findViewById<TextView>(R.id.musicName)!!
        val songImage: ImageView = itemView.findViewById(R.id.musicImage)
        val musicArtist: TextView = itemView.findViewById(R.id.musicArtist)
        val menuMore: ImageView  = itemView.findViewById(R.id.menu_options)

    }
}