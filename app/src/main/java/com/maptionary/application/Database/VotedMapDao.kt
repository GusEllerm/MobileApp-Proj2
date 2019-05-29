package com.maptionary.application.Database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query


@Dao
interface VotedMapDao {


    @Query("SELECT 1 FROM voted_maps WHERE id = :id")
    fun getVotedMapById(id : Int) : VotedMap

    @Insert
    suspend fun insertVotedMap(votedMap : VotedMap)

    @Query("DELETE FROM voted_maps WHERE id = :id")
    fun removeVotedMap(id : Int)
}