package com.example.termtwoproject



object AppConstants {

    // temp colours and implementation
    val COLOURS = listOf("Red", "Green", "Blue", "Black", "Yellow")


    val MAP_TYPES = listOf("Normal", "Satellite", "Hybrid", "Terrain")

    val ORDER_TYPES = mapOf("Most recent" to "newest", "Least recent" to "oldest", "Most votes" to "most_votes", "Least votes" to "least_votes")

    val CATEGORIES = mapOf("Any" to "")


    private const val END_POINT = "http://101.100.139.72:4567/api"

    const val GPS_END = "$END_POINT/gps_map"

    const val GPS_LIST_END = "$GPS_END/list"

    const val GPS_VOTE_END = "$GPS_END/vote"
}