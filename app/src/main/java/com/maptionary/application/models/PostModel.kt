package com.maptionary.application.models

import java.net.URL

data class PostModel(val url : URL, val map : GpsMap?, val vote : Boolean, val method : String)