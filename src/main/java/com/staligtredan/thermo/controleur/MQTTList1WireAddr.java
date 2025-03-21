package com.staligtredan.thermo.controleur;

import java.util.logging.Level;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.staligtredan.onewire.DS18B20;
import com.staligtredan.onewire.DS2413;
import com.staligtredan.onewire.DS2480B;
import com.staligtredan.thermo.modele.oneWire.OneWireElement;
import com.staligtredan.thermo.modele.oneWire.PowerSwitch;
import com.staligtredan.thermo.modele.oneWire.TemperatureSensor;

public class MQTTList1WireAddr implements IMqttMessageListener {

	@Override
	public void messageArrived( String topic, MqttMessage message ) throws Exception {
		
		DS2480B.searchROMs();
		//System.out.println("Search 1 wire network");
		Brasserie.publishLog(Level.INFO, "Search 1 wire network");
		
		for( byte[] address : DS2480B.OWList ) {
			
			if( address[0] == DS18B20.familyCode ) {
				
				//System.out.println("T°C : "+address[0]);
				
				// Si la sonde n'est pas déclarée, on la rajoute à la liste
				OneWireElement ow = Brasserie.oneWireElementExist(address);
				if( ow == null ) {
					TemperatureSensor ts = new TemperatureSensor(address);
					Brasserie.getElements().add(ts);
					Brasserie.publishLog(Level.INFO, "Added DS18B20 "+ DS2480B.print(ts.getAddress()));
				}	
				else {
					Brasserie.publishLog(Level.INFO, "Found DS18B20 "+ DS2480B.print(ow.getAddress()));
				}
			}
			else if( address[0] == DS2413.familyCode ) {
				
				//System.out.println("Relais : "+address[0]);
				
				OneWireElement ow = Brasserie.oneWireElementExist(address);
				if( ow == null ) {
					PowerSwitch ts = new PowerSwitch(address);

					Brasserie.getElements().add(ts);
					Brasserie.publishLog(Level.INFO, "Found DS2413 "+ DS2480B.print(ts.getAddress()));
					
				}
				else {
					Brasserie.publishLog(Level.INFO, "Found DS2413 "+ DS2480B.print(ow.getAddress()));
				}
			}			
		}
		
		
		//System.out.println("OWElement found : " + Brasserie.getElements().size());
		Brasserie.publishLog(Level.INFO, "OWElement found : " + DS2480B.OWList.size());
		Brasserie.publishLog(Level.INFO, "OWElement declared : " + Brasserie.getElements().size());
		
		Brasserie.publishOneWireElements();
	}
}
