package com.example.termtwoproject.DrawingListDetailActivity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.NavUtils
import android.view.MenuItem
import androidx.room.Room
import com.example.termtwoproject.Database.Drawing
import com.example.termtwoproject.DrawSettingsActivity
import com.example.termtwoproject.Database.DrawingsDatabase
import com.example.termtwoproject.R

import kotlinx.android.synthetic.main.activity_drawing_list.*
import kotlinx.android.synthetic.main.drawing_list_content.view.*
import kotlinx.android.synthetic.main.drawing_list.*

/**
 * An activity representing a list of Pings. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [DrawingDetailActivity] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
class DrawingListActivity : AppCompatActivity() {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private var twoPane: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawing_list)

        setSupportActionBar(toolbar)
        toolbar.title = title

        // Making a new drawing
        fab.setOnClickListener { startActivity(Intent(applicationContext, DrawSettingsActivity::class.java)) }

        // Show the Up button in the action bar.
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (drawing_detail_container != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            twoPane = true
        }

        val database = Room.databaseBuilder(applicationContext, DrawingsDatabase::class.java, "drawings").allowMainThreadQueries().build()
        setupRecyclerView(drawing_list, database)
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            android.R.id.home -> {
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                NavUtils.navigateUpFromSameTask(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    private fun setupRecyclerView(recyclerView: RecyclerView, database: DrawingsDatabase) {
        recyclerView.adapter =
            SimpleItemRecyclerViewAdapter(
                this,
                database.drawingDao().getAll(),
                twoPane
            )
    }

    class SimpleItemRecyclerViewAdapter(
        private val parentActivity: DrawingListActivity,
        private val values: List<Drawing>,
        private val twoPane: Boolean
    ) : RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

        private val onClickListener: View.OnClickListener

        init {
            onClickListener = View.OnClickListener { v ->
                val item = v.tag as Drawing
                if (twoPane) {
                    val fragment = DrawingDetailFragment().apply {
                        arguments = Bundle().apply {
                            putString(DrawingDetailFragment.ARG_ITEM_ID, item.id.toString())
                        }
                    }
                    parentActivity.supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.drawing_detail_container, fragment)
                        .commit()
                } else {
                    val intent = Intent(v.context, DrawingDetailActivity::class.java).apply {
                        putExtra(DrawingDetailFragment.ARG_ITEM_ID, item.id.toString())
                    }
                    v.context.startActivity(intent)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.drawing_list_content, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = values[position]
            holder.idView.text = item.id.toString()
            holder.contentView.text = item.title

            with(holder.itemView) {
                tag = item
                setOnClickListener(onClickListener)
            }
        }

        override fun getItemCount() = values.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val idView: TextView = view.id_text
            val contentView: TextView = view.content
        }
    }
}
