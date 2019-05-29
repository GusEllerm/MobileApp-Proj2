package com.maptionary.application.Database

import androidx.room.*

@Dao
interface DrawingDao {
    @Insert
    fun insert(drawing: Drawing): Long

    @Update
    fun update(drawing: Drawing)

    @Delete
    fun delete(drawing: Drawing)

    @Query("DELETE FROM drawings")
    fun deleteAll()

    @Query("SELECT * FROM drawings")
    fun getAll(): List<Drawing>

    @Query("SELECT * FROM drawings WHERE id = :id")
    fun getDrawingById(id: Long): Drawing

    @Query("SELECT * FROM drawings WHERE id = (SELECT MAX(id) FROM drawings)")
    fun getLastDrawing(): Drawing
}