package com.staligtredan.thermo.controleur;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.staligtredan.thermo.modele.slaves.Cuve;

public class MQTTAddSlave implements IMqttMessageListener {

	@Override
	public void messageArrived( String topic, MqttMessage message ) throws Exception {

		
		int id=0;
		if( ( Brasserie.getModules().size() >= 1 ) && 
				(Brasserie.getModules().get(Brasserie.getModules().size()-1) != null) )
			id = Brasserie.getModules().get(Brasserie.getModules().size()-1).getId()+1;
		
		Cuve c = new Cuve(id);

		Brasserie.getModules().add(c);
		
		Brasserie.publishSlaves();
	}

}
