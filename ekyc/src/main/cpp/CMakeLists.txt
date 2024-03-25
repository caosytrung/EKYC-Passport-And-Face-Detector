cmake_minimum_required(VERSION 3.18.1)
add_compile_options(-std=c++11)
project("cpp1")

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fopenmp")
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fopenmp")
find_package(OpenMP)
find_package( OpenCV REQUIRED )
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} ${OpenMP_C_FLAGS}")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${OpenMP_CXX_FLAGS} -std=c++11 -Wall -Ofast -Wfatal-errors -D_MWAITXINTRIN_H_INCLUDED")

set(ncnn_DIR ${CMAKE_SOURCE_DIR}/ncnn-20220701-android/${ANDROID_ABI}/lib/cmake/ncnn)
find_package(ncnn REQUIRED)
set_target_properties(
        ncnn PROPERTIES
        INTERFACE_COMPILE_OPTIONS "-frtti;-fexceptions"
        # ncnn.cmake 里面是关的，把它重新打开防止跟opencv2冲突，如果是重新编译ncnn的请自己尝试要开还是关
)

set(ANDROID_OPENCV_COMPONENTS "opencv_java" CACHE STRING "")
message(STATUS "ANDROID_ABI=${ANDROID_ABI}")
find_package(OpenCV REQUIRED COMPONENTS ${ANDROID_OPENCV_COMPONENTS})

add_library(
        cpp1
        SHARED
        native-lib.cpp
        src/config.h
        src/config.cpp
        src/tools.h
        src/tools.cpp
        src/face.h
        src/face.cpp
        src/dto.hpp
        src/nanodet.hpp
        src/nanodet.cpp
)

find_library(
        log-lib
        log)

target_link_libraries(
        cpp1
        ${log-lib}
        ${ANDROID_OPENCV_COMPONENTS}
        ncnn
)
