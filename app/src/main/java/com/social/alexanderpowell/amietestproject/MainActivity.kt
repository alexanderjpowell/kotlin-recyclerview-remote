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
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.MotionEventCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley

class MainActivity : AppCompatActivity(), CellClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var items: MutableList<ImageItem>
    private lateinit var downloadManager: DownloadManager
    private var lastDownload = -1L
    private lateinit var sharedPref: SharedPreferences// = this.getPreferences(Context.MODE_PRIVATE)
    private lateinit var favoritesSet: MutableSet<String>// = mutableSetOf<String>()
    private lateinit var mDetector: GestureDetectorCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mDetector = GestureDetectorCompat(this, MyGestureListener())
        //mDetector.setOnDoubleTapListener(this)

        sharedPref = this.getPreferences(Context.MODE_PRIVATE)
        favoritesSet = sharedPref.getStringSet("FAVORITE_IDS", setOf<String>())?:return

        downloadManager = (getSystemService(DOWNLOAD_SERVICE) as DownloadManager)

        //
        val queue = Volley.newRequestQueue(this)
        val url = "https://picsum.photos/v2/list"
        val stringRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                viewManager = LinearLayoutManager(this)

                items = mutableListOf()
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
                    items.add(imageItem)
                }

                viewAdapter = CustomAdapter(items, this)

                recyclerView = findViewById<RecyclerView>(R.id.my_recycler_view).apply {
                    layoutManager = viewManager
                    adapter = viewAdapter
                }
            },
            {
                //textView.text = "That didn't work!"
            })
        queue.add(stringRequest)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.favorites_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.show_all -> {
                if (item.isChecked) {
                    //Toast.makeText(baseContext, "checked", Toast.LENGTH_SHORT).show()
                    // Show all
                    //items.clear()
                    items.removeAt(0)
                    viewAdapter.notifyItemRemoved(0)
                } else {
                    //Toast.makeText(baseContext, "unchecked", Toast.LENGTH_SHORT).show()
                    // Show only favorites
                }
                item.isChecked = !item.isChecked
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //

    /*override fun onDown(event: MotionEvent): Boolean {
        Log.d("DEBUG_TAG", "onDown: $event")
        return true
    }

    override fun onFling(
        event1: MotionEvent,
        event2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        Log.d("DEBUG_TAG", "onFling: $event1 $event2")
        return true
    }

    override fun onLongPress(event: MotionEvent) {
        Log.d("DEBUG_TAG", "onLongPress: $event")
    }

    override fun onScroll(
        event1: MotionEvent,
        event2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        Log.d("DEBUG_TAG", "onScroll: $event1 $event2")
        return true
    }

    override fun onShowPress(event: MotionEvent) {
        Log.d("DEBUG_TAG", "onShowPress: $event")
    }

    override fun onSingleTapUp(event: MotionEvent): Boolean {
        Log.d("DEBUG_TAG", "onSingleTapUp: $event")
        return true
    }

    override fun onDoubleTap(event: MotionEvent): Boolean {
        Log.d("DEBUG_TAG", "onDoubleTap: $event")
        return true
    }

    override fun onDoubleTapEvent(event: MotionEvent): Boolean {
        Log.d("DEBUG_TAG", "onDoubleTapEvent: $event")
        return true
    }

    override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
        Log.d("DEBUG_TAG", "onSingleTapConfirmed: $event")
        return true
    }*/

    //

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

    override fun onCellLongClickListener(id: String, checked: Boolean) {
        with (sharedPref.edit()) {

            clear()
            if (favoritesSet.contains(id)) {
                favoritesSet.remove(id)
            } else {
                favoritesSet.add(id)
            }
            putStringSet("FAVORITE_IDS", favoritesSet)
            apply()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action: Int = MotionEventCompat.getActionMasked(event)
        mDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    private class MyGestureListener : GestureDetector.SimpleOnGestureListener() {

        override fun onDoubleTap(event: MotionEvent): Boolean {
            Log.d("DEBUG_TAG", "onDoubleTap: $event")
            return true
        }

    }

}

interface CellClickListener {
    fun onCellClickListener(downloadUrl: String)
    fun onCellLongClickListener(id: String, checked: Boolean)
}