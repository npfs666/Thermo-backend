package com.staligtredan.thermo.util;

public class Utils {
	
	/*
	 * Convert a string (40,-86,57,70,26,19,2,54) to a byte[] ([ 0x3A 0x82 0xF7 0x41 0x00 0x00 0x00 0x83 ])
	 */
	public static byte[] strToByteArray(String str) {
		
		byte[] bytes= new byte[8];
		
		int i=0;
		for( String s : str.split(",")) {
			bytes[i] = Byte.valueOf(s);
			i++;
		}
		
		return bytes;
	}

}
