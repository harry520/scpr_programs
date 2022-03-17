package com.harry.scprprograms.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.harry.scprprograms.databinding.ProgramClipsHeaderBinding
import com.harry.scprprograms.databinding.ProgramClipsItemBinding
import com.harry.scprprograms.model.Clip
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class ProgramClipsAdapter(
    private val programArtworkUrl: String?,
    private val programDescription: String?,
    private val listener: RecyclerItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    interface RecyclerItemClickListener {
        fun onClickListener(audioUrl: String)
    }

    inner class HeaderViewHolder(val binding: ProgramClipsHeaderBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class ItemViewHolder(val binding: ProgramClipsItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    private lateinit var headerBinding: ProgramClipsHeaderBinding
    private lateinit var itemBinding: ProgramClipsItemBinding
    private val differCallback = object : DiffUtil.ItemCallback<Clip>() {
        override fun areItemsTheSame(oldItem: Clip, newItem: Clip) =
            oldItem.Description == newItem.Description

        override fun areContentsTheSame(oldItem: Clip, newItem: Clip) = oldItem == newItem
    }
    val differ = AsyncListDiffer(this, differCallback)

    override fun getItemViewType(position: Int): Int {
        if (position == 0)
            return TYPE_HEADER
        return TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        if (viewType == TYPE_HEADER) {
            headerBinding = ProgramClipsHeaderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            HeaderViewHolder(headerBinding)
        } else {
            itemBinding =
                ProgramClipsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ItemViewHolder(itemBinding)
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            val context = holder.binding.root.context
            Glide.with(context).load(programArtworkUrl)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(holder.binding.programArtwork)
            holder.binding.programDescription.text = programDescription
        }
        if (holder is ItemViewHolder) {
            val clip = differ.currentList[position - 1]
            holder.binding.clipDetails.text = clip.Description
            holder.binding.clipPublishedTime.text = convertUtcToLocal(clip.PublishedUtc)
            holder.binding.root.setOnClickListener {
                listener.onClickListener(clip.AudioUrl)
            }
        }
    }

    override fun getItemCount() = differ.currentList.size + 1

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }
}