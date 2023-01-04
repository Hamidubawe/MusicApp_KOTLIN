package com.example.musicapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager.widget.ViewPager
import com.example.musicapp.Fragments.SongsFragment
import com.example.musicapp.Models.MusicModel
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {
    private val REQUEST_CODE = 1
    lateinit var musicFiles : ArrayList<MusicModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permission()

    }


    private fun permission(){
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_CODE
            )

        }
        else{
            supportFragmentManager.beginTransaction()
                .replace(R.id.framelayout, SongsFragment())
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
        permissions: Array<out String>,grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE){

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){

                supportFragmentManager.beginTransaction()
                    .replace(R.id.framelayout, SongsFragment())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit()
            }
        }
        else{


            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_CODE
            )
        }
    }


}