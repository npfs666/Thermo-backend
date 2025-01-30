package com.staligtredan.thermo.modele.slaves;

import com.staligtredan.thermo.modele.oneWire.PowerSwitch;
import com.staligtredan.thermo.modele.oneWire.TemperatureSensor;

public  class SlaveModule {
	
	public static final int typeCuve = 1;
	public static final int typeCirculateur = 2;
	public static final int typeTemperature = 3;
	
	private int id;
	private int type;
	private PowerSwitch ds2413;
	private TemperatureSensor ds18b20;
	
	public SlaveModule( int id , int type) {
		this.id = id;
		this.type = type;
		this.ds18b20 = new TemperatureSensor(new byte[0]);
		this.ds2413 = new PowerSwitch(new byte[0]);
	}



	public int getId() {
		return id;
	}



	public PowerSwitch getDs2413() {
		return ds2413;
	}



	public void setDs2413( PowerSwitch ds2413 ) {
		this.ds2413 = ds2413;
	}



	public TemperatureSensor getDs18b20() {
		return ds18b20;
	}



	public void setDs18b20( TemperatureSensor ds18b20 ) {
		this.ds18b20 = ds18b20;
	}



	public int getType() {
		return type;
	}



	public void setType( int type ) {
		this.type = type;
	}
}
