package com.example.myapplication


import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemPostBinding
import com.squareup.picasso.Picasso

class PostAdapter(
    private var posts: List<PostData>,
    private val onPostLongClick: (PostData) -> Unit,
    private val onPostClick: (PostData) -> Unit

) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    inner class PostViewHolder(private val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: PostData) {
            binding.textViewPrice.text = post.price
            binding.textViewPlace.text = post.place
            binding.textViewDescription.text = post.description
            binding.textViewContactNumber.text = post.contactNumber
            Picasso.get().load(post.photoDownloadUrl).into(binding.imageViewPhoto)


            binding.root.setOnClickListener {
                onPostClick(post)
            }

            // Set OnLongClickListener for the item view
            binding.root.setOnLongClickListener {
                onPostLongClick(post)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount(): Int = posts.size

    fun updateList(newPosts: List<PostData>) {
        posts = newPosts
        notifyDataSetChanged()
    }
}