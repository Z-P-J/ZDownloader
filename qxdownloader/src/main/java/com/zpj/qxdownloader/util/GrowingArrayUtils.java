///*
// * Copyright (C) 2014 The Android Open Source Project
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.zpj.qxdownloader.util;
//
///**
// * A helper class that aims to provide comparable growth performance to ArrayList, but on primitive
// * arrays. Common array operations are implemented for efficient use in dynamic containers.
// *
// * All methods in this class assume that the length of an array is equivalent to its capacity and
// * NOT the number of elements in the array. The current size of the array is always passed in as a
// * parameter.
// *
// * @hide
// */
//public final class GrowingArrayUtils {
//
//    /**
//     * Primitive long version of
//     */
//    public static long[] append(long[] array, int currentSize, long element) {
//        assert currentSize <= array.length;
//
//        if (currentSize + 1 > array.length) {
//            long[] newArray = new long[growSize(currentSize)];
//            System.arraycopy(array, 0, newArray, 0, currentSize);
//            array = newArray;
//        }
//        array[currentSize] = element;
//        return array;
//    }
//
//    /**
//     * Primitive boolean version of
//     */
//    public static boolean[] append(boolean[] array, int currentSize, boolean element) {
//        assert currentSize <= array.length;
//
//        if (currentSize + 1 > array.length) {
//            boolean[] newArray = new boolean[growSize(currentSize)];
//            System.arraycopy(array, 0, newArray, 0, currentSize);
//            array = newArray;
//        }
//        array[currentSize] = element;
//        return array;
//    }
//
//    /**
//     * Primitive long version of
//     */
//    public static long[] insert(long[] array, int currentSize, int index, long element) {
//        assert currentSize <= array.length;
//
//        if (currentSize + 1 <= array.length) {
//            System.arraycopy(array, index, array, index + 1, currentSize - index);
//            array[index] = element;
//            return array;
//        }
//
//        long[] newArray = new long[growSize(currentSize)];
//        System.arraycopy(array, 0, newArray, 0, index);
//        newArray[index] = element;
//        System.arraycopy(array, index, newArray, index + 1, array.length - index);
//        return newArray;
//    }
//
//    /**
//     * Primitive boolean version of
//     */
//    public static boolean[] insert(boolean[] array, int currentSize, int index, boolean element) {
//        assert currentSize <= array.length;
//
//        if (currentSize + 1 <= array.length) {
//            System.arraycopy(array, index, array, index + 1, currentSize - index);
//            array[index] = element;
//            return array;
//        }
//
//        boolean[] newArray = new boolean[growSize(currentSize)];
//        System.arraycopy(array, 0, newArray, 0, index);
//        newArray[index] = element;
//        System.arraycopy(array, index, newArray, index + 1, array.length - index);
//        return newArray;
//    }
//
//    /**
//     * Given the current size of an array, returns an ideal size to which the array should grow.
//     * This is typically double the given size, but should not be relied upon to do so in the
//     * future.
//     */
//    public static int growSize(int currentSize) {
//        return currentSize <= 4 ? 8 : currentSize * 2;
//    }
//
//    // Uninstantiable
//    private GrowingArrayUtils() {}
//}
