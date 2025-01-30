package com.staligtredan.thermo.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.staligtredan.onewire.DS2480B;

public class Log {

	//private final static Level lowLevelLogs = Level.FINE;
	
	public static void log(Level level, String msg) {
		
		Logger.getLogger(DS2480B.class.getName()).log(level, msg);
	}
}
