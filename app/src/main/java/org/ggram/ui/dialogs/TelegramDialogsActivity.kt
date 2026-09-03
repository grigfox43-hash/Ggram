package org.ggram.ui.dialogs

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import org.ggram.R
import org.ggram.config.GgramConfig
import org.ggram.messenger.TelegramEngine
import org.ggram.messenger.model.TelegramChat
import org.ggram.tabs.SmartTabType
import org.ggram.ui.chat.TelegramChatActivity
import org.ggram.ui.preferences.GgramPreferencesActivity

/**
 * TelegramDialogsActivity - Main chat list screen of Ggram.
 * Displays smart tabs, chat list, drawer menu, and connects to Ggram preferences.
 */
class TelegramDialogsActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerDialogs: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var adapter: DialogsAdapter

    private var currentTab: SmartTabType = SmartTabType.ALL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_telegram_dialogs)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        tabLayout = findViewById(R.id.tab_layout)
        recyclerDialogs = findViewById(R.id.recycler_dialogs)
        swipeRefresh = findViewById(R.id.swipe_refresh)

        setupDrawer()
        setupToolbar()
        setupTabs()
        setupRecycler()
    }

    override fun onResume() {
        super.onResume()
        refreshChats()
    }

    private fun setupToolbar() {
        findViewById<ImageView>(R.id.btn_open_drawer).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        findViewById<ImageView>(R.id.btn_mark_all_read).setOnClickListener {
            TelegramEngine.markAllAsRead()
            refreshChats()
            showToast("Все чаты помечены прочитанными")
        }

        findViewById<ImageView>(R.id.btn_search).setOnClickListener {
            showToast("Поиск по сообщениям и контактам")
        }

        findViewById<View>(R.id.fab_new_message).setOnClickListener {
            showToast("Создать новый диалог")
        }
    }

    private fun setupDrawer() {
        val user = TelegramEngine.currentUser
        val header = navigationView.getHeaderView(0)
        if (user != null && header != null) {
            header.findViewById<TextView>(R.id.drawer_name)?.text = user.fullName
            header.findViewById<TextView>(R.id.drawer_phone)?.text = "${user.phone} • @${user.username ?: "user"}"
        }

        navigationView.setNavigationItemSelectedListener { item ->
            drawerLayout.closeDrawer(GravityCompat.START)
            when (item.itemId) {
                R.id.nav_ggram_settings -> {
                    startActivity(Intent(this, GgramPreferencesActivity::class.java))
                    true
                }
                R.id.nav_saved -> {
                    openChat(1004L)
                    true
                }
                R.id.nav_github -> {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/grigfox43-hash/Ggram")))
                    true
                }
                else -> {
                    showToast(item.title.toString())
                    true
                }
            }
        }
    }

    private fun setupTabs() {
        tabLayout.removeAllTabs()
        SmartTabType.values().forEach { tab ->
            val tabView = tabLayout.newTab().setText(tab.titleRu)
            tabLayout.addTab(tabView)
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val index = tab?.position ?: 0
                currentTab = SmartTabType.values().getOrElse(index) { SmartTabType.ALL }
                refreshChats()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRecycler() {
        recyclerDialogs.layoutManager = LinearLayoutManager(this)
        adapter = DialogsAdapter(emptyList()) { chat ->
            openChat(chat.id)
        }
        recyclerDialogs.adapter = adapter

        swipeRefresh.setColorSchemeColors(0xFF01BA53.toInt())
        swipeRefresh.setOnRefreshListener {
            refreshChats()
            swipeRefresh.isRefreshing = false
        }
    }

    private fun refreshChats() {
        val chats = TelegramEngine.getChats(currentTab)
        adapter.update(chats)
    }

    private fun openChat(chatId: Long) {
        val intent = Intent(this, TelegramChatActivity::class.java).apply {
            putExtra("extra_chat_id", chatId)
        }
        startActivity(intent)
    }

    private fun showToast(text: String) {
        val view = findViewById<View>(android.R.id.content)
        Snackbar.make(view, text, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(0xFF01BA53.toInt())
            .setTextColor(0xFFFFFFFF.toInt())
            .show()
    }

    // --- DIALOGS ADAPTER ---
    class DialogsAdapter(
        private var items: List<TelegramChat>,
        private val onClick: (TelegramChat) -> Unit
    ) : RecyclerView.Adapter<DialogsAdapter.ViewHolder>() {

        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val tvAvatar: TextView = v.findViewById(R.id.dialog_avatar_text)
            val tvTitle: TextView = v.findViewById(R.id.dialog_title)
            val tvTime: TextView = v.findViewById(R.id.dialog_time)
            val tvLastMessage: TextView = v.findViewById(R.id.dialog_last_message)
            val tvUnreadBadge: TextView = v.findViewById(R.id.dialog_unread_badge)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_telegram_dialog, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val chat = items[position]
            holder.tvAvatar.text = chat.avatarInitials
            holder.tvTitle.text = chat.title
            holder.tvTime.text = chat.lastMessageTime
            holder.tvLastMessage.text = chat.lastMessage

            if (chat.unreadCount > 0) {
                holder.tvUnreadBadge.visibility = View.VISIBLE
                holder.tvUnreadBadge.text = chat.unreadCount.toString()
            } else {
                holder.tvUnreadBadge.visibility = View.GONE
            }

            holder.itemView.setOnClickListener { onClick(chat) }
        }

        override fun getItemCount(): Int = items.size

        fun update(newItems: List<TelegramChat>) {
            items = newItems
            notifyDataSetChanged()
        }
    }
}
