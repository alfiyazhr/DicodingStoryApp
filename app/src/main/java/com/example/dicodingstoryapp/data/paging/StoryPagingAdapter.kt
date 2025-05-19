package com.example.dicodingstoryapp.data.paging

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.paging.PagingDataAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.dicodingstoryapp.R
import com.example.dicodingstoryapp.data.model.Story
import com.example.dicodingstoryapp.databinding.ItemStoryBinding

class StoryPagingAdapter(private val onItemClick: (Story) -> Unit) :
    PagingDataAdapter<Story, StoryPagingAdapter.StoryViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<Story>() {
        override fun areItemsTheSame(oldItem: Story, newItem: Story): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Story, newItem: Story): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = getItem(position)
        holder.bind(story)
    }

    inner class StoryViewHolder(private val binding: ItemStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(story: Story?) {
            binding.tvItemName.text = ""
            binding.tvItemDescription.text = ""
            binding.ivItemPhoto.setImageDrawable(null)
            binding.progressBar.visibility = View.VISIBLE

            story?.let { safeStory ->
                binding.apply {
                    tvItemName.text = safeStory.name
                    tvItemDescription.text = safeStory.description

                    Glide.with(ivItemPhoto.context)
                        .load(safeStory.photoUrl)
                        .apply(RequestOptions().placeholder(R.drawable.progress_bar)) // Using resource ID
                        .error(R.drawable.progress_bar)
                        .into(ivItemPhoto)

                    ivItemPhoto.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                }

                itemView.setOnClickListener {
                    onItemClick(safeStory)
                }
            }
        }
    }
}
