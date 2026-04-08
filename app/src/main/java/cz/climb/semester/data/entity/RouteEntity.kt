package cz.climb.semester.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "routes",
    foreignKeys = [
        ForeignKey(
            entity = AreaEntity::class,
            parentColumns = ["id"],
            childColumns = ["areaId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = CragEntity::class,
            parentColumns = ["id"],
            childColumns = ["cragId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = SectorEntity::class,
            parentColumns = ["id"],
            childColumns = ["sectorId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("areaId"), Index("cragId"), Index("sectorId")],
)
data class RouteEntity(
    @PrimaryKey val id: String,
    val name: String,
    val grade: String = "",
    val gradeSystem: String = "French",
    val gradeIndex: Int = -1,
    val type: String = "sport",
    val description: String = "",
    val rating: Int = 0,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val areaId: String? = null,
    val cragId: String? = null,
    val sectorId: String? = null,
    val photoUri: String? = null,
    val rockType: String = "",
    val indoorColor: String = "",
    val routeDate: String = "",
    val routeStatus: String = "",
    val favorite: Boolean = false,
    val createdAt: String,
    val updatedAt: String,
    val synced: Boolean = false,
)
