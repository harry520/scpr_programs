package com.harry.scprprograms.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import com.harry.scprprograms.`interface`.ActionPlaying
import com.harry.scprprograms.util.Constants.ACTION_FAST_FORWARD
import com.harry.scprprograms.util.Constants.ACTION_PLAY_PAUSE
import com.harry.scprprograms.util.Constants.ACTION_REWIND


class ProgramClipsService : Service() {
    inner class MyBinder : Binder() {
        fun getService() = this@ProgramClipsService
    }

    private val mBinder = MyBinder()
    private var actionPlaying: ActionPlaying? = null

    override fun onBind(p0: Intent?) = mBinder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val actionName = intent?.getStringExtra("myActionName")
        if (actionName != null)
            when (actionName) {
                ACTION_REWIND -> actionPlaying?.rewindClicked()
                ACTION_PLAY_PAUSE -> actionPlaying?.playPauseClicked()
                ACTION_FAST_FORWARD -> actionPlaying?.fastForwardClicked()
            }
        return START_STICKY
    }

    fun setCallback(actionPlaying: ActionPlaying) {
        this.actionPlaying = actionPlaying
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
    }
}