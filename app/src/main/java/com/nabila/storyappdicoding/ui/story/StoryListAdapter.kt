package com.nabila.storyappdicoding.ui.story

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nabila.storyappdicoding.data.model.Story
import com.nabila.storyappdicoding.databinding.ItemStoryBinding
import com.nabila.storyappdicoding.ui.detailstory.StoryDetailActivity
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import androidx.core.util.Pair
import org.joda.time.DateTimeZone

class StoryListAdapter : ListAdapter<Story, StoryListAdapter.StoryViewHolder>(DIFF_CALLBACK) {

    class StoryViewHolder(private val binding: ItemStoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(story: Story) {
            binding.tvItemName.text = story.name
            binding.tvItemDescription.text = story.description
            binding.tvItemCreatedAt.text = formatCreatedAt(story.createdAt)
            Glide.with(itemView.context)
                .asBitmap()
                .load(story.photoUrl)
                .into(binding.imgItemPhoto)
        }

        fun getBinding(): ItemStoryBinding {
            return binding
        }

        private fun formatCreatedAt(createdAt: String): String {
            val dateTime = DateTime.parse(createdAt).withZone(DateTimeZone.getDefault())

            val formatter = DateTimeFormat.forPattern("dd MMM yyyy HH:mm")
            return formatter.print(dateTime)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = getItem(position)
        holder.bind(story)
        Log.d(TAG, "Binding story: $story")

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, StoryDetailActivity::class.java)
            intent.putExtra(StoryDetailActivity.EXTRA_STORY, story)

            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                holder.itemView.context as Activity,
                Pair.create(holder.getBinding().imgItemPhoto, "image_transition"),
            )

            holder.itemView.context.startActivity(intent, options.toBundle())
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Story>() {
            override fun areItemsTheSame(oldItem: Story, newItem: Story): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Story, newItem: Story): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}