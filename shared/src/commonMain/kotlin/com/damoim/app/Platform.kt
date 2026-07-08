package com.damoim.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform