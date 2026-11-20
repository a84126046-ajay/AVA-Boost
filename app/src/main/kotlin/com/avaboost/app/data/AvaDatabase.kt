package com.avaboost.app.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val deviceId: String,
    val displayName: String,
    val accessLabel: String,
    val redeemedCodesCsv: String
)

@Entity(tableName = "user_games")
data class UserGameEntity(
    @PrimaryKey val packageName: String,
    val title: String,
    val lastSessionActive: Boolean = false
)

@Dao
interface ProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1") fun observe(): Flow<UserProfileEntity?>
    @Query("SELECT * FROM user_profile WHERE id = 1") suspend fun getOnce(): UserProfileEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(entity: UserProfileEntity)
    @Query("DELETE FROM user_profile") suspend fun clear()
}

@Dao
interface GameDao {
    @Query("SELECT * FROM user_games ORDER BY title ASC") fun observeAll(): Flow<List<UserGameEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(game: UserGameEntity)
    @Query("DELETE FROM user_games WHERE packageName = :packageName") suspend fun delete(packageName: String)
    @Query("UPDATE user_games SET lastSessionActive = :active WHERE packageName = :packageName") suspend fun setSessionActive(packageName: String, active: Boolean)
}

@Database(entities = [UserProfileEntity::class, UserGameEntity::class], version = 1, exportSchema = false)
abstract class AvaDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun gameDao(): GameDao
    companion object {
        @Volatile private var INSTANCE: AvaDatabase? = null
        fun getInstance(context: Context): AvaDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context.applicationContext, AvaDatabase::class.java, "avaboost.db").build().also { INSTANCE = it }
        }
    }
}
