

###
#
# compile for [armv7] [android-19]
#
###


PREFIX=~/work/av/ffmpeg-4.3.1-armv7a-29
SYSROOT=~/program/android-ndk-r21b/toolchains/llvm/prebuilt/linux-x86_64/sysroot
PLATFORM=~/program/android-ndk-r21b/toolchains/llvm/prebuilt/linux-x86_64

./configure \
    --prefix=$PREFIX \
    --cross-prefix=$PLATFORM/bin/arm-linux-androideabi- \
    --cross-prefix-clang=$PLATFORM/bin/armv7a-linux-androideabi19- \
    --target-os=android \
    --arch=arm \
    --sysroot=$SYSROOT \
    --enable-shared \
    --disable-static \
    --disable-programs \
    --enable-cross-compile && make -j8 && make install





###
#
# compile for [arm64-v8A] [android-29]
#
###


PREFIX=~/work/av/ffmpeg-4.3.1-arm64-v8a-29
SYSROOT=~/program/android-ndk-r21b/toolchains/llvm/prebuilt/linux-x86_64/sysroot
PLATFORM=~/program/android-ndk-r21b/toolchains/llvm/prebuilt/linux-x86_64

./configure \
    --prefix=$PREFIX \
    --cross-prefix=$PLATFORM/bin/aarch64-linux-android- \
    --cross-prefix-clang=$PLATFORM/bin/aarch64-linux-android29- \
    --target-os=android \
    --arch=aarch64 \
    --sysroot=$SYSROOT \
    --enable-shared \
    --disable-static \
    --disable-programs \
    --enable-cross-compile && make -j8 && make install





1. 下载ffmpeg源码
2. 修改configure脚本
3. 执行上述的脚本交叉编译出对应版本及对应平台的ffmpeg


4. 将lib库放到as项目中的非main/jniLibs目录，因为与cmake相冲突
5. 修改cmakelists脚本，
    add_library(avutil SHARED IMPORTED)
    set_target_properties(avutil PROPERTIES IMPORTED_LOCATION ${lib_src_DIR}/libavutil.so)
    avutil添加进target_link_libraries
    其余六个库一样的写法
6. 复制头文件到as项目中，同时更新cmakelists
7. 由于ffmpeg是c写的，c++调用c文件，所以头文件需要使用extern "C"框起来


至此，编译成功，运行成功。



