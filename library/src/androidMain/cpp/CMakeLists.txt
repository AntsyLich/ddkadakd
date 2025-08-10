cmake_minimum_required(VERSION 3.14)
project(imagedecoder_jni C CXX ASM)

# This path assumes this CMakeLists.txt is in src/androidMain/cpp/
set(COMMON_CPP_DIR ../../commonMain/cpp)

# Add the common C++ code and its dependencies as a subdirectory.
# This will execute the CMakeLists.txt in commonMain/cpp, building all external projects
# (vips, jpeg-turbo, etc.) and the imagecore_static library.
# The second argument specifies the binary directory for this sub-build.
add_subdirectory(${COMMON_CPP_DIR} ${CMAKE_CURRENT_BINARY_DIR}/common_build_dir)

# Define the JNI shared library
add_library(imagedecoder SHARED
        image-decoder/java_stream.cpp
        image-decoder/java_wrapper.cpp
        image-decoder/java_objects.cpp
)

# Include directories:
# 1. Headers from the JNI code itself (if any, usually covered by current dir)
# 2. Headers from the imagecore_static library in commonMain/cpp/image-decoder
# 3. Headers from the dependencies (vips, etc.) installed by the common build
#    into its fakeroot/include directory.
set(IMAGECORE_STATIC_INCLUDE_DIR ${COMMON_CPP_DIR}/image-decoder)
set(COMMON_DEPS_INCLUDE_DIR ${CMAKE_CURRENT_BINARY_DIR}/common_build_dir/fakeroot/include)

target_include_directories(imagedecoder PRIVATE
    ${IMAGECORE_STATIC_INCLUDE_DIR}
    ${COMMON_DEPS_INCLUDE_DIR}
)

# Link against the static library built by the common code.
# The imagecore_static target is defined in the common_build_dir scope.
# Also link against standard Android JNI libraries.
# The VIPS library and its dependencies are statically linked into imagecore_static.
# If lcms2 is not part of VIPS_STATIC_LIBRARIES in imagecore_static, it might need to be added here too.
# However, imagecore_static already links lcms2.

target_link_libraries(imagedecoder
    imagecore_static  # The static library from commonMain
    android
    jnigraphics
    log
    # The C++ standard library should be handled by the NDK toolchain (e.g., libc++_shared.so or static variant)
)

# Optional: If you need to explicitly point to the .a file if CMake has trouble finding imagecore_static target
# This usually isn't needed if add_subdirectory() is used correctly and targets are named uniquely.
# get_target_property(IMAGECORE_LIB_LOCATION imagecore_static ARCHIVE_OUTPUT_DIRECTORY)
# if(IMAGECORE_LIB_LOCATION)
#    target_link_directories(imagedecoder PRIVATE ${IMAGECORE_LIB_LOCATION})
# else()
#    # Fallback if property not found, assumes a standard output location within common_build_dir
#    target_link_directories(imagedecoder PRIVATE ${CMAKE_CURRENT_BINARY_DIR}/common_build_dir/image-decoder)
# endif()
