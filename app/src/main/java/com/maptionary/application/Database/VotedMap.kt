package com.maptionary.application.Database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "voted_maps")
class VotedMap(@PrimaryKey @ColumnInfo(name = "id") val mapId : Int)