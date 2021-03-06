package com.maptionary.application.DrawingListDetailActivity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import androidx.room.Room
import com.maptionary.application.Database.DbWorkerThread
import com.maptionary.application.Database.DrawingsDatabase
import com.maptionary.application.DrawActivity
import com.maptionary.application.R
import kotlinx.android.synthetic.main.activity_drawing_detail.*

/**
 * An activity representing a single Drawing detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a [DrawingListActivity].
 */
class DrawingDetailActivity : AppCompatActivity() {

    lateinit var thread: DbWorkerThread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawing_detail)
        setSupportActionBar(detail_toolbar)

        // MultiThreading
        thread = DbWorkerThread("dbWorkerThread")
        thread.start()

        // Database Access
        val database = Room.databaseBuilder(applicationContext, DrawingsDatabase::class.java, "drawings")
            .fallbackToDestructiveMigration()
            .build()

        // Start correct drawing
        toDrawing.setOnClickListener {
            val id = intent.getStringExtra(DrawingDetailFragment.ARG_ITEM_ID)
            val task = Runnable {
                database.drawingDao().getDrawingById(id.toLong())
                val intent = Intent(this, DrawActivity::class.java)
                intent.putExtra("drawingID", id.toLong())
                startActivity(intent)
            }

            thread.postTask(task)
            //TODO value needs to be the name of the selected drawing - that way we know what drawing we are editing
            intent.putExtra("drawingName", "test")
            startActivity(intent)
        }

        // Show the Up button in the action bar.
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            val fragment = DrawingDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(
                        DrawingDetailFragment.ARG_ITEM_ID,
                        intent.getStringExtra(DrawingDetailFragment.ARG_ITEM_ID)
                    )
                }
            }

            supportFragmentManager.beginTransaction()
                .add(R.id.drawing_detail_container, fragment)
                .commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            android.R.id.home -> {
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back

                navigateUpTo(Intent(this, DrawingListActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
}
