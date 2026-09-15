package com.srapp.core.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.srapp.blocking.data.BlockAttemptEntity
import com.srapp.blocking.data.BlockType
import com.srapp.blocking.data.BlockedAppEntity
import com.srapp.blocking.data.BlockingDao
import com.srapp.blocking.data.StreakEntity
import com.srapp.blocking.data.StreakType
import com.srapp.blocking.data.HabitEntity
import com.srapp.blocking.data.FocusSessionEntity

class Converters {
    @TypeConverter
    fun fromBlockType(v: BlockType): String = v.name
    @TypeConverter
    fun toBlockType(v: String): BlockType = BlockType.valueOf(v)

    @TypeConverter
    fun fromStreakType(v: StreakType): String = v.name
    @TypeConverter
    fun toStreakType(v: String): StreakType = StreakType.valueOf(v)
}

/**
 * Phase 1 schema only: blocked_apps, block_attempts, streaks.
 * Habits / focus_sessions / journal / character_stats / achievements /
 * Additional local-first feature tables are added through Room migrations.
 */
@Database(
    entities = [BlockedAppEntity::class, BlockAttemptEntity::class, StreakEntity::class, HabitEntity::class, FocusSessionEntity::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class SrDatabase : RoomDatabase() {
    abstract fun blockingDao(): BlockingDao

    companion object {
        @Volatile private var instance: SrDatabase? = null

        fun get(context: Context): SrDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    SrDatabase::class.java,
                    "sr_app.db"
                ).addMigrations(MIGRATION_1_2).build().also { instance = it }
            }

        private val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS habits (date TEXT NOT NULL, habitType TEXT NOT NULL, completed INTEGER NOT NULL, updatedAt INTEGER NOT NULL, PRIMARY KEY(date, habitType))")
                database.execSQL("CREATE TABLE IF NOT EXISTS focus_sessions (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, category TEXT NOT NULL, startTime INTEGER NOT NULL, endTime INTEGER, durationMinutes INTEGER, completed INTEGER NOT NULL)")
            }
        }
    }
}
