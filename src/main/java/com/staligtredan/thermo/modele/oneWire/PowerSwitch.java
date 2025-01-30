package com.staligtredan.thermo.modele.oneWire;

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
	}

	public boolean isPioB() {
		return pioB;
	}

	public void setPioB(boolean pioB) {
		this.pioB = pioB;
	}
}
