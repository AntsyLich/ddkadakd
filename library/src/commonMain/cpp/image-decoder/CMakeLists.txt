cmake_minimum_required(VERSION 3.14)
project(imagecore_static C CXX ASM)

add_library(imagecore_static STATIC
  # java_stream.cpp # Moved to androidMain
  # java_wrapper.cpp # Moved to androidMain
  # java_objects.cpp # Moved to androidMain
  borders.cpp
  row_convert.cpp
  decoder_vips.cpp # Assuming this is core logic
)

# target_link_libraries(imagecore_static android jnigraphics log lcms2) # Android libs removed, lcms2 handled by vips

# set pkg-config libdir path - this should be handled by the parent CMake that includes this one
# set(ENV{PKG_CONFIG_LIBDIR} "${CMAKE_INSTALL_PREFIX}/lib/pkgconfig")
# message(STATUS "PKG_CONFIG_LIBDIR=${CMAKE_INSTALL_PREFIX}/lib/pkgconfig")

find_package(PkgConfig REQUIRED)

pkg_search_module(VIPS REQUIRED vips-cpp)
target_include_directories(imagecore_static PRIVATE ${VIPS_STATIC_INCLUDE_DIRS})
target_link_directories(imagecore_static PRIVATE ${VIPS_STATIC_LIBRARY_DIRS})

# remove `spng` from VIPS_STATIC_LIBRARY_DIRS, and add `spng_static`
list(REMOVE_ITEM VIPS_STATIC_LIBRARIES "spng")
list(APPEND VIPS_STATIC_LIBRARIES "spng_static")

list(REMOVE_ITEM VIPS_STATIC_LIBRARIES "c++") # Remove if linked by NDK toolchain automatically

message(STATUS "imagecore_static: VIPS_STATIC_INCLUDE_DIRS=${VIPS_STATIC_INCLUDE_DIRS}")
message(STATUS "imagecore_static: VIPS_STATIC_LIBRARY_DIRS=${VIPS_STATIC_LIBRARY_DIRS}")
message(STATUS "imagecore_static: VIPS_STATIC_LIBRARIES=${VIPS_STATIC_LIBRARIES}")

add_definitions(-DHAVE_LIBVIPS) # This definition might be project-wide, consider moving to top-level if needed by JNI too
# target_sources(imagecore_static PRIVATE decoder_vips.cpp) # Already added in add_library
target_link_libraries(imagecore_static PRIVATE vips-cpp ${VIPS_STATIC_LIBRARIES} lcms2 -static-libgcc -static-libstdc++)

# No install command for this static library here
