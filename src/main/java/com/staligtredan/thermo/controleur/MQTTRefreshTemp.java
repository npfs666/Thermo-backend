package com.staligtredan.thermo.controleur;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.Gson;
import com.staligtredan.onewire.DS18B20;
import com.staligtredan.onewire.DS2480B;
import com.staligtredan.thermo.modele.oneWire.OneWireElement;
import com.staligtredan.thermo.modele.oneWire.TemperatureSensor;

public class MQTTRefreshTemp implements IMqttMessageListener {

	@Override
	public void messageArrived( String topic, MqttMessage message ) throws Exception {
		
		/*
		 * format d'envoi en MQTT
		 * { "address": [ 40, -86, 106, 72, 26, 19, 2, -8 ]}
		 */
		if( (message != null) && (message.getPayload().length > 0) ) {
			
			// get address
			//System.out.println(new String(message.getPayload()));
			String s = new String(message.getPayload()).replaceAll(":\"", ":[");
			s = s.replaceAll("\"}", "]}");
			//System.out.println(s);
			OneWireElement owe = new Gson().fromJson(s, OneWireElement.class);

			System.out.println("refresh temp : "+DS2480B.print(owe.getAddress()));
			Brasserie.publishLog("refresh temp : "+DS2480B.print(owe.getAddress()));

			// Config and convert
			long time = System.currentTimeMillis();
			for (OneWireElement sm : Brasserie.getElements() ) {
				
				if( Arrays.equals(sm.getAddress(), owe.getAddress()) ) {
					
					DS18B20.setResolution(sm.getAddress(), ((TemperatureSensor) sm).getResolution());
					DS18B20.convert(sm.getAddress(), false, null);
				}
			}
			System.out.println("Résol + convert = "+(System.currentTimeMillis()-time)+" ms");
			Brasserie.publishLog("Résol + convert = "+(System.currentTimeMillis()-time)+" ms");
			
			// Wait convert time
			try {
				Thread.sleep(750);
			} catch ( InterruptedException e ) {
				Logger.getLogger(DS2480B.class.getName()).log(Level.WARNING, "Sleep problem");
			}
			
			// read temperature
			for (OneWireElement sm : Brasserie.getElements() ) {
				
				if( Arrays.equals(sm.getAddress(), owe.getAddress()) ) {
					TemperatureSensor ts = (TemperatureSensor) sm;
					double d = DS18B20.readTemp(ts.getAddress()) + ts.getOffset();
					BigDecimal bd = new BigDecimal(d).setScale(2, RoundingMode.HALF_UP);
					ts.setTemp(bd.doubleValue());
				}
			}
		}

		Brasserie.publishOneWireElements();
	}

}
