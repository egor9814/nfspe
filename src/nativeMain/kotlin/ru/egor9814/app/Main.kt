package ru.egor9814.app

import kotlinx.cinterop.*
import org.sdl.*
import ru.egor9814.util.throwIfSDLError

@OptIn(ExperimentalForeignApi::class)
fun MemScope.Main(args: Array<String>): Int {
    println("args> ${args.joinToString()}")
    val arena = Arena()
    defer { arena.clear() }
    val window = SDL_CreateWindow("Untitled", 640, 480, 0u).throwIfSDLError(arena)
    arena.defer { SDL_DestroyWindow(window) }
    val renderer = SDL_CreateRenderer(
        window,
        null,
        SDL_RENDERER_ACCELERATED or SDL_RENDERER_PRESENTVSYNC
    ).throwIfSDLError(arena)
    arena.defer { SDL_DestroyRenderer(renderer) }
    val event = alloc<SDL_Event>()
    var running = true
    var red: UByte = 0u
    var green: UByte = 0u
    var blue: UByte = 0u
    while (running) {
        SDL_RenderClear(renderer)
        SDL_SetRenderDrawColor(renderer, red, green, blue, SDL_ALPHA_OPAQUE.toUByte())
        SDL_RenderPresent(renderer)

        while (SDL_PollEvent(event.ptr) != 0) {
            when (event.type) {
                SDL_EVENT_QUIT    -> {
                    running = false
                    break
                }
                SDL_EVENT_KEY_DOWN -> {
                    val keyboardEvent = event.ptr.reinterpret<SDL_KeyboardEvent>().pointed
                    when (keyboardEvent.keysym.scancode) {
                        SDL_SCANCODE_Q -> {
                            running = false
                            break
                        }
                        SDL_SCANCODE_1 -> red++
                        SDL_SCANCODE_2 -> red--
                        SDL_SCANCODE_3 -> green++
                        SDL_SCANCODE_4 -> green--
                        SDL_SCANCODE_5 -> blue++
                        SDL_SCANCODE_6 -> blue--
                    }
                }
            }
        }
    }
    return 0
}
