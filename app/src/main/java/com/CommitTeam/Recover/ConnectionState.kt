package com.CommitTeam.Recover

/**
 * Описывает все возможные состояния подключения к серверу.
 */
sealed class ConnectionState {
    object Connected : ConnectionState()

    object Connecting : ConnectionState()

    object WaitingForNetwork : ConnectionState()

    object Updating : ConnectionState()


    object Disconnected : ConnectionState()
}