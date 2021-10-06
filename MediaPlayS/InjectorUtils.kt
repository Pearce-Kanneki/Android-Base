package com.kanneki.mediaplays.di

import android.content.ComponentName
import android.content.Context
import com.example.mediaplay.media.MusicServiceConnection
import com.example.mediaplay.reppository.SongListRepositoryImpl
import com.kanneki.mediaplays.media.MusicService
import com.kanneki.mediaplays.ui.main.MainViewModel
import com.kanneki.mediaplays.ui.nowplay.NowPlayViewModel
import com.kanneki.mediaplays.ui.playdetail.PlayDetailViewModel

object InjectorUtils {

    private fun provideMusicServiceConnection(context: Context): MusicServiceConnection{
        return MusicServiceConnection.getInstance(
            context,
            ComponentName(context, MusicService::class.java)
        )
    }

    fun provideSongListRepository(context: Context) =
            SongListRepositoryImpl(context.contentResolver)

    fun provideMainViewModel(context: Context): MainViewModel.Factory{
        val contentResolver = context.contentResolver

        return MainViewModel.Factory(
            contentResolver,
            provideSongListRepository(context),
            provideMusicServiceConnection(context)
        )
    }

    fun provideNowPlayingViewModel(context: Context): NowPlayViewModel.Factory{
        val musicServiceConnection = provideMusicServiceConnection(context)

        return NowPlayViewModel.Factory(
                context,
                musicServiceConnection
        )
    }

    fun providePlayDetailViewModel(context: Context): PlayDetailViewModel.Factory{

        return PlayDetailViewModel.Factory(provideSongListRepository(context))
    }
}