package com.example.musicapp.Models

class MusicModel {
    var path: String? = ""
    var title: String? = ""
    var artist: String? = ""
    var album: String? = ""
    var duration: String? = ""


    constructor()
    constructor(path: String?, title: String?, artist: String?, album: String?, duration: String?) {
        this.path = path
        this.title = title
        this.artist = artist
        this.album = album
        this.duration = duration
    }

}