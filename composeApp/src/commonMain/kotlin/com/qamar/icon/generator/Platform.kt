package com.qamar.icon.generator

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform