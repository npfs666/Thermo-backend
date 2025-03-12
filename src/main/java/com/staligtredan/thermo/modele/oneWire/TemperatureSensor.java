package com.staligtredan.thermo.modele.oneWire;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.staligtredan.onewire.DS18B20;

/** 
* @author Brendan
* @since v1.0
* @version 1.0 (02/2018)
*/
public class TemperatureSensor extends OneWireElement {
	
	private double temp;
	private byte resolution;
	private float offset;

	public TemperatureSensor(byte[] address) {
		
		super(address);
		temp = 0;
		name = "nouveau capteur";
		resolution = 12;
		offset = 0;
	}

	public double getTemp() {
		return temp;
	}
	public void setTemp(double temp) {
		this.temp = temp;
	}
	public byte getResolution() {
		return resolution;
	}
	public void setResolution(byte resolution) {
		this.resolution = resolution;
	}
	public float getOffset() {
		return offset;
	}
	public void setOffset(float offset) {
		this.offset = offset;
	}
	
	public void convertTemperature() {
		
		DS18B20.setResolution(getAddress(), getResolution());
		DS18B20.convert(getAddress(), false, null);
	}
	
	public void readTemperature() {
		double d = DS18B20.readTemp(getAddress()) + getOffset();
		BigDecimal bd = new BigDecimal(d).setScale(2, RoundingMode.HALF_UP);
		setTemp(bd.doubleValue());
	}
}
