package com.example.termtwoproject.Database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.termtwoproject.Database.Drawing
import com.example.termtwoproject.Database.DrawingDao

@Database(entities = [Drawing::class], version = 1)
abstract class DrawingsDatabase: RoomDatabase() {
    abstract fun drawingDao(): DrawingDao
}