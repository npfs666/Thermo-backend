package com.staligtredan.thermo.controleur;

import java.util.Arrays;
import java.util.logging.Level;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.Gson;
import com.staligtredan.onewire.DS2480B;
import com.staligtredan.thermo.modele.oneWire.OneWireElement;
import com.staligtredan.thermo.modele.oneWire.PowerSwitch;

public class MQTTSetSwitch implements IMqttMessageListener {

	@Override
	public void messageArrived( String topic, MqttMessage message ) throws Exception {
		
		if( (message != null) && (message.getPayload().length > 0) ) {
			
			// get address // clean string or byte[]
			String s = new String(message.getPayload()).replaceAll(":\"", ":[");
			s = s.replaceAll("\",", "],");

			PowerSwitch owe = new Gson().fromJson(s, PowerSwitch.class);
			//System.out.println("SET PIO ("+DS2480B.print(owe.getAddress())+") PioA:"+owe.isPioA()+" PioB: "+owe.isPioB());
			Brasserie.publishLog(Level.INFO, "SET PIO ("+DS2480B.print(owe.getAddress())+") PioA:"+owe.isPioA()+" PioB: "+owe.isPioB());
			
			// set local model
			for (OneWireElement sm : Brasserie.getElements() ) {
				if( Arrays.equals(sm.getAddress(), owe.getAddress()) ) {
					((PowerSwitch)sm).setPioA(owe.isPioA());
					((PowerSwitch)sm).setPioB(owe.isPioB());
				}
			}
			
			// update hardware
			//DS2413.setOutputs(owe.getAddress(), owe.isPioA(), owe.isPioB());
		}
		
		Brasserie.publishOneWireElements();
	}

}
