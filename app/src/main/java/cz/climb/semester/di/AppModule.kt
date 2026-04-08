package cz.climb.semester.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import cz.climb.semester.data.dao.AreaDao
import cz.climb.semester.data.dao.AscentDao
import cz.climb.semester.data.dao.CaptureDao
import cz.climb.semester.data.dao.CragDao
import cz.climb.semester.data.dao.RouteDao
import cz.climb.semester.data.dao.SectorDao
import cz.climb.semester.data.dao.SessionDao
import cz.climb.semester.data.db.AppDatabase

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "semester_climb.db",
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideAreaDao(database: AppDatabase): AreaDao = database.areaDao()

    @Provides
    fun provideCragDao(database: AppDatabase): CragDao = database.cragDao()

    @Provides
    fun provideSectorDao(database: AppDatabase): SectorDao = database.sectorDao()

    @Provides
    fun provideRouteDao(database: AppDatabase): RouteDao = database.routeDao()

    @Provides
    fun provideSessionDao(database: AppDatabase): SessionDao = database.sessionDao()

    @Provides
    fun provideAscentDao(database: AppDatabase): AscentDao = database.ascentDao()

    @Provides
    fun provideCaptureDao(database: AppDatabase): CaptureDao = database.captureDao()

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
