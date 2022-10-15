cmake_minimum_required(VERSION 3.16 FATAL_ERROR)

project(fintamath-android)

set(CMAKE_CONFIGURATION_TYPES
    "Release"
    CACHE STRING "" FORCE)

set(BUILD_SHARED_LIBS ON)
set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/lib)
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/lib)
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

add_subdirectory(fintamath)

file(
  GLOB_RECURSE
  SOURCES
  CONFIGURE_DEPENDS
  "src/*.cpp"
  "src/*.hpp")

add_library(${PROJECT_NAME} SHARED ${SOURCES})

target_include_directories(${PROJECT_NAME} PUBLIC src)

target_link_libraries(${PROJECT_NAME} PUBLIC fintamath)
