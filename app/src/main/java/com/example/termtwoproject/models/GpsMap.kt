package com.example.termtwoproject.models

data class GpsMap(val id : Int, val title : String, val type : String, val fragmentAmount : Int, val category : String,
                  val votes : Int, val fragments : List<Fragment>)