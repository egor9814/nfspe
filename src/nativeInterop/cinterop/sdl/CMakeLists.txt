cmake_minimum_required(VERSION 3.27)
if (SDL_TOOLCHAIN)
    set(CMAKE_TOOLCHAIN_FILE "${CMAKE_CURRENT_LIST_DIR}/toolchains/${SDL_TOOLCHAIN}.cmake")
endif ()
project(sdl_cinterop C)

# Debug messages
# message(STATUS "CMAKE_C_COMPILER: ${CMAKE_C_COMPILER}")
# message(STATUS "CMAKE_CXX_COMPILER: ${CMAKE_CXX_COMPILER}")
# message(STATUS "CMAKE_RC_COMPILER: ${CMAKE_RC_COMPILER}")
# message(STATUS "CMAKE_AR: ${CMAKE_AR}")
# message(STATUS "CMAKE_GENERATOR: ${CMAKE_GENERATOR}")

set(CMAKE_C_STANDARD 11)

include(FetchContent)

# Get SDL3 sources
FetchContent_Declare(
        sdl3
        GIT_REPOSITORY https://github.com/libsdl-org/SDL.git
        GIT_TAG 57a160c # https://github.com/libsdl-org/SDL/commit/57a160c9ab94613a6bab96287fe51b7cf1dafc56
)
# Building only dynamic libraries
set(SDL_SHARED ON)
set(SDL_STATIC OFF)
FetchContent_MakeAvailable(sdl3)

# Selecting output directory
if (SDL_ARTIFACTS_DIR)
    set(SDL_OUT "${SDL_ARTIFACTS_DIR}")
else ()
    set(SDL_OUT "${CMAKE_CURRENT_LIST_DIR}/out")
endif ()

if (WIN32)
    set(lib_prefix "")
    set(lib_suffix_shared ".dll")
    set(lib_suffix_import ".lib")
else ()
    set(lib_prefix "lib")
    set(lib_suffix_shared ".so")
    set(lib_suffix_import ".a")
endif ()
add_custom_target(${PROJECT_NAME}
        # Copy headers
        COMMAND ${CMAKE_COMMAND} -E copy_directory "${sdl3_SOURCE_DIR}/include" "${SDL_OUT}/include"

        # Copy dynamic library
        COMMAND ${CMAKE_COMMAND} -E copy
        "${sdl3_BINARY_DIR}/${CMAKE_BUILD_TYPE}/${lib_prefix}SDL3${lib_suffix_shared}"
        "${SDL_OUT}/${lib_prefix}SDL3${lib_suffix_shared}"

        # Copy import library
        COMMAND ${CMAKE_COMMAND} -E copy
        "${sdl3_BINARY_DIR}/${CMAKE_BUILD_TYPE}/${lib_prefix}SDL3${lib_suffix_import}"
        "${SDL_OUT}/${lib_prefix}SDL3${lib_suffix_import}"
)
add_dependencies(${PROJECT_NAME} SDL3-shared)
