package cz.climb.semester.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ascents",
    foreignKeys = [
        ForeignKey(
            entity = RouteEntity::class,
            parentColumns = ["id"],
            childColumns = ["routeId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("routeId"), Index("sessionId")],
)
data class AscentEntity(
    @PrimaryKey val id: String,
    val routeId: String,
    val sessionId: String? = null,
    val date: String,
    val style: String = "redpoint",
    val category: String = "",
    val success: Boolean = true,
    val notes: String = "",
    val createdAt: String,
    val updatedAt: String,
    val synced: Boolean = false,
)
