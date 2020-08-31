package com.social.alexanderpowell.amietestproject

import android.app.DownloadManager
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), CellClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var recyclerItems: MutableList<ImageItem> = mutableListOf()
    private var all: MutableList<ImageItem> = mutableListOf()
    private var favorites: MutableList<ImageItem> = mutableListOf()
    private lateinit var downloadManager: DownloadManager
    private var lastDownload = -1L
    private lateinit var sharedPref: SharedPreferences
    private lateinit var favoritesSet: MutableSet<String>
    private lateinit var switchMaterial: SwitchMaterial
    private var showingFavorites: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        switchMaterial = favorites_switch
        sharedPref = this.getPreferences(Context.MODE_PRIVATE)
        favoritesSet = sharedPref.getStringSet("FAVORITE_IDS", mutableSetOf<String>())?:return
        downloadManager = (getSystemService(DOWNLOAD_SERVICE) as DownloadManager)

        val queue = Volley.newRequestQueue(this)
        val url = "https://picsum.photos/v2/list"
        val stringRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                viewManager = LinearLayoutManager(this)

                for (i in 0 until response.length()) {
                    val imageItem = ImageItem(
                        response.getJSONObject(i).getString("id"),
                        response.getJSONObject(i).getString("author"),
                        response.getJSONObject(i).getInt("width"),
                        response.getJSONObject(i).getInt("height"),
                        response.getJSONObject(i).getString("url"),
                        response.getJSONObject(i).getString("download_url"),
                        favoritesSet.contains(response.getJSONObject(i).getString("id"))
                    )
                    all.add(imageItem)
                }

                recyclerItems.addAll(all)

                viewAdapter = CustomAdapter(recyclerItems, this)

                recyclerView = findViewById<RecyclerView>(R.id.my_recycler_view).apply {
                    layoutManager = viewManager
                    adapter = viewAdapter
                }

                // Populate Favorites List
                for (i in 0 until all.size) {
                    if (favoritesSet.contains(all[i].id)) {
                        favorites.add(all[i])
                    }
                }
            },
            {
                Log.d("MainActivity.kt", "That didn't work!")
            })
        queue.add(stringRequest)

        switchMaterial.setOnCheckedChangeListener { _, isChecked ->
            showingFavorites = !showingFavorites
            recyclerItems.clear()
            if (isChecked) { // Show only favorites
                recyclerItems.addAll(favorites)
            } else { // Show entire list
                recyclerItems.addAll(all)
            }
            viewAdapter.notifyDataSetChanged()
            toggleEmptyStateMessage()
        }

    }

    override fun onCellClickListener(downloadUrl: String) {
        val uri: Uri = Uri.parse(downloadUrl)
        Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            .mkdirs()

        lastDownload = downloadManager.enqueue(
            DownloadManager.Request(uri)
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
                .setAllowedOverRoaming(false)
                .setTitle("Image download manager")
                .setDescription("This saves the selected image to the downloads directory in external storage")
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "image.jpg"
                )
        )
        Toast.makeText(applicationContext, "Download started and will run in background", Toast.LENGTH_LONG).show()
    }

    override fun onCellLongClickListener(id: String) {
        with (sharedPref.edit()) {
            clear()
            if (showingFavorites) {
                Toast.makeText(baseContext, "Item removed from favorites", Toast.LENGTH_SHORT).show()
                favoritesSet.remove(id)
                val index: Int? = removeImageItemFromList(id)
                if (index != null) {
                    recyclerItems.clear()
                    recyclerItems.addAll(favorites)
                    viewAdapter.notifyItemRemoved(index)
                    viewAdapter.notifyItemRangeChanged(0, favorites.size)
                }
            } else {
                Toast.makeText(baseContext, "Item added to favorites", Toast.LENGTH_SHORT).show()
                val newFavorite: Boolean = favoritesSet.add(id)
                if (newFavorite) getImageItemFromId(id)?.let { favorites.add(it) }
            }
            putStringSet("FAVORITE_IDS", favoritesSet)
            apply()
        }

        toggleEmptyStateMessage()
    }

    private fun removeImageItemFromList(id: String) : Int? {
        for (i in 0 until favorites.size) {
            if (favorites[i].id == id) {
                favorites.removeAt(i)
                return i
            }
        }
        return null
    }

    private fun getImageItemFromId(id: String) : ImageItem? {
        for (i in 0 until all.size) {
            if (all[i].id == id) {
                return all[i]
            }
        }
        return null
    }

    private fun toggleEmptyStateMessage() {
        if (showingFavorites && favorites.size == 0) {
            empty_state_text_view.visibility = View.VISIBLE
        } else {
            empty_state_text_view.visibility = View.GONE
        }
    }

}

interface CellClickListener {
    fun onCellClickListener(downloadUrl: String)
    fun onCellLongClickListener(id: String)
}