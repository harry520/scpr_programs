package com.harry.scprprograms.view

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.scprprograms.util.Status
import com.harry.scprprograms.R
import com.harry.scprprograms.adapter.ProgramClipsAdapter
import com.harry.scprprograms.databinding.ActivityProgramClipsBinding
import com.harry.scprprograms.model.Clips
import com.harry.scprprograms.repository.SCPRProgramsRepository
import com.harry.scprprograms.viewmodel.ClipsViewModel
import com.harry.scprprograms.viewmodel.ClipsViewModelProviderFactory


class ProgramClipsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProgramClipsBinding
    private lateinit var clipsViewModel: ClipsViewModel
    private val mediaPlayer = MediaPlayer()
    private lateinit var podcastUrl: String
    private var isStopIcon = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProgramClipsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val extras = intent.extras
        var programId: String? = null
        var programArtworkUrl: String? = null
        var programName: String? = null
        var programDescription: String? = null
        if (extras != null) {
            programId = extras.getString("programId")
            programArtworkUrl = extras.getString("programArtworkUrl")
            programName = extras.getString("programName")
            programDescription = extras.getString("programDescription")
        }
        Glide.with(this).load(programArtworkUrl).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .into(binding.programArtwork)
        binding.programDescription.text = programDescription
        Glide.with(this).load(programArtworkUrl).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .into(binding.programArtworkSnap)
        binding.programName.text = programName
        val scprProgramsRepository = SCPRProgramsRepository()
        val clipsViewModelProviderFactory = ClipsViewModelProviderFactory(scprProgramsRepository)
        clipsViewModel =
            ViewModelProvider(this, clipsViewModelProviderFactory)[ClipsViewModel::class.java]
        clipsViewModel.getProgramClips(programId!!)
        lifecycleScope.launchWhenStarted {
            clipsViewModel.programClips.collect {
                when (it.status) {
                    Status.EMPTY -> Unit
                    Status.SUCCESS -> displayClips(it.data?.body()!!)
                    Status.ERROR -> Toast.makeText(
                        this@ProgramClipsActivity,
                        "Something went wrong.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        binding.playPauseIcon.setOnClickListener {
            if (isStopIcon) {
                binding.playPauseIcon.setImageResource(R.drawable.play_arrow)
                isStopIcon = false
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                    mediaPlayer.reset()
                }
            } else {
                binding.playPauseIcon.setImageResource(R.drawable.stop)
                isStopIcon = true
                playAudio(podcastUrl)
            }
        }
    }

    private fun displayClips(clips: Clips) {
        val programClipsAdapter =
            ProgramClipsAdapter(object : ProgramClipsAdapter.RecyclerItemClickListener {
                override fun onClickListener(audioUrl: String) {
                    podcastUrl = audioUrl
                    binding.playPauseIcon.visibility = View.VISIBLE
                    if (isStopIcon) {
                        binding.playPauseIcon.setImageResource(R.drawable.stop)
                        isStopIcon = true
                        playAudio(audioUrl)
                    } else {
                        binding.playPauseIcon.setImageResource(R.drawable.play_arrow)
                        isStopIcon = false
                        if (mediaPlayer.isPlaying) {
                            mediaPlayer.stop()
                            mediaPlayer.reset()
                        } else {
                            binding.playPauseIcon.setImageResource(R.drawable.stop)
                            isStopIcon = true
                            playAudio(audioUrl)
                        }
                    }
                }

            })
        if (clips.Clips.size <= 50)
            programClipsAdapter.differ.submitList(clips.Clips)
        else
            programClipsAdapter.differ.submitList(clips.Clips.subList(0, 50))
        val linearLayoutManager =
            LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
        binding.programClipsRecyclerView.apply {
            layoutManager = linearLayoutManager
            this.adapter = programClipsAdapter
            addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
        }
    }

        fun playAudio(audioUrl: String) {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.reset()
        }
        mediaPlayer.setAudioAttributes(
            AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
        )
        try {
            mediaPlayer.setDataSource(audioUrl)
            mediaPlayer.prepare()
            mediaPlayer.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        mediaPlayer.release()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
}