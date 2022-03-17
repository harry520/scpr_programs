package com.harry.scprprograms.view

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.scprprograms.util.Status
import com.harry.scprprograms.R
import com.harry.scprprograms.`interface`.ActionPlaying
import com.harry.scprprograms.adapter.ProgramClipsAdapter
import com.harry.scprprograms.databinding.ActivityProgramClipsBinding
import com.harry.scprprograms.model.Clips
import com.harry.scprprograms.receiver.NotificationReceiver
import com.harry.scprprograms.repository.SCPRProgramsRepository
import com.harry.scprprograms.service.ProgramClipsService
import com.harry.scprprograms.util.Constants.ACTION_FAST_FORWARD
import com.harry.scprprograms.util.Constants.ACTION_PLAY_PAUSE
import com.harry.scprprograms.util.Constants.ACTION_REWIND
import com.harry.scprprograms.util.Constants.CHANNEL_ID_2
import com.harry.scprprograms.viewmodel.ClipsViewModel
import com.harry.scprprograms.viewmodel.ClipsViewModelProviderFactory
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class ProgramClipsActivity : AppCompatActivity(), ActionPlaying {
    private lateinit var binding: ActivityProgramClipsBinding
    private lateinit var clipsViewModel: ClipsViewModel
    private var programArtworkUrl: String? = null
    private var programDescription: String? = null
    private var programName: String? = null
    private val mediaPlayer = MediaPlayer()
    private lateinit var podcastUrl: String
    private lateinit var notificationManager: NotificationManager
    private lateinit var mediaSession: MediaSessionCompat
    private var programClipsService: ProgramClipsService? = null
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            val binder = p1 as ProgramClipsService.MyBinder
            programClipsService = binder.getService()
            programClipsService!!.setCallback(this@ProgramClipsActivity)
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            programClipsService = null
        }
    }
    private lateinit var notifyIntent: Intent
    private lateinit var notificationReceiver: NotificationReceiver
    private var isBound = false
    private var largeIcon: Bitmap? = null
    private var isPauseIcon = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProgramClipsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val extras = intent.extras
        var programId: String? = null
        if (extras != null) {
            programId = extras.getString("programId")
            programArtworkUrl = extras.getString("programArtworkUrl")
            programName = extras.getString("programName")
            programDescription = extras.getString("programDescription")
        }
        mediaSession = MediaSessionCompat(this, "PlayerAudio")
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationReceiver = NotificationReceiver()
            registerReceiver(notificationReceiver, IntentFilter("PROGRAM_CLIP"))
            notifyIntent = Intent(this, ProgramClipsService::class.java)
            isBound = bindService(notifyIntent, connection, BIND_AUTO_CREATE)
            startService(notifyIntent)
        }
        Glide.with(this).asBitmap().load(programArtworkUrl)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    largeIcon = resource
                    binding.programArtworkSnap.setImageBitmap(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
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
        binding.rewindIcon.setOnClickListener {
            rewindClicked()
        }
        binding.playPauseIcon.setOnClickListener {
            playPauseClicked()
        }
        binding.fastForwardIcon.setOnClickListener {
            fastForwardClicked()
        }
        if (mediaPlayer.currentPosition == mediaPlayer.duration) {
            isPauseIcon = false
            binding.playPauseIcon.setImageResource(R.drawable.ic_play_arrow)
            mediaPlayer.stop()
            mediaPlayer.reset()
        }
    }

    private fun displayClips(clips: Clips) {
        val programClipsAdapter = ProgramClipsAdapter(
            programArtworkUrl,
            programDescription,
            object : ProgramClipsAdapter.RecyclerItemClickListener {
                override fun onClickListener(audioUrl: String) {
                    podcastUrl = audioUrl
                    val sizeInDp = 60f
                    val marginInDp = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        sizeInDp,
                        resources.displayMetrics
                    ).toInt()
                    val params = binding.topLayout.layoutParams as ViewGroup.MarginLayoutParams
                    params.bottomMargin = marginInDp
                    binding.topLayout.layoutParams = params
                    binding.clipPlayerLayout.visibility = View.VISIBLE
                    if (isPauseIcon) {
                        isPauseIcon = true
                        playAudio(audioUrl)
                        isVolumeOff()
                    } else {
                        isPauseIcon = false
                        mediaPlayer.stop()
                        mediaPlayer.reset()
                        binding.playPauseIcon.setImageResource(R.drawable.ic_pause)
                        isPauseIcon = true
                        playAudio(audioUrl)
                        isVolumeOff()
                    }
                    createNotification(R.drawable.ic_pause)
                }
            })
        if (clips.Clips.size <= 50)
            programClipsAdapter.differ.submitList(clips.Clips)
        else
            programClipsAdapter.differ.submitList(clips.Clips.subList(0, 50))
        val linearLayoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.programClipsRecyclerView.apply {
            layoutManager = linearLayoutManager
            this.adapter = programClipsAdapter
            addItemDecoration(
                DividerItemDecoration(
                    this@ProgramClipsActivity,
                    DividerItemDecoration.VERTICAL
                )
            )
        }
    }

    private fun playAudio(audioUrl: String) {
        isPauseIcon = true
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

    private fun isVolumeOff() {
        val audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (audio.getStreamVolume(AudioManager.STREAM_MUSIC) == 0)
            Toast.makeText(this@ProgramClipsActivity, "Volume is off", Toast.LENGTH_SHORT).show()
    }

    private fun createNotification(playPauseBtn: Int) {
        val contentIntent = PendingIntent.getActivity(
            this, 0, notifyIntent, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            else
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        val rewindIntent =
            Intent(this, NotificationReceiver::class.java).setAction(ACTION_REWIND)
        val rewindPendingIntent = PendingIntent.getBroadcast(
            this, 0, rewindIntent, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            else
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        val playPauseIntent =
            Intent(this, NotificationReceiver::class.java).setAction(ACTION_PLAY_PAUSE)
        val playPausePendingIntent = PendingIntent.getBroadcast(
            this, 0, playPauseIntent, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            else
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        val fastForwardIntent =
            Intent(this, NotificationReceiver::class.java).setAction(ACTION_FAST_FORWARD)
        val fastForwardPendingIntent = PendingIntent.getBroadcast(
            this, 0, fastForwardIntent, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            else
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification =
            NotificationCompat.Builder(this, CHANNEL_ID_2).setLargeIcon(largeIcon)
                .setSmallIcon(R.drawable.ic_music_note)
                .setContentTitle(programName)
                .addAction(R.drawable.ic_rewind, "Rewind", rewindPendingIntent)
                .addAction(playPauseBtn, "Play Pause", playPausePendingIntent)
                .addAction(R.drawable.ic_fast_forward, "Fast Forward", fastForwardPendingIntent)
                .setStyle(
                    androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.sessionToken)
                        .setShowActionsInCompactView(0, 1, 2)
                ).setPriority(NotificationCompat.PRIORITY_LOW).setContentIntent(contentIntent)
                .setSilent(true)
        notificationManager.notify(1, notification.build())
    }

    override fun rewindClicked() {
        if (isPauseIcon)
            if (mediaPlayer.currentPosition < 30000)
                mediaPlayer.seekTo(0)
            else
                mediaPlayer.seekTo(mediaPlayer.currentPosition - 30000)
    }

    override fun playPauseClicked() {
        if (isPauseIcon) {
            binding.playPauseIcon.setImageResource(R.drawable.ic_play_arrow)
            isPauseIcon = false
            if (mediaPlayer.isPlaying)
                mediaPlayer.pause()
            createNotification(R.drawable.ic_play_arrow)
        } else {
            binding.playPauseIcon.setImageResource(R.drawable.ic_pause)
            isPauseIcon = true
            mediaPlayer.start()
            isVolumeOff()
            createNotification(R.drawable.ic_pause)
        }
    }

    override fun fastForwardClicked() {
        if (isPauseIcon)
            if (mediaPlayer.currentPosition + 30000 >= mediaPlayer.duration)
                mediaPlayer.seekTo(mediaPlayer.duration)
            else
                mediaPlayer.seekTo(mediaPlayer.currentPosition + 30000)
    }

    override fun onResume() {
        super.onResume()
        isBound = bindService(notifyIntent, connection, BIND_AUTO_CREATE)
    }


    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            notificationManager.cancelAll()
        if (isBound)
            unbindService(connection)
        unregisterReceiver(notificationReceiver)
    }
}