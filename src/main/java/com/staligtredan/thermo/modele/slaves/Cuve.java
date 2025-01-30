package com.staligtredan.thermo.modele.slaves;

import java.util.Date;

import com.staligtredan.thermo.modele.oneWire.PowerSwitch;

/** 
* @author Brendan
* @since v1.0
* @version 1.0 (02/2018)
*/
public class Cuve extends SlaveModule {
	
	private PowerSwitch circulateur;
	private boolean regulation;
	private boolean active;
	private double setpoint;
	private String nomCommun;
	private String comment;
	private Date dateDebut;
	
	public Cuve(int id) {
		
		super(id, SlaveModule.typeCuve);
		circulateur = new PowerSwitch(new byte[0]);
		regulation = false;
		active = false;
		setpoint = 15;
		nomCommun = "nouvelle cuve";
		setComment("bière 11/24");
		dateDebut = new Date(0);
	}

	public String getNomCommun() {
		return nomCommun;
	}

	public void setNomCommun(String nomCommun) {
		this.nomCommun = nomCommun;
	}

	public Date getDateDebut() {
		return dateDebut;
	}

	public void setDateDebut(Date dateDebut) {
		this.dateDebut = dateDebut;
	}

	public boolean isRegulation() {
		return regulation;
	}

	public void setRegulation(boolean regulation) {
		this.regulation = regulation;
	}

	public double getConsigne() {
		return setpoint;
	}

	public void setConsigne(double consigne) {
		this.setpoint = consigne;
	}

	public PowerSwitch getCirculateur() {
		return circulateur;
	}

	public void setCirculateur( PowerSwitch circulateur ) {
		this.circulateur = circulateur;
	}

	public String getComment() {
		return comment;
	}

	public void setComment( String comment ) {
		this.comment = comment;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive( boolean active ) {
		this.active = active;
	}

	
}
