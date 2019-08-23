package j2me.lang;

public class IntegerMethods {
	
	/**
	 * Find the number of trailing zeros in value.
	 * @param i the value to examine
	 * @return
	 */
	public static int numberOfTrailingZeros(int i) {
		int n = 0;
		if ((i & 0x0000FFFF) == 0) {
			i >>= 16;
			n += 16;
		}
		if ((i & 0x000000FF) == 0) {
			i >>= 8;
			n += 8;
		}
		if ((i & 0x0000000F) == 0) {
			i >>= 4;
			n += 4;
		}
		if ((i & 0x0000000C) == 0) {
			i >>= 2;
			n += 2;
		}
		if ((i & 0x00000003) == 0) {
			i >>= 1;
			n += 1;
		}
		if (i > 0) {
			return n;
		} else {
			return n + 1;
		}
	}

	/**
	 * Return the number of leading zeros in value.
	 * 
	 * @param x the value to examine
	 */
	public static int numberOfLeadingZeros(int x) {  
	    int y;  
	    int n = 32;  
	    y = x >> 16;  
	    if (y != 0) {  
	        n = n - 16;  
	        x = y;  
	    }  
	    y = x >> 8;  
	    if (y != 0) {  
	        n = n - 8;  
	        x = y;  
	    }  
	    y = x >> 4;  
	    if (y != 0) {  
	        n = n - 4;  
	        x = y;  
	    }  
	    y = x >> 2;  
	    if (y != 0) {  
	        n = n - 2;  
	        x = y;  
	    }  
	    y = x >> 1;  
	    if (y != 0) { 
	    	return n - 2;  
	    }
	    return n - x;  
	}  
	  

	/**
	 * Return the number of bits set in x.
	 * 
	 * @param i value to examine
	 */
	public static int bitCount(int i) {
		int n = 0;
		while (i > 0) {
			n += i & 1;
			i >>= 1;
		}
		return n;
	}
	
	/**
	 * 
	 * @param i
	 * @param gen bit count / 2
	 * @return
	 * @deprecated Think efficiency before coming up with new methods
	 */
	@Deprecated
	private static int rollBitCount(int i, int gen) {
		if (i == 0) {
			return 0;
		} else if (i == 1) {
			return 1;
		}
		int shift1 = gen;
		int shift2 = (1 << gen) - 1;
		int nextGen = gen >> 1;
		int nextHigher = i >>> shift1;
		int nextLower = i & shift2;
		int higher = rollBitCount(nextHigher, nextGen);
		int lower = rollBitCount(nextLower, nextGen);
		return higher + lower;
	}
}
