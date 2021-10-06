package com.kanneki.mediaplays.ui.main

import android.content.ContentResolver
import android.database.ContentObserver
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.*
import com.example.mediaplay.extension.*
import com.example.mediaplay.media.MusicServiceConnection
import com.example.mediaplay.reppository.SongListRepository
import com.kanneki.mediaplays.model.Song
import kotlinx.coroutines.launch

private const val TAG = "MainViewModel"

class MainViewModel(
    private val contentResolver: ContentResolver,
    private val songListRepository: SongListRepository,
    private val musicServiceConnection: MusicServiceConnection
): ViewModel() {


    private var contentObserver: ContentObserver? = null
    private val _songs = MutableLiveData<List<Song>>()
    val songs: LiveData<List<Song>> get() = _songs
    private val _songToolBarTitle = MutableLiveData<String>()
    val songToolBarTitle: LiveData<String> get() = _songToolBarTitle

    fun setSongToolBarTitle(name: String){
        _songToolBarTitle.postValue(name)
    }

    fun loadSongs(list: List<Song> = listOf()){
        viewModelScope.launch {
            if (list.isEmpty()){
                _songs.postValue(querySongs())
            } else {
                _songs.postValue(list)
            }

            contentObserver ?:run {
                contentObserver = contentResolver.registerObserver(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                ){
                    loadSongs(list)
                }
            }
        }
    }

    fun playMedia(mediaItem: Song, pauseAllowed: Boolean = true){
        val nowPlaying = musicServiceConnection.nowPlaying.value
        val transportControls = musicServiceConnection.transportControls

        val isPrepared = musicServiceConnection.playbackState.value?.isPrepared ?: false
        if (isPrepared && mediaItem.id.toString() == nowPlaying?.id){
            musicServiceConnection.playbackState.value?.let { playbackState ->
                when{
                    playbackState.isPlaying -> {
                        if (pauseAllowed) transportControls.pause() else Unit
                    }
                    else -> {
                        Log.w(
                            TAG,
                            "Playable item clicked but neither play nor pause are enabled!" +
                                    " (mediaId=${mediaItem.id})"
                        )
                    }
                } // end when
            }
        } else {
            transportControls.playFromMediaId(mediaItem.id.toString(), null)
        }

    }

    fun playMediaId(mediaId: String) {
        val nowPlaying = musicServiceConnection.nowPlaying.value
        val transportControls = musicServiceConnection.transportControls

        val isPrepared = musicServiceConnection.playbackState.value?.isPrepared ?: false
        if (isPrepared && mediaId == nowPlaying?.id){
            musicServiceConnection.playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> transportControls.pause()
                    playbackState.isPlayEnabled -> transportControls.play()
                    else -> {
                        Log.w(
                            TAG,
                            "Playable item clicked but neither play nor pause are enabled!" +
                                    " (mediaId=$mediaId)"
                        )
                    }
                } // end when
            }
        } else {
            transportControls.playFromMediaId(mediaId, null)
        }
    }

    private suspend fun querySongs(): List<Song> =
        songListRepository.getSongs()

    override fun onCleared() {
        contentObserver?.let {
            contentResolver.unregisterContentObserver(it)
        }
    }

    class Factory(
        private val contentResolver: ContentResolver,
        private val songListRepository: SongListRepository,
        private val musicServiceConnection: MusicServiceConnection
    ): ViewModelProvider.NewInstanceFactory(){

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MainViewModel(
                contentResolver,
                songListRepository,
                musicServiceConnection
            ) as T
        }
    }

}