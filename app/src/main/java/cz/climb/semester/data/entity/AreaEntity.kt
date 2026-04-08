package cz.climb.semester.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "areas")
data class AreaEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String = "sport",
    val favorite: Boolean = false,
    val previewUri: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val createdAt: String,
    val synced: Boolean = false,
)
