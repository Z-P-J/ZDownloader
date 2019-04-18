//package com.zpj.qxdownloader.util;
//
//public class ContainerHelpers {
//
//    // This is Arrays.binarySearch(), but doesn't do any argument validation.
//    static int binarySearch(int[] array, int size, int value) {
//        int lo = 0;
//        int hi = size - 1;
//
//        while (lo <= hi) {
//            final int mid = (lo + hi) >>> 1;
//            final int midVal = array[mid];
//
//            if (midVal < value) {
//                lo = mid + 1;
//            } else if (midVal > value) {
//                hi = mid - 1;
//            } else {
//                return mid;  // value found
//            }
//        }
//        return ~lo;  // value not present
//    }
//
//    static int binarySearch(long[] array, int size, long value) {
//        int lo = 0;
//        int hi = size - 1;
//
//        while (lo <= hi) {
//            final int mid = (lo + hi) >>> 1;
//            final long midVal = array[mid];
//
//            if (midVal < value) {
//                lo = mid + 1;
//            } else if (midVal > value) {
//                hi = mid - 1;
//            } else {
//                return mid;  // value found
//            }
//        }
//        return ~lo;  // value not present
//    }
//
//}
