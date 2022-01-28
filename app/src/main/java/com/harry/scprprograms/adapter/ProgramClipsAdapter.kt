package com.harry.scprprograms.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.harry.scprprograms.databinding.ProgramClipsItemBinding
import com.harry.scprprograms.model.Clip
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class ProgramClipsAdapter(private val listener: RecyclerItemClickListener) :
    RecyclerView.Adapter<ProgramClipsAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: ProgramClipsItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    private lateinit var binding: ProgramClipsItemBinding
    private val differCallback = object : DiffUtil.ItemCallback<Clip>() {
        override fun areItemsTheSame(oldItem: Clip, newItem: Clip) =
            oldItem.Description == newItem.Description

        override fun areContentsTheSame(oldItem: Clip, newItem: Clip) = oldItem == newItem
    }
    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding =
            ProgramClipsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    private fun convertUtcToLocal(utcTime: String?): String {
        var time = ""
        if (utcTime != null) {
            val utcFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            utcFormatter.timeZone = TimeZone.getTimeZone("UTC")
            var gpsUTCDate: Date? = null
            try {
                gpsUTCDate = utcFormatter.parse(utcTime)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            val localFormatter = SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa", Locale.getDefault())
            localFormatter.timeZone = TimeZone.getDefault()
            assert(gpsUTCDate != null)
            time = localFormatter.format(gpsUTCDate!!.time)
        }
        return time
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val clip = differ.currentList[position]
        holder.binding.clipDetails.text =  clip.Description
        holder.binding.clipPublishedTime.text = convertUtcToLocal(clip.PublishedUtc)
        holder.binding.root.setOnClickListener {
            listener.onClickListener(clip.AudioUrl)
        }
    }

    override fun getItemCount() = differ.currentList.size

    interface RecyclerItemClickListener {
        fun onClickListener(audioUrl: String)
    }
}