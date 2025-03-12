package com.staligtredan.thermo.modele;

import java.util.ArrayList;

import com.staligtredan.thermo.modele.oneWire.OneWireElement;
import com.staligtredan.thermo.modele.slaves.SlaveModule;

public class OneWireMaster {

	// Liste des object physiques qui ont un ou plusieurs éléments 1-wire
	private ArrayList<SlaveModule> modules;
	
	// Liste de tous les éléments 1-wire
	private ArrayList<OneWireElement> elts;
	
	/**
	 * Hystérésis de °C pour éviter les marche/arrêt intenpestifs
	 */
	public static final double tempHysteresis = 0.2;
	
	
	
	public OneWireMaster() {
		
		modules = new ArrayList<>();
		elts = new ArrayList<>();
	}

	public void setModules( ArrayList<SlaveModule> modules ) {
		this.modules = modules;
	}

	public void setElts( ArrayList<OneWireElement> elts ) {
		this.elts = elts;
	}

	public ArrayList<SlaveModule> getModules() {
		return modules;
	}

	public ArrayList<OneWireElement> getElts() {
		return elts;
	}
}
