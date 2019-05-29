package com.maptionary.application.Database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Drawing::class], version = 2)
abstract class DrawingsDatabase: RoomDatabase() {
    abstract fun drawingDao(): DrawingDao

    //abstract fun votedMapDao(): VotedMapDao
}