package com.staligtredan.thermo.controleur;

import java.util.Arrays;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.Gson;
import com.staligtredan.onewire.DS18B20;
import com.staligtredan.onewire.DS2413;
import com.staligtredan.thermo.modele.oneWire.OneWireElement;
import com.staligtredan.thermo.modele.oneWire.PowerSwitch;
import com.staligtredan.thermo.modele.oneWire.TemperatureSensor;

public class MQTTSetOWElement implements IMqttMessageListener {

	@Override
	public void messageArrived( String topic, MqttMessage message ) throws Exception {

		/*
		 * format d'envoi en MQTT { "address": [ 40, -86, 106, 72, 26, 19, 2, -8
		 * ]}
		 */
		if( (message != null) && (message.getPayload().length > 0) ) {
			
			String s = new String(message.getPayload());
			
			//System.out.println(s);	// before
			
			//s = s.replaceAll("\\s+","");		//remove all spaces
			s = s.replaceFirst(":\"", ":[");	// convert adress to byte array
			s = s.replaceFirst("\",", "],");	// follow up
			
			//System.out.println(s);	//after
			
			OneWireElement owe = new Gson().fromJson(s, OneWireElement.class);

			// Delete OneWireElement from the local server database
			if( owe.getName().equals("delete") ) {

				for( int i = 0; i < Brasserie.getElements().size(); i++ ) {
					
					if( Arrays.equals(Brasserie.getElements().get(i).getAddress(), owe.getAddress()) ) {
						Brasserie.getElements().remove(i);
						break;
					}
				}
			}
				
			
			
			else if( owe.getFamily() == DS18B20.familyCode ) {
				
				TemperatureSensor ts = new Gson().fromJson(s, TemperatureSensor.class);
				
				for( int i = 0; i < Brasserie.getElements().size(); i++ ) {
					if( Arrays.equals(Brasserie.getElements().get(i).getAddress(), owe.getAddress()) ) {
						
						TemperatureSensor localTS = (TemperatureSensor) Brasserie.getElements().get(i);
						localTS.setName(ts.getName());
						localTS.setResolution(ts.getResolution());
						localTS.setOffset(ts.getOffset());
					}
				}
			}
			else if ( owe.getFamily() == DS2413.familyCode ) {
				
				PowerSwitch ps = new Gson().fromJson(s, PowerSwitch.class);
				
				for( int i = 0; i < Brasserie.getElements().size(); i++ ) {
					if( Arrays.equals(Brasserie.getElements().get(i).getAddress(), owe.getAddress()) ) {
						
						PowerSwitch localTS = (PowerSwitch) Brasserie.getElements().get(i);
						localTS.setName(ps.getName());
					}
				}
			}
			
			Brasserie.publishOneWireElements();
		}
	}

	
	public static void main( String[] args ) throws Exception {
		String s = new String("{\r\n"
				+ "  \"address\": \"40,-86,106,72,26,19,2,-8\",\r\n"
				+ "  \"name\": \"delete\",\r\n"
				+ "  \"resolution\": \"10\",\r\n"
				+ "  \"offset\": \"-0.9\"\r\n"
				+ "}");
		
		MqttMessage msg = new MqttMessage(s.getBytes());
		new MQTTSetOWElement().messageArrived("", msg);
	}
}
