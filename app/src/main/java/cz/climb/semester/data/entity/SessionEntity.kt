package cz.climb.semester.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val notes: String = "",
    val date: String,
    val autoName: Boolean = true,
    val startedAt: String,
    val endedAt: String? = null,
    val active: Boolean = true,
    val createdAt: String,
    val updatedAt: String,
    val synced: Boolean = false,
)
