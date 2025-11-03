package com.CommitTeam.Recover

import android.app.Application

class App : Application() {

    // Раньше здесь была база данных через Room.
    // Теперь просто оставляем шаблон, чтобы не ломались зависимости.
    override fun onCreate() {
        super.onCreate()
        // Здесь можно инициализировать, например, RetrofitClient, если нужно.
    }
}
