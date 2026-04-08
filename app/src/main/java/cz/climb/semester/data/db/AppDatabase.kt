package cz.climb.semester.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import cz.climb.semester.data.dao.AreaDao
import cz.climb.semester.data.dao.AscentDao
import cz.climb.semester.data.dao.CaptureDao
import cz.climb.semester.data.dao.CragDao
import cz.climb.semester.data.dao.RouteDao
import cz.climb.semester.data.dao.SectorDao
import cz.climb.semester.data.dao.SessionDao
import cz.climb.semester.data.entity.AreaEntity
import cz.climb.semester.data.entity.AscentEntity
import cz.climb.semester.data.entity.CaptureEntity
import cz.climb.semester.data.entity.CragEntity
import cz.climb.semester.data.entity.RouteEntity
import cz.climb.semester.data.entity.SectorEntity
import cz.climb.semester.data.entity.SessionEntity

@Database(
    entities = [
        AreaEntity::class,
        CragEntity::class,
        SectorEntity::class,
        RouteEntity::class,
        SessionEntity::class,
        AscentEntity::class,
        CaptureEntity::class,
    ],
    version = 4,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun areaDao(): AreaDao
    abstract fun cragDao(): CragDao
    abstract fun sectorDao(): SectorDao
    abstract fun routeDao(): RouteDao
    abstract fun sessionDao(): SessionDao
    abstract fun ascentDao(): AscentDao
    abstract fun captureDao(): CaptureDao
}
