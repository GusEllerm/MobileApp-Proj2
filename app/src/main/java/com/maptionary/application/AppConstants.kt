package com.maptionary.application

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions


object AppConstants {

    // temp colours and implementation
    val COLOURS = listOf("Red", "Green", "Blue", "Black", "Yellow")


    val MAP_TYPES = listOf("Normal", "Satellite", "Hybrid", "Terrain")
    val MAP_TYPES_MAP = mapOf<String, Int>("Normal" to GoogleMap.MAP_TYPE_NORMAL,
        "Satellite" to GoogleMap.MAP_TYPE_SATELLITE,
        "Hybrid" to GoogleMap.MAP_TYPE_HYBRID,
        "Terrain" to GoogleMap.MAP_TYPE_TERRAIN)

    val ORDER_TYPES = mapOf("Most recent" to "newest", "Least recent" to "oldest", "Most votes" to "most_votes", "Least votes" to "least_votes")

    val CATEGORIES = mapOf("Any" to "")


    private const val END_POINT = "http://101.100.139.72:4567/api"

    const val GPS_END = "$END_POINT/gps_map"

    const val GPS_LIST_END = "$GPS_END/list"

    const val GPS_VOTE_END = "$GPS_END/vote"

    const val ERROR_JSON_STRING_OBJECT = "{ id : -1 }"

    const val ERROR_JSON_STRING_VOTED = "{ votes : -1 }"

    const val ERROR_JSON_STRING_ARRAY = "[]"

    const val ERROR_JSON_STRING_GPSMAP = "{ id : -1, mapType : \"Normal\", mapTitle : \"Error downloading\", " +
            "fragmentAmount : 0, votes : -1, category : \"Error\", imageData : \"\", fragments : []}"

    const val GRAVITY : Float = 9.81f

    const val MAX_ACCELERATION : Float = 3.0f

    const val MAX_SHAKES : Double = 3.0

    const val MAX_EVENTS : Int = 100
}