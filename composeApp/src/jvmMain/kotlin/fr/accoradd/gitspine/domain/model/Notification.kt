package fr.accoradd.gitspine.domain.model

import java.util.UUID

data class Notification(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String = "",
    val progress: Progress = Progress.Indeterminate,
    val status: Status = Status.Running
) {
    sealed class Progress {
        data object Indeterminate : Progress()
        data class Determinate(val current: Int, val total: Int) : Progress() {
            val percentage: Float
                get() = if (total > 0) current.toFloat() / total.toFloat() else 0f
        }
    }

    enum class Status {
        Running,
        Success,
        Error
    }
}
