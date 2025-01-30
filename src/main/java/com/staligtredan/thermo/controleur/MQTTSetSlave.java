package com.staligtredan.thermo.controleur;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.Gson;
import com.staligtredan.thermo.modele.oneWire.PowerSwitch;
import com.staligtredan.thermo.modele.oneWire.TemperatureSensor;
import com.staligtredan.thermo.modele.slaves.Cuve;
import com.staligtredan.thermo.modele.slaves.SlaveModule;

public class MQTTSetSlave implements IMqttMessageListener {

	@Override
	public void messageArrived( String topic, MqttMessage message ) throws Exception {

		
		if( (message != null) && (message.getPayload().length > 0) ) {

			String s = new String(message.getPayload());

			// Préparation du message pour convertir les String en byte[]
			s = s.replaceAll("\"\\[", "[");	// convert adress to byte array
			s = s.replaceAll("\\]\"", "]");	// follow up

			SlaveMessage msg = new Gson().fromJson(s, SlaveMessage.class);
			
			if( msg.name.equals("delete") ) {
				
				for( int i =0; i < Brasserie.getModules().size(); i++) {
					
					if( Brasserie.getModules().get(i).getId() == msg.id ) {
						Brasserie.getModules().remove(i);
						
						Brasserie.publishSlaves();
						
						return;
					}
				}
			}
			
			/* Enlevable, puisque l'autre technique via objet fonctionne bien
			// Clean pour extraire le reste des adresses
			s = s.replaceAll("\\{", "");
			s = s.replaceAll("\\}", "");
			s = s.replaceAll("\",\"", "\";\"");
			
			System.out.println(s);
			String[] list = s.split(";");
			
			byte[] thermometerAddr = Utils.strToByteArray(list[2].split(":")[1].replaceAll("\"+", ""));
			byte[] valveAddr = Utils.strToByteArray(list[3].split(":")[1].replaceAll("\"+", ""));
			byte[] pumpAddr = Utils.strToByteArray(list[4].split(":")[1].replaceAll("\"+", ""));
			
			DS2480B.print(thermometerAddr);
			DS2480B.print(valveAddr);
			DS2480B.print(pumpAddr);
			//Byte[] thermometre = list[2].split(":")[1]*/
					
			// On parcours la liste des esclaves pour trouver le bon et mettre à jour
			for( SlaveModule module : Brasserie.getModules() ) {
				
				 if( module.getId() == msg.id ) {
					 
					 // MAJ du module esclave
					 module.setDs18b20((TemperatureSensor)Brasserie.oneWireElementExist(msg.thermometer));
					 module.setDs2413((PowerSwitch)Brasserie.oneWireElementExist(msg.valve));
					 
					 // Si Cuve MAJ cuve
					 if( module.getType() == SlaveModule.typeCuve ) {
						 
						 Cuve c = (Cuve) module;
						 c.setCirculateur((PowerSwitch)Brasserie.oneWireElementExist(msg.pump));
						 c.setNomCommun(msg.name);
					 }
				 }
			}
			Brasserie.publishSlaves();
		}
	}

	/**
	 * Classe qui copie le type de message MQTT envoyé par le client.
	 */
	public class SlaveMessage {
		
		public int id;
		public String name;
		public byte[] thermometer;
		public byte[] valve;
		public byte[] pump;
	}

	// Unit testing
	public static void main( String[] args ) throws Exception {
		
		//String s = new String("{\"id\":\"0\",\"name\":\"\",\"thermometer\":\"40,-86,57,70,26,19,2,54\",\"valve\":\"58,-126,-9,65,0,0,0,-125\",\"pump\":\"58,-5,15,66,0,0,0,-99\"}");
		String s = new String("{\"id\":\"0\",\"name\":\"\",\"thermometer\":\"[40,-86,57,70,26,19,2,54]\",\"valve\":\"[58,-126,-9,65,0,0,0,-125]\",\"pump\":\"[58,-126,-9,65,0,0,0,-125]\"}");
		
		MqttMessage msg = new MqttMessage(s.getBytes());
		new MQTTSetSlave().messageArrived("", msg);
	}
}
