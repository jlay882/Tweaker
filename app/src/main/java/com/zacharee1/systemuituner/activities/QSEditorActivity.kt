package com.zacharee1.systemuituner.activities

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zacharee1.systemuituner.R
import com.zacharee1.systemuituner.data.QSTileInfo
import com.zacharee1.systemuituner.util.SettingsType
import com.zacharee1.systemuituner.util.getSetting
import kotlinx.android.synthetic.main.activity_qs_editor.*
import kotlinx.android.synthetic.main.qs_tile.view.*
import java.util.*
import kotlin.collections.ArrayList

class QSEditorActivity : AppCompatActivity() {
    private val adapter by lazy { QSEditorAdapter(this) }

    private val touchHelperCallback = object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.DOWN or ItemTouchHelper.UP or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT, 0) {
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            adapter.move(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
            return true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_qs_editor)

        qs_list.adapter = adapter
        ItemTouchHelper(touchHelperCallback).attachToRecyclerView(qs_list)

        adapter.populateTiles()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_qs_editor, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.add -> {
                adapter.doAddTile()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    class QSEditorAdapter(private val context: Context) : RecyclerView.Adapter<QSEditorAdapter.QSVH>() {
        private val defaultTiles = ArrayList<String>().apply {
            try {
                val remRes = context.packageManager.getResourcesForApplication("com.android.systemui")
                val id = remRes.getIdentifier("quick_settings_tiles_default", "string", "com.android.systemui")

                val items = remRes.getString(id)

                addAll(items.split(","))
            } catch (e: Exception) {}
        }

        val currentTiles = ArrayList<QSTileInfo>()
        private val availableTiles = ArrayList<String>()

        fun populateTiles() {
            currentTiles.clear()

            val tiles = context.getSetting(SettingsType.SECURE, "sysui_qs_tiles")

            if (tiles.isNullOrBlank()) currentTiles.addAll(defaultTiles.map { QSTileInfo(it) })
            else {
                currentTiles.addAll(
                    tiles.split(",").map { QSTileInfo(it) }
                )
            }

            updateAvailableTiles()
        }

        fun move(from: Int, to: Int) {
            if (from < to) {
                for (i in from until to) {
                    Collections.swap(currentTiles, i, i + 1)
                }
            } else {
                for (i in from downTo to + 1) {
                    Collections.swap(currentTiles, i, i - 1)
                }
            }
            notifyItemMoved(from, to)
        }

        fun doAddTile() {

        }

        private fun addTile(tile: QSTileInfo) {
            currentTiles.add(tile)
            notifyItemInserted(currentTiles.lastIndex)

            updateAvailableTiles()
        }

        private fun removeTile(position: Int) {
            currentTiles.removeAt(position)
            notifyItemRemoved(position)

            updateAvailableTiles()
        }

        private fun updateAvailableTiles() {
            availableTiles.clear()

            availableTiles.addAll(defaultTiles.filterNot {
                currentTiles.map { tile -> tile.key }.contains(it)
            })
        }

        override fun getItemCount(): Int {
            return currentTiles.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QSVH {
            return QSVH(
                LayoutInflater.from(parent.context).inflate(R.layout.qs_tile, parent, false)
            )
        }

        @ExperimentalStdlibApi
        override fun onBindViewHolder(holder: QSVH, position: Int) {
            holder.onBind(currentTiles[position])
        }

        inner class QSVH(view: View) : RecyclerView.ViewHolder(view) {
            init {
                itemView.setOnLongClickListener {
                    itemView.remove.apply { isVisible = !isVisible }
                    true
                }

                itemView.remove.setOnClickListener {
                    val newPos = bindingAdapterPosition

                    if (newPos != -1) {
                        removeTile(newPos)
                    }
                }
            }

            @ExperimentalStdlibApi
            fun onBind(info: QSTileInfo) {
                itemView.icon.setImageDrawable(info.getIcon(itemView.context))
                itemView.label.text = info.getLabel(itemView.context)
            }
        }
    }

    class AddDialog(context: Context, adapter: QSEditorAdapter) : MaterialAlertDialogBuilder(context) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_add_qs_tile, null)

        init {

        }
    }
}