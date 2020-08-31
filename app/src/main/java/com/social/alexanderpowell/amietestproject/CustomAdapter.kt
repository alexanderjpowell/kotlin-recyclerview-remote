package com.social.alexanderpowell.amietestproject

import android.animation.ObjectAnimator
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.text_row_item.view.*


class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val imageView: ImageView = itemView.image_view
    val authorText: TextView = itemView.author_text
    val dimensionsText: TextView = itemView.dimensions_text_view
    val urlText: TextView = itemView.url_text_view
    val downloadUrlText: TextView = itemView.download_url_text_view
    val expandableContent: LinearLayout = itemView.expandable_linear_layout
    val expandCollapseButton: ImageView = itemView.expand_collapse_button
    val clickableContentView: LinearLayout = itemView.clickable_linear_layout
    val downloadButton: MaterialButton = itemView.download_button
    //val materialCardView: MaterialCardView = itemView.material_card_view
    var rotationAngle: Float = 0f

    fun bind(imageItem: ImageItem) {
        //materialCardView.setChecked(imageItem.isFavorite)
        expandableContent.visibility = if (imageItem.expanded) View.VISIBLE else View.GONE
        Glide.with(imageView).load(imageItem.downloadUrl).placeholder(ColorDrawable(Color.GRAY)).into(imageView)
        authorText.text = imageItem.author
        dimensionsText.text = "Dimens: ${imageItem.width} x ${imageItem.height}"
        urlText.text = imageItem.url
        downloadUrlText.text = imageItem.downloadUrl
    }
}

class CustomAdapter(
    private val dataSet: MutableList<ImageItem>,
    private val cellClickListener: CellClickListener
) : RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.text_row_item, viewGroup, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val context = viewHolder.authorText.context
        val intent = Intent(Intent.ACTION_VIEW)

        val imageItem = dataSet[position]
        viewHolder.bind(imageItem)

        viewHolder.clickableContentView.setOnClickListener {
            val anim: ObjectAnimator = ObjectAnimator.ofFloat(
                viewHolder.expandCollapseButton,
                "rotation",
                viewHolder.rotationAngle,
                viewHolder.rotationAngle + 180
            )
            anim.setDuration(500)
            anim.start()
            viewHolder.rotationAngle += 180f
            viewHolder.rotationAngle = viewHolder.rotationAngle%360

            val expanded: Boolean = imageItem.expanded
            imageItem.expanded = !expanded
            notifyItemChanged(position, true)
        }

        viewHolder.clickableContentView.setOnLongClickListener {
            cellClickListener.onCellLongClickListener(
                imageItem.id
            )
            true
        }

        viewHolder.downloadButton.setOnClickListener {
            cellClickListener.onCellClickListener(imageItem.downloadUrl)
        }

        viewHolder.urlText.setOnClickListener {
            intent.data = Uri.parse(viewHolder.urlText.text.toString())
            context.startActivity(intent)
        }

        viewHolder.downloadUrlText.setOnClickListener {
            intent.data = Uri.parse(viewHolder.downloadUrlText.text.toString())
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = dataSet.size
}