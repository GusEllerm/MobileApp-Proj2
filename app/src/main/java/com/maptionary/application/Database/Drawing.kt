package com.maptionary.application.Database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drawings")
class Drawing(@PrimaryKey(autoGenerate = true) var id: Long = 0,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "map_type") var mapType: String,
    @ColumnInfo(name = "folder_name") var folderName: String,
    @ColumnInfo(name = "category") var category: String)
