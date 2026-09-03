package org.ggram.ui.diff

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.ggram.R
import org.ggram.antirecall.GgramAntiRecallManager
import org.ggram.antirecall.MessageEditRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * GgramDiffActivity - Displays full edit history and text revisions for a selected message.
 */
class GgramDiffActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ggram_diff)

        val toolbar = findViewById<Toolbar>(R.id.diff_toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val messageId = intent.getLongExtra("extra_message_id", 0L)
        val revisions = GgramAntiRecallManager.getEditHistory(messageId)

        val recyclerView = findViewById<RecyclerView>(R.id.diff_recycler)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = DiffAdapter(revisions)
    }

    class DiffAdapter(private val items: List<MessageEditRecord>) :
        RecyclerView.Adapter<DiffAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvRevision: TextView = view.findViewById(android.R.id.text1)
            val tvDetails: TextView = view.findViewById(android.R.id.text2)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_2, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            val sdf = SimpleDateFormat("HH:mm:ss dd.MM.yyyy", Locale.getDefault())
            val dateStr = sdf.format(Date(item.timestamp))

            holder.tvRevision.text = "Revision #${item.revisionId} • $dateStr"
            holder.tvRevision.setTextColor(0xFF01BA53.toInt())

            holder.tvDetails.text = "Before: \"${item.previousText}\"\nAfter: \"${item.newText}\""
            holder.tvDetails.setTextColor(0xFFFFFFFF.toInt())
        }

        override fun getItemCount(): Int = items.size
    }
}
