package com.staligtredan.thermo.modele.oneWire;

import com.staligtredan.onewire.DS2413;

/** 
* @author Brendan
* @since v1.0
* @version 1.0 (02/2018)
*/
public class PowerSwitch extends OneWireElement {

	private boolean pioA;
	private boolean pioB;
	
	public PowerSwitch(byte[] adress) {
		
		super(adress);
		name = "nouveau relais";
		pioA = false;
		pioB = false;
	}

	public boolean isPioA() {
		return pioA;
	}

	public void setPioA(boolean pioA) {
		this.pioA = pioA;
		DS2413.setOutputs(getAddress(), pioA, pioB);
	}

	public boolean isPioB() {
		return pioB;
	}

	public void setPioB(boolean pioB) {
		this.pioB = pioB;
		DS2413.setOutputs(getAddress(), pioA, pioB);
	}
}
