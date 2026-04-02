package com.pcs8.orientasi.util;

/**
 * Utility class for comparing numeric nomor (e.g., "3.1", "3.11", "3.2")
 * Used for natural sorting of program and initiative numbers
 */
public class NomorComparator {

    private NomorComparator() {
        // Utility class, no instantiation
    }

    /**
     * Compare two nomor strings numerically
     * Splits on dots and compares each segment numerically
     * 
     * Example: "3.1" < "3.2" < "3.10" < "3.10.1"
     * 
     * @param a First nomor string
     * @param b Second nomor string
     * @return -1 if a < b, 0 if a == b, 1 if a > b
     */
    public static int compare(String a, String b) {
        if (a == null || b == null) {
            return (a == null) ? (b == null ? 0 : -1) : 1;
        }
        
        String[] aParts = a.split("\\.");
        String[] bParts = b.split("\\.");
        int maxLength = Math.max(aParts.length, bParts.length);
        
        for (int i = 0; i < maxLength; i++) {
            // If one string runs out of parts, it's considered smaller
            if (i >= aParts.length) return -1;
            if (i >= bParts.length) return 1;
            
            String aPart = aParts[i];
            String bPart = bParts[i];
            
            // Try to parse as integers for numerical comparison
            try {
                int aNum = Integer.parseInt(aPart);
                int bNum = Integer.parseInt(bPart);
                if (aNum != bNum) {
                    return Integer.compare(aNum, bNum);
                }
            } catch (NumberFormatException e) {
                // If not integers, compare as strings
                int strCompare = aPart.compareTo(bPart);
                if (strCompare != 0) {
                    return strCompare;
                }
            }
        }
        
        return 0;
    }
}
