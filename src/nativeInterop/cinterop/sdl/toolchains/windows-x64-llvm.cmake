include(CMakeForceCompiler)

# the name of the target operating system
set(CMAKE_SYSTEM_NAME Windows)

set(VS2019_BASE "C:/Program Files (x86)/Microsoft Visual Studio/2019")
set(VS_LLVM_TOOLS_BASE "VC/Tools/Llvm/x64")
set(VS_LLVM_BIN "${VS_LLVM_TOOLS_BASE}/bin")
set(VS_LLVM_LIB "${VS_LLVM_TOOLS_BASE}/lib")
set(VS_CLANG_BASE "${VS_LLVM_BIN}/clang")

# VS Community 2019
set(VSC2019_TOOLCHAIN_PATH "${VS2019_BASE}/Community")
# VS Professional 2019
set(VSP2019_TOOLCHAIN_PATH "${VS2019_BASE}/Professional")

if (EXISTS "${VSP2019_TOOLCHAIN_PATH}/${VS_CLANG_BASE}.exe")
    set(VS_TOOLCHAIN_PATH ${VSP2019_TOOLCHAIN_PATH})
elseif (EXISTS "${VSC2019_TOOLCHAIN_PATH}/${VS_CLANG_BASE}.exe")
    set(VS_TOOLCHAIN_PATH ${VSC2019_TOOLCHAIN_PATH})
else ()
    message(FATAL_ERROR "Visual Studio compiler not found")
endif ()

# which compilers to use for C and C++
set(CMAKE_C_COMPILER "${VS_TOOLCHAIN_PATH}/${VS_CLANG_BASE}.exe" CACHE STRING "C compiler" FORCE)
set(CMAKE_CXX_COMPILER "${VS_TOOLCHAIN_PATH}/${VS_CLANG_BASE}++.exe" CACHE STRING "C++ compiler" FORCE)
set(CMAKE_RC_COMPILER "${VS_TOOLCHAIN_PATH}/${VS_LLVM_BIN}/llvm-rc.exe" CACHE STRING "RC compiler" FORCE)
set(CMAKE_AR "${VS_TOOLCHAIN_PATH}/${VS_LLVM_BIN}/llvm-ar.exe" CACHE STRING "AR" FORCE)

# here is the target environment located
set(CMAKE_FIND_ROOT_PATH "${VS_LLVM_LIB}" CACHE STRING "Libraries" FORCE)

# generator
set(CMAKE_GENERATOR "Visual Studio 16 2019" CACHE STRING "Generator" FORCE)

# adjust the default behaviour of the FIND_XXX() commands:
# search headers and libraries in the target environment, search
# programs in the host environment
SET(CMAKE_FIND_ROOT_PATH_MODE_PROGRAM NEVER)
SET(CMAKE_FIND_ROOT_PATH_MODE_LIBRARY ONLY)
SET(CMAKE_FIND_ROOT_PATH_MODE_INCLUDE ONLY)
