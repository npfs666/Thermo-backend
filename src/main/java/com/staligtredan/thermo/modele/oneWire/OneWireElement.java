package com.staligtredan.thermo.modele.oneWire;

/**
 * 
 */
public class OneWireElement {
	
	private byte[] address;
	protected String name;
	
	public OneWireElement(byte[] address) {
		this.address = address;
		this.name = "nouvel élement";
	}
	
	public byte[] getAddress() {
		return address;
	}

	public byte getFamily() {
		
		if( address != null )
			return address[0];
		else
			return 0;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
