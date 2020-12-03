# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

#查找当前目录下的所有源文件，保存至DIR_SRCS变量中
#aux_source_directory(. DIR_SRCS)

link_directories(${CMAKE_SOURCE_DIR}/../../../libs/ffmpeg-lib/${ANDROID_ABI})

add_library(ffmpeg-jni SHARED native-lib.cpp)

target_include_directories(ffmpeg-jni PRIVATE ${CMAKE_SOURCE_DIR}/include_ffmpeg/)

find_library(log-lib log)

target_link_libraries(ffmpeg-jni ${log-lib})
target_link_libraries(ffmpeg-jni swscale)
target_link_libraries(ffmpeg-jni avcodec)
target_link_libraries(ffmpeg-jni avformat)
target_link_libraries(ffmpeg-jni swresample)
target_link_libraries(ffmpeg-jni avutil)
target_link_libraries(ffmpeg-jni avfilter)
target_link_libraries(ffmpeg-jni avdevice)

