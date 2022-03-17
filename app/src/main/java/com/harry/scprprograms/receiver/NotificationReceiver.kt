package com.harry.scprprograms.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.harry.scprprograms.service.ProgramClipsService
import com.harry.scprprograms.util.Constants.ACTION_FAST_FORWARD
import com.harry.scprprograms.util.Constants.ACTION_PLAY_PAUSE
import com.harry.scprprograms.util.Constants.ACTION_REWIND

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        p0?.sendBroadcast(Intent("PROGRAM_CLIP"))
        val intent = Intent(p0, ProgramClipsService::class.java)
        if (p1 != null && p1.action != null)
            when (p1.action) {
                ACTION_REWIND -> {
                    intent.putExtra("myActionName", p1.action)
                    p0?.startService(intent)
                }
                ACTION_PLAY_PAUSE -> {
                     intent.putExtra("myActionName", p1.action)
                    p0?.startService(intent)
                }
                ACTION_FAST_FORWARD -> {
                    intent.putExtra("myActionName", p1.action)
                    p0?.startService(intent)
                }
            }
    }
}