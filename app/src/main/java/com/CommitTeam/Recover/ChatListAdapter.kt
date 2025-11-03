package com.CommitTeam.Recover

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.CommitTeam.Recover.models.Chat

class ChatListAdapter(private var chats: List<Chat>) :
    RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val chatNameTextView: TextView = view.findViewById(R.id.chat_name_text_view)
        val chatIconTextView: TextView = view.findViewById(R.id.chat_icon_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chats[position]

        val chatName = chat.name ?: "Чат"
        holder.chatNameTextView.text = chatName
        holder.chatIconTextView.text = chatName.firstOrNull()?.uppercase() ?: "R"

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ChatActivity::class.java).apply {
                putExtra("CHAT_ID", chat.id)
                putExtra("CHAT_NAME", chatName)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return chats.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newChats: List<Chat>) {
        chats = newChats
        notifyDataSetChanged()
    }
}