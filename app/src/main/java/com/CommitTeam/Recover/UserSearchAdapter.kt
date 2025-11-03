package com.CommitTeam.Recover

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.CommitTeam.Recover.models.UserProfile

class UserSearchAdapter(
    private val context: Context,
    private var users: List<UserProfile>
) : RecyclerView.Adapter<UserSearchAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val usernameText: TextView = view.findViewById(R.id.user_username)
        val fullNameText: TextView = view.findViewById(R.id.user_full_name)
        val avatar: ImageView = view.findViewById(R.id.user_avatar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_user_search, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        holder.usernameText.text = "@${user.username}"

        val fullName = listOfNotNull(user.firstName, user.lastName)
            .joinToString(" ")
            .ifBlank { "Без имени" }

        holder.fullNameText.text = fullName

        // Здесь можно добавить загрузку аватара, если появится позже
        holder.avatar.setImageResource(R.drawable.ic_menu_profile)
    }

    override fun getItemCount() = users.size

    fun updateData(newUsers: List<UserProfile>) {
        users = newUsers
        notifyDataSetChanged()
    }
}
