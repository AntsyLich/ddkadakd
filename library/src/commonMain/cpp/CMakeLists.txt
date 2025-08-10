find_program(Make_EXECUTABLE make)
if(NOT Make_EXECUTABLE)
    message(FATAL_ERROR "Make is required")
endif()

find_program(Ninja_EXECUTABLE ninja)
if(NOT Ninja_EXECUTABLE)
    message(FATAL_ERROR "Ninja is required")
endif()

find_program(Meson_EXECUTABLE meson)
if(NOT Meson_EXECUTABLE)
    message(FATAL_ERROR "Meson is required")
endif()

cmake_minimum_required(VERSION 3.14)
project(imagedecoder_common_super C CXX ASM)

set(THIRD_PARTY_LIB_PATH ${CMAKE_BINARY_DIR}/fakeroot)
set(CMAKE_FIND_ROOT_PATH ${THIRD_PARTY_LIB_PATH};${CMAKE_FIND_ROOT_PATH})
set_directory_properties(PROPERTIES EP_PREFIX ${THIRD_PARTY_LIB_PATH})

set(CMAKE_FIND_LIBRARY_SUFFIXES ".a;.so")

find_program(CCACHE_FOUND ccache)
if(CCACHE_FOUND)
  set_property(GLOBAL PROPERTY CMAKE_C_COMPILER_LAUNCHER ccache)
  set_property(GLOBAL PROPERTY CMAKE_C_LINKER_LAUNCHER ccache)
endif(CCACHE_FOUND)

if(ANDROID_ABI STREQUAL "arm64-v8a")
    set(ANDROID_CPU_FAMILY "aarch64")
    set(ANDROID_CPU "aarch64")
    set(ANDROID_CPU_ENDIANESS "little")
    set(ANDROID_TARGET "aarch64-linux-android")
elseif(ANDROID_ABI STREQUAL "armeabi-v7a")
    set(ANDROID_CPU_FAMILY "arm")
    set(ANDROID_CPU "armv7-a")
    set(ANDROID_CPU_ENDIANESS "little")
    set(ANDROID_TARGET "armv7a-linux-androideabi")
elseif(ANDROID_ABI STREQUAL "x86")
    set(ANDROID_CPU_FAMILY "x86")
    set(ANDROID_CPU "i686")
    set(ANDROID_CPU_ENDIANESS "little")
    set(ANDROID_TARGET "i686-linux-android")
elseif(ANDROID_ABI STREQUAL "x86_64")
    set(ANDROID_CPU_FAMILY "x86_64")
    set(ANDROID_CPU "x86_64")
    set(ANDROID_CPU_ENDIANESS "little")
    set(ANDROID_TARGET "x86_64-linux-android")
else()
    # This CMakeLists.txt might be included by non-Android builds too.
    # Handle non-Android cases gracefully or set default values.
    message(STATUS "Non-Android build or ANDROID_ABI not set. Certain Android-specific settings will be skipped.")
    # Set default dummy values if needed for cross-file, or make its generation conditional
    set(ANDROID_CPU_FAMILY "unknown")
    set(ANDROID_CPU "unknown")
    set(ANDROID_CPU_ENDIANESS "little")
    set(ANDROID_TARGET "unknown-linux-unknown")
    set(ANDROID_PLATFORM_LEVEL "") # Ensure this is defined
endif()

set(CROSS_FILE_CONTENT "
[host_machine]
system = 'android'
cpu_family = '${ANDROID_CPU_FAMILY}'
cpu = '${ANDROID_CPU}'
endian = '${ANDROID_CPU_ENDIANESS}'

[binaries]
c = ['${CMAKE_C_COMPILER}', '--target=${ANDROID_TARGET}${ANDROID_PLATFORM_LEVEL}']
cpp = ['${CMAKE_CXX_COMPILER}', '--target=${ANDROID_TARGET}${ANDROID_PLATFORM_LEVEL}']
ar = '${CMAKE_AR}'
ld = '${CMAKE_LINKER}'
strip = '${CMAKE_STRIP}'
ranlib = '${CMAKE_RANLIB}'
as = '${CMAKE_ASM_COMPILER}'
pkg-config = 'pkg-config'

[built-in options]
c_args = ['-I${THIRD_PARTY_LIB_PATH}/include', '-Wno-error=format-nonliteral']
cpp_args = ['-I${THIRD_PARTY_LIB_PATH}/include', '-Wno-error=format-nonliteral']
c_link_args = ['-L${THIRD_PARTY_LIB_PATH}/lib']
cpp_link_args = ['-L${THIRD_PARTY_LIB_PATH}/lib']

[properties]
cmake_toolchain_file = '${CMAKE_TOOLCHAIN_FILE}'
pkg_config_libdir = '${THIRD_PARTY_LIB_PATH}/lib/pkgconfig'

[cmake]
CMAKE_BUILD_WITH_INSTALL_RPATH     = 'ON'
CMAKE_FIND_ROOT_PATH_MODE_PROGRAM  = 'NEVER'
CMAKE_FIND_ROOT_PATH_MODE_LIBRARY  = 'ONLY'
CMAKE_FIND_ROOT_PATH_MODE_INCLUDE  = 'ONLY'
CMAKE_FIND_ROOT_PATH_MODE_PACKAGE  = 'ONLY'
")

set(MESON_CROSS_FILE_PATH "${CMAKE_BINARY_DIR}/android-cross-file.txt")
file(WRITE ${MESON_CROSS_FILE_PATH} "${CROSS_FILE_CONTENT}")

set(EP_MESON_ARGS "--cross-file=${MESON_CROSS_FILE_PATH}"
  "--prefix=<INSTALL_DIR>"
  "--libdir=lib"
  "--default-library=static"
  "--buildtype=release"
)

string(REPLACE ";" "$<SEMICOLON>" CMAKE_FIND_ROOT_PATH_STR "${CMAKE_FIND_ROOT_PATH}")
string(REPLACE ";" "$<SEMICOLON>" CMAKE_FIND_LIBRARY_SUFFIXES_STR "${CMAKE_FIND_LIBRARY_SUFFIXES}")

set(EP_CMAKE_ARGS "-DANDROID_ABI=${ANDROID_ABI}"
  "-DANDROID_PLATFORM=${ANDROID_PLATFORM}"
  "-DANDROID_USE_LEGACY_TOOLCHAIN_FILE=OFF"
  "-DCMAKE_BUILD_TYPE=${CMAKE_BUILD_TYPE}"
  "-DCMAKE_INSTALL_PREFIX=${CMAKE_BINARY_DIR}/fakeroot"
  "-DCMAKE_PREFIX_PATH=${CMAKE_PREFIX_PATH}"
  "-DCMAKE_SYSROOT=${CMAKE_SYSROOT}"
  "-DCMAKE_TOOLCHAIN_FILE=${CMAKE_TOOLCHAIN_FILE}"
  "-DCMAKE_FIND_ROOT_PATH=${CMAKE_FIND_ROOT_PATH_STR}"
  "-DCMAKE_FIND_LIBRARY_SUFFIXES=${CMAKE_FIND_LIBRARY_SUFFIXES_STR}"
  "-DCMAKE_FIND_DEBUG_MODE=OFF"
  "-DBUILD_SHARED_LIBS=OFF"
  "-DBUILD_TESTING=OFF"
  "-DCMAKE_C_COMPILER_LAUNCHER=${CMAKE_C_COMPILER_LAUNCHER}"
  "-DCMAKE_C_LINKER_LAUNCHER=${CMAKE_C_LINKER_LAUNCHER}")

set(EP_AUTOTOOLS_ARGS "--host=${ANDROID_TARGET}${ANDROID_PLATFORM_LEVEL}"
  "CC=${CMAKE_C_COMPILER} --target=${ANDROID_TARGET}${ANDROID_PLATFORM_LEVEL}"
  "CXX=${CMAKE_CXX_COMPILER} --target=${ANDROID_TARGET}${ANDROID_PLATFORM_LEVEL}"
  "AR=${CMAKE_AR}"
  "AS=${CMAKE_ASM_COMPILER}"
  "LD=${CMAKE_LINKER}"
  "RANLIB=${CMAKE_RANLIB}"
  "STRIP=${CMAKE_STRIP}"
  "--prefix=${THIRD_PARTY_LIB_PATH}"
  "--disable-shared"
  "--enable-static"
  "--with-pic")

include("cmake/little-cms.cmake")
include("cmake/jpeg-turbo.cmake")
include("cmake/webp.cmake")
include("cmake/zlib.cmake")
include("cmake/dav1d.cmake")
include("cmake/de265.cmake")
include("cmake/heif.cmake")
include("cmake/highway.cmake")
include("cmake/brotli.cmake")
include("cmake/jxl.cmake")
include("cmake/tiff.cmake")
include("cmake/ffi.cmake")
include("cmake/iconv.cmake")
include("cmake/glib.cmake")
include("cmake/expat.cmake")
include("cmake/spng.cmake")
include("cmake/vips.cmake")

include(ExternalProject)
# Removed ExternalProject_Add(ep_image-decoder ...)

# Add the image-decoder (now imagecore_static) as a direct subdirectory
# This ensures its targets (like imagecore_static) are part of this main build configuration.
add_subdirectory(image-decoder)

# The static libraries from the dependencies above (ep_vips, ep_jpeg-turbo, etc.)
# and imagecore_static are built. The androidMain/cpp/CMakeLists.txt will then
# link against imagecore_static.
