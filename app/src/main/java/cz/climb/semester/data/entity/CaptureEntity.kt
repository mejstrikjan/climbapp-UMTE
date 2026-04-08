package cz.climb.semester.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "captures")
data class CaptureEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val filePath: String,
    val routeId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)
