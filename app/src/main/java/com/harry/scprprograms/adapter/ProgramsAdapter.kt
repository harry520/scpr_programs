package com.harry.scprprograms.adapter

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.harry.scprprograms.databinding.ProgramsItemBinding
import com.harry.scprprograms.model.Program
import com.harry.scprprograms.view.ProgramClipsActivity

class ProgramsAdapter : RecyclerView.Adapter<ProgramsAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: ProgramsItemBinding) : RecyclerView.ViewHolder(binding.root)

    private lateinit var binding: ProgramsItemBinding
    private val differCallback = object : DiffUtil.ItemCallback<Program>() {
        override fun areItemsTheSame(oldItem: Program, newItem: Program) =
            oldItem.ArtworkUrl == newItem.ArtworkUrl

        override fun areContentsTheSame(oldItem: Program, newItem: Program) = oldItem == newItem
    }
    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ProgramsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.binding.root.context
        val program = differ.currentList[position]
        Glide.with(context).load(program.ArtworkUrl).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .into(holder.binding.programImg)
        holder.binding.root.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("programId", program.Id)
            bundle.putString("programArtworkUrl", program.ArtworkUrl)
            bundle.putString("programName", program.Name)
            bundle.putString("programDescription", program.Description)
            val intent = Intent(context, ProgramClipsActivity::class.java)
            intent.putExtras(bundle)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = differ.currentList.size
}