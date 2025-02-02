package com.example.week1_0627.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.week1_0627.R

class ImageAdapter(
    private val images: MutableList<String>,
    private val onFavoriteClick: (String) -> Unit
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imagePath = images[position]
        Glide.with(holder.itemView.context)
            .load("file:///android_asset/$imagePath")
            .into(holder.imageView)

        holder.favoriteButton.setOnClickListener {
            onFavoriteClick(imagePath)
        }
    }

    override fun getItemCount(): Int = images.size

    fun removeImage(imagePath: String) {
        val position = images.indexOf(imagePath)
        if (position >= 0) {
            images.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_view)
        val favoriteButton: ImageButton = itemView.findViewById(R.id.button_favorite_image)
    }
}
