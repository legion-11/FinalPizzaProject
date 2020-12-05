package com.dmytroandriichuk.finallpizzaproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class ViewPagerAdapter(private val imagesId: IntArray) : RecyclerView.Adapter<PagerVH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerVH =
        PagerVH(LayoutInflater.from(parent.context).inflate(R.layout.item_image_page, parent, false))

    override fun getItemCount(): Int = imagesId.size

    override fun onBindViewHolder(holder: PagerVH, position: Int) = holder.itemView.run {
        val pizzaImage = findViewById<ImageView>(R.id.pizzaImage)
        pizzaImage.setImageResource(imagesId[position])
    }
}

class PagerVH(itemView: View) : RecyclerView.ViewHolder(itemView)