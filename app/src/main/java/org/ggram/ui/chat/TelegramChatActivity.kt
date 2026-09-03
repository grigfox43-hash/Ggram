package org.ggram.ui.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import org.ggram.R
import org.ggram.chat.GgramChatErgonomics
import org.ggram.config.GgramConfig
import org.ggram.messenger.TelegramEngine
import org.ggram.messenger.model.TelegramChat
import org.ggram.messenger.model.TelegramMessage
import org.ggram.profiler.GgramUserProfiler
import org.ggram.ui.diff.GgramDiffActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * TelegramChatActivity - Full Telegram conversation screen.
 * Supports incoming/outgoing messages, Ggram Anti-Recall indicators,
 * User Profiler inspector, and message action menus.
 */
class TelegramChatActivity : AppCompatActivity() {

    private var chatId: Long = 0L
    private var chat: TelegramChat? = null
    private lateinit var recyclerMessages: RecyclerView
    private lateinit var etMessageInput: EditText
    private lateinit var adapter: MessagesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_telegram_chat)

        chatId = intent.getLongExtra("extra_chat_id", 0L)
        chat = TelegramEngine.getChat(chatId)

        setupToolbar()
        setupRecycler()
        setupInput()
    }

    private fun setupToolbar() {
        findViewById<ImageView>(R.id.btn_chat_back).setOnClickListener { finish() }

        val currentChat = chat
        if (currentChat != null) {
            findViewById<TextView>(R.id.tv_chat_title).text = currentChat.title
            findViewById<TextView>(R.id.chat_avatar_text).text = currentChat.avatarInitials
        }

        // Profiler Inspector Button
        findViewById<ImageView>(R.id.btn_user_profiler).setOnClickListener {
            showProfilerDialog()
        }

        findViewById<View>(R.id.layout_chat_title_info).setOnClickListener {
            showProfilerDialog()
        }

        // Jump to First Message
        findViewById<ImageView>(R.id.btn_jump_first).setOnClickListener {
            if (adapter.itemCount > 0) {
                recyclerMessages.smoothScrollToPosition(0)
                showToast("Переход в самое начало переписки")
            }
        }
    }

    private fun setupRecycler() {
        recyclerMessages = findViewById(R.id.recycler_messages)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        recyclerMessages.layoutManager = layoutManager

        val messages = TelegramEngine.getMessages(chatId)
        adapter = MessagesAdapter(messages.toMutableList(),
            onMessageLongClick = { msg -> showMessageOptions(msg) },
            onEditedClick = { msg ->
                val intent = Intent(this, GgramDiffActivity::class.java).apply {
                    putExtra("extra_message_id", msg.id)
                }
                startActivity(intent)
            }
        )
        recyclerMessages.adapter = adapter
    }

    private fun setupInput() {
        etMessageInput = findViewById(R.id.et_message_input)

        findViewById<View>(R.id.btn_send_message).setOnClickListener {
            val text = etMessageInput.text.toString().trim()
            if (text.isNotEmpty()) {
                val sentMsg = TelegramEngine.sendMessage(chatId, text)
                adapter.addMessage(sentMsg)
                recyclerMessages.scrollToPosition(adapter.itemCount - 1)
                etMessageInput.setText("")
            }
        }

        findViewById<ImageView>(R.id.btn_voice_record).setOnClickListener {
            if (GgramConfig.isConfirmVoice) {
                AlertDialog.Builder(this)
                    .setTitle("Подтверждение отправки")
                    .setMessage("Вы действительно хотите записать и отправить голосовое сообщение?")
                    .setPositiveButton("Отправить") { _, _ ->
                        val voiceMsg = TelegramEngine.sendMessage(chatId, "🎤 Голосовое сообщение (0:14)")
                        adapter.addMessage(voiceMsg)
                        recyclerMessages.scrollToPosition(adapter.itemCount - 1)
                    }
                    .setNegativeButton("Отмена", null)
                    .show()
            } else {
                val voiceMsg = TelegramEngine.sendMessage(chatId, "🎤 Голосовое сообщение (0:14)")
                adapter.addMessage(voiceMsg)
                recyclerMessages.scrollToPosition(adapter.itemCount - 1)
            }
        }

        findViewById<ImageView>(R.id.btn_attach).setOnClickListener {
            showToast("Выбор медиафайлов для отправки")
        }
    }

    private fun showProfilerDialog() {
        val partnerId = chat?.partnerUserId ?: 100000001L
        val regEstimate = GgramUserProfiler.estimateRegistrationDate(partnerId)
        val isMutual = GgramUserProfiler.isMutualContact(partnerId)

        val message = """
            👤 Собеседник: ${chat?.title ?: "Пользователь"}
            🆔 Telegram ID: $partnerId
            📅 Регистрация аккаунта: $regEstimate
            📱 Взаимный контакт: ${if (isMutual) "Да (номер сохранен в контактах)" else "Нет"}
            🛡️ AdBlock статус: Активен (реклама удаляется)
            👻 Ghost режим: Активен
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("🔍 Шпионский инспектор Ggram")
            .setMessage(message)
            .setPositiveButton("Закрыть", null)
            .show()
    }

    private fun showMessageOptions(msg: TelegramMessage) {
        val options = arrayOf(
            "Копировать текст",
            "Копировать фрагмент текста",
            "Переслать без автора (1 клик)",
            "История изменений (Diff)"
        )

        AlertDialog.Builder(this)
            .setTitle("Действия над сообщением")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Telegram message", msg.text))
                        showToast("Текст скопирован в буфер")
                    }
                    1 -> {
                        val snippet = GgramChatErgonomics.copyTextSnippet(this, msg.text, 0, minOf(30, msg.text.length))
                        showToast("Скопирован фрагмент: '$snippet'")
                    }
                    2 -> {
                        val cleanText = GgramChatErgonomics.stripAuthorQuote(msg.text)
                        val forwarded = TelegramEngine.sendMessage(chatId, cleanText)
                        adapter.addMessage(forwarded)
                        recyclerMessages.scrollToPosition(adapter.itemCount - 1)
                        showToast("Переслано без указания автора")
                    }
                    3 -> {
                        val intent = Intent(this, GgramDiffActivity::class.java).apply {
                            putExtra("extra_message_id", msg.id)
                        }
                        startActivity(intent)
                    }
                }
            }
            .show()
    }

    private fun showToast(text: String) {
        val view = findViewById<View>(android.R.id.content)
        Snackbar.make(view, text, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(0xFF01BA53.toInt())
            .setTextColor(0xFFFFFFFF.toInt())
            .show()
    }

    // --- MESSAGES ADAPTER ---
    class MessagesAdapter(
        private val items: MutableList<TelegramMessage>,
        private val onMessageLongClick: (TelegramMessage) -> Unit,
        private val onEditedClick: (TelegramMessage) -> Unit
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
            private const val TYPE_INCOMING = 1
            private const val TYPE_OUTGOING = 2
        }

        override fun getItemViewType(position: Int): Int {
            return if (items[position].isOutgoing) TYPE_OUTGOING else TYPE_INCOMING
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return if (viewType == TYPE_OUTGOING) {
                val v = inflater.inflate(R.layout.item_telegram_message_out, parent, false)
                OutgoingViewHolder(v)
            } else {
                val v = inflater.inflate(R.layout.item_telegram_message_in, parent, false)
                IncomingViewHolder(v)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val msg = items[position]
            val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp))

            if (holder is OutgoingViewHolder) {
                holder.tvText.text = msg.text
                holder.tvTime.text = timeStr
                holder.tvEdited.visibility = if (msg.isEdited) View.VISIBLE else View.GONE
                holder.tvEdited.setOnClickListener { onEditedClick(msg) }
                holder.itemView.setOnLongClickListener {
                    onMessageLongClick(msg)
                    true
                }
            } else if (holder is IncomingViewHolder) {
                holder.tvSender.text = msg.senderName
                holder.tvText.text = msg.text
                holder.tvTime.text = timeStr
                holder.tvDeleted.visibility = if (msg.isDeleted) View.VISIBLE else View.GONE
                holder.tvEdited.visibility = if (msg.isEdited) View.VISIBLE else View.GONE
                holder.tvEdited.setOnClickListener { onEditedClick(msg) }
                holder.itemView.setOnLongClickListener {
                    onMessageLongClick(msg)
                    true
                }
            }
        }

        override fun getItemCount(): Int = items.size

        fun addMessage(msg: TelegramMessage) {
            items.add(msg)
            notifyItemInserted(items.size - 1)
        }

        class OutgoingViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val tvText: TextView = v.findViewById(R.id.msg_text)
            val tvTime: TextView = v.findViewById(R.id.msg_time)
            val tvEdited: TextView = v.findViewById(R.id.msg_edited_badge)
            val imgStatus: ImageView = v.findViewById(R.id.msg_read_status)
        }

        class IncomingViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val tvSender: TextView = v.findViewById(R.id.msg_sender_name)
            val tvText: TextView = v.findViewById(R.id.msg_text)
            val tvTime: TextView = v.findViewById(R.id.msg_time)
            val tvDeleted: TextView = v.findViewById(R.id.msg_deleted_badge)
            val tvEdited: TextView = v.findViewById(R.id.msg_edited_badge)
        }
    }
}
