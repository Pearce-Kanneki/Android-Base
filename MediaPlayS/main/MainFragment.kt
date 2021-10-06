package com.kanneki.mediaplays.ui.main

import android.annotation.SuppressLint
import android.net.Uri
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import coil.load
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kanneki.mediaplays.R
import com.kanneki.mediaplays.base.BaseFragment
import com.kanneki.mediaplays.di.InjectorUtils
import com.kanneki.mediaplays.model.NowPlayingMetadata
import com.kanneki.mediaplays.ui.nowplay.NowPlayViewModel
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.fragment_song.view.*
import kotlinx.android.synthetic.main.section_now_play_small.*
import kotlinx.android.synthetic.main.section_now_play_small.view.*

class MainFragment : BaseFragment<MainViewModel>() {

    private val nowPlayViewModel: NowPlayViewModel by viewModels {
        InjectorUtils.provideNowPlayingViewModel(requireContext())
    }

    override val viewModel: MainViewModel by viewModels {
        InjectorUtils.provideMainViewModel(requireContext())
    }

    override fun getLayoutResId(): Int = R.layout.fragment_main

    override fun initData() {
        disableSeekInSmallSeekBar()
        updateUI(null)

        nowPlayViewModel.mediaMetadata.observe(this){
            updateUI(it)
        }
        nowPlayViewModel.mediaPlayProgress.observe(this){
            updateProgressBar(it)
        }

        nowPlayViewModel.mediaButtonRes.observe(this){
            playPauseButton.isSelected = it
        }

        setClickListener()
    }

    override fun initView(view: View) {
        val navView: BottomNavigationView = view.findViewById(R.id.nav_view)
        val navController = requireActivity().findNavController(R.id.nav_host_fragment)
        navView.setupWithNavController(navController)

        viewModel.songToolBarTitle.observe(viewLifecycleOwner){
            view.songToolBar.title = it
        }
    }

    private fun updateUI(metadata: NowPlayingMetadata?){
        metadata?.let { data ->

            if (data.albumAutUri == Uri.EMPTY) {
                nowPlaySmall.smallCover.setImageResource(R.drawable.ic_music_note)
                nowPlaySmall.smallCover.setBackgroundResource(R.drawable.default_background)
            } else {
                nowPlaySmall.smallCover.load(data.albumAutUri)
            }

            nowPlaySmall.smallTitle.text = data.title
            nowPlaySmall.smallSubTitle.text = data.subtitle
        } // end let

        nowPlaySmall.visibility = if (metadata != null) View.VISIBLE else View.GONE
    }

    private fun setClickListener() {
        nowPlaySmall.playPauseButton.setOnClickListener {
            nowPlayViewModel.mediaMetadata.value?.let { song ->
                viewModel.playMediaId(song.id)
            }
        }
        nowPlaySmall.setOnClickListener {
            // TODO go nowplaying
            findNavController().navigate(R.id.action_mainFragment_to_nowPlayFragment)

        }
    }

    private fun updateProgressBar(progress: Int){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            nowPlaySmall.smallSeekBar.setProgress(progress, true)
        } else {
            nowPlaySmall.smallSeekBar.progress = progress
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun disableSeekInSmallSeekBar() {
        nowPlaySmall.smallSeekBar.setOnTouchListener { _, _ ->  true }
    }
}