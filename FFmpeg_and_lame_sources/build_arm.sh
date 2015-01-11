#!/bin/bash

#=======================================================================
# CONFIGURATION: 
# -> set the NDK variable below
#=======================================================================

export NDK=${HOME}/Scaricati/android-ndk-r9b

SYSROOT=$NDK/platforms/android-19/arch-arm

TOOLCHAIN=`echo $NDK/toolchains/arm-linux-androideabi-4.8/prebuilt/linux-x86_64`

export PATH=$TOOLCHAIN/bin:$PATH

#=======================================================================
# build lame based on content from 
# https://github.com/intervigilium/liblame
#=======================================================================

cd liblame
$NDK/ndk-build

# copy libmp3lame files into android-ndk appropriate folders, to let the ffmpeg configure script find them
cp -rn jni/lame $SYSROOT/usr/include
cp -n libs/armeabi-v7a/liblame.so $SYSROOT/usr/lib/libmp3lame.so

cd ..

#=======================================================================
# build FFmpeg adapting content from 
# http://bambuser.com/opensource
# and using FFmpeg build 2.1
#=======================================================================

BASE_DIR=`pwd`
BUILD_DIR="build"
rm -rf $BUILD_DIR
mkdir -p $BUILD_DIR
cd ffmpeg-2.1

DEST=$BASE_DIR/$BUILD_DIR

CFLAGS="-O3 -Wall -pipe -fpic -fasm"

FLAGS="--target-os=linux --cross-prefix=arm-linux-androideabi- --arch=arm \
	--sysroot=$SYSROOT \
	--enable-small \
	--enable-gpl \
	--enable-version3 \
	--disable-ffplay --disable-ffprobe --disable-ffserver \
	--disable-doc --disable-htmlpages --disable-manpages --disable-podpages --disable-txtpages \
	--enable-libmp3lame"

for version in neon armv7a armv5te; do

	case "$version" in
    	neon)
      		EXTRA_CFLAGS="-march=armv7-a -mfpu=neon -mfloat-abi=softfp -mvectorize-with-neon-quad"
      		EXTRA_LDFLAGS="-Wl,--fix-cortex-a8"
      		;;
		armv7a)
			EXTRA_CFLAGS="-march=armv7-a -mfpu=vfpv3-d16 -mfloat-abi=softfp"
      		EXTRA_LDFLAGS="-Wl,--fix-cortex-a8"
			;;
		armv5te)
			EXTRA_CFLAGS=""
			EXTRA_LDFLAGS=""
			;;
	esac
	PREFIX="$DEST/$version" && mkdir -p $PREFIX
	FLAGS="$FLAGS --prefix=$PREFIX"

	echo $FLAGS --extra-cflags="$CFLAGS $EXTRA_CFLAGS" --extra-ldflags="$EXTRA_LDFLAGS" > $PREFIX/info.txt
	./configure $FLAGS --extra-cflags="$CFLAGS $EXTRA_CFLAGS" --extra-ldflags="$EXTRA_LDFLAGS" | tee $PREFIX/configuration.txt
	[ $PIPESTATUS == 0 ] || exit 1

	make clean
	make -j4 || exit 1
	make prefix=$PREFIX install || exit 1

done

