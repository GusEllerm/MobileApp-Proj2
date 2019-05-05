package com.example.termtwoproject

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Drawing::class], version = 1)
abstract class DrawingsDatabase: RoomDatabase() {
    abstract fun drawingDao(): DrawingDao
}