package com.staligtredan.thermo.controleur;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.Gson;
import com.staligtredan.thermo.modele.slaves.Cuve;
import com.staligtredan.thermo.modele.slaves.SlaveModule;

public class MQTTSetControl implements IMqttMessageListener {

	@Override
	public void messageArrived( String topic, MqttMessage message ) throws Exception {

		if( (message != null) && (message.getPayload().length > 0) ) {
			
			String s = new String(message.getPayload());
			
			Cuve msg = new Gson().fromJson(s, Cuve.class);
			
			//System.out.println(msg.getId()+"  "+msg.getComment()+"  "+msg.isRegulation()+"  "+msg.getConsigne());
			
			for( SlaveModule module : Brasserie.getModules() ) {
				
				 if( module.getId() == msg.getId() ) {
					 
					 Cuve c = (Cuve)module;
					 c.setComment(msg.getComment());
					 c.setRegulation(msg.isRegulation());
					 c.setConsigne(msg.getConsigne());
				 }
			}
			Brasserie.publishSlaves();
		}

	}

}
