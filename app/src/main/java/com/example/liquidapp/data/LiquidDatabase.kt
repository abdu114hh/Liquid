package com.example.liquidapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.liquidapp.data.dao.HydrationDao
import com.example.liquidapp.data.entity.DailyGoalEntity
import com.example.liquidapp.data.entity.WaterLogEntity
import com.example.liquidapp.data.repository.HydrationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Type converters for Room database to handle LocalDate.
class Converters {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    @TypeConverter
    fun fromLocalDate(date: LocalDate): String {
        return date.format(formatter)
    }

    @TypeConverter
    fun toLocalDate(dateString: String): LocalDate {
        return LocalDate.parse(dateString, formatter)
    }
}

// Main Room database for the Liquid app.
@Database(
    entities = [WaterLogEntity::class, DailyGoalEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class LiquidDatabase : RoomDatabase() {
    
    abstract fun hydrationDao(): HydrationDao
    
    companion object {
        @Volatile
        private var INSTANCE: LiquidDatabase? = null
        
        fun getInstance(context: Context): LiquidDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LiquidDatabase::class.java,
                    "liquid_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Initialize database with default data when created
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                // Insert default daily goal
                                val dao = database.hydrationDao()
                                val defaultGoal = DailyGoalEntity(
                                    start_date = LocalDate.now(),
                                    goal_oz = HydrationRepository.DEFAULT_GOAL_OZ
                                )
                                dao.insertGoal(defaultGoal)
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 