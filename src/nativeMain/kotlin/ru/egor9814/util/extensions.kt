package ru.egor9814.util

import kotlinx.cinterop.Arena
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import org.sdl.SDL_GetError

@OptIn(ExperimentalForeignApi::class)
fun sdlError() = SDL_GetError()?.toKString()

@OptIn(ExperimentalForeignApi::class)
fun <T> T?.throwIfSDLError(arena: Arena? = null): T = when {
    this == null -> sdlError().let {
        arena?.clear()
        if (it == null)
            throw NullPointerException()
        else
            throw throw RuntimeException(it)
    }
    else -> this
}
