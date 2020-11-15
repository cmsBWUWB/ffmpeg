# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             native-lib.cpp
        )

# include_directories("include_ffmpeg")

set(lib_src_DIR ${CMAKE_SOURCE_DIR}/../jniLib/${ANDROID_ABI})

target_include_directories(
        native-lib
        PRIVATE
        ${CMAKE_SOURCE_DIR}/include_ffmpeg/
)

add_library(avutil SHARED IMPORTED)
add_library(avcodec SHARED IMPORTED)
add_library(avdevice SHARED IMPORTED)
add_library(avformat SHARED IMPORTED)
add_library(swresample SHARED IMPORTED)
add_library(swscale SHARED IMPORTED)
add_library(avfilter SHARED IMPORTED)


#find_library(avutil-lib avutil)
#find_library(avcodec-lib avcodec)
#find_library(avdevice-lib avdevice)
#find_library(avformat-lib avformat)
#find_library(swresample-lib swresample)
#find_library(swscale-lib swscale)
#find_library(avfilter-lib avfilter)


set_target_properties(avutil PROPERTIES IMPORTED_LOCATION ${lib_src_DIR}/libavutil.so)
set_target_properties(avcodec PROPERTIES IMPORTED_LOCATION ${lib_src_DIR}/libavcodec.so)
set_target_properties(avdevice PROPERTIES IMPORTED_LOCATION ${lib_src_DIR}/libavdevice.so)
set_target_properties(avformat PROPERTIES IMPORTED_LOCATION ${lib_src_DIR}/libavformat.so)
set_target_properties(swresample PROPERTIES IMPORTED_LOCATION ${lib_src_DIR}/libswresample.so)
set_target_properties(swscale PROPERTIES IMPORTED_LOCATION ${lib_src_DIR}/libswscale.so)
set_target_properties(avfilter PROPERTIES IMPORTED_LOCATION ${lib_src_DIR}/libavfilter.so)


# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )


# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        native-lib

        swscale
        avcodec
        avformat
        swresample
        avutil
        avfilter
        avdevice
                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib}
        )