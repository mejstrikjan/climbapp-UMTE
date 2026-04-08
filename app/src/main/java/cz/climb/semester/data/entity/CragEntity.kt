package cz.climb.semester.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "crags",
    foreignKeys = [
        ForeignKey(
            entity = AreaEntity::class,
            parentColumns = ["id"],
            childColumns = ["areaId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("areaId")],
)
data class CragEntity(
    @PrimaryKey val id: String,
    val name: String,
    val areaId: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val createdAt: String,
    val synced: Boolean = false,
)
