package com.example.termtwoproject

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drawings")
class Drawing(@PrimaryKey(autoGenerate = true) var id: Long = 0,
    var title: String,
    @ColumnInfo(name = "map_type") var mapType: String,
    @ColumnInfo(name = "line_color") var lineColor: String,
    @ColumnInfo(name = "fragment_amount") var fragmentAmount: Int)