package cz.climb.semester.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sectors",
    foreignKeys = [
        ForeignKey(
            entity = CragEntity::class,
            parentColumns = ["id"],
            childColumns = ["cragId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = AreaEntity::class,
            parentColumns = ["id"],
            childColumns = ["areaId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("cragId"), Index("areaId")],
)
data class SectorEntity(
    @PrimaryKey val id: String,
    val name: String,
    val cragId: String? = null,
    val areaId: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val createdAt: String,
    val synced: Boolean = false,
)
