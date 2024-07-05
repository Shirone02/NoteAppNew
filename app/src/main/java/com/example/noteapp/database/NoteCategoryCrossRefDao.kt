package com.example.noteapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import com.example.noteapp.models.NoteCategoryCrossRef

@Dao
interface NoteCategoryCrossRefDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(noteCategoryCrossRef: NoteCategoryCrossRef)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNoteCategoryCrossRefs(noteCategoryCrossRefs: List<NoteCategoryCrossRef>)
}