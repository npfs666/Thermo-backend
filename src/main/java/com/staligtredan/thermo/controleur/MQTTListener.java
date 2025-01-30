package com.staligtredan.thermo.controleur;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MQTTListener implements IMqttMessageListener {

	@Override
	public void messageArrived( String topic, MqttMessage message ) throws Exception {
		
		
		// Faire un refresh d'une sonde
		if( topic == "onewire/refreshTemp") {
			
			//DS18B20.convert(message, false, null);
		}

	}

}
