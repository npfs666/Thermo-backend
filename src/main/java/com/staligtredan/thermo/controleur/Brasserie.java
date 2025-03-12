package com.staligtredan.thermo.controleur;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.google.gson.Gson;
import com.staligtredan.onewire.DS18B20;
import com.staligtredan.onewire.DS2480B;
import com.staligtredan.thermo.modele.OneWireMaster;
import com.staligtredan.thermo.modele.oneWire.OneWireElement;
import com.staligtredan.thermo.modele.oneWire.PowerSwitch;
import com.staligtredan.thermo.modele.oneWire.TemperatureSensor;
import com.staligtredan.thermo.modele.slaves.Cuve;
import com.staligtredan.thermo.modele.slaves.SlaveModule;
import com.staligtredan.thermo.util.Log;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.XppDriver;

public class Brasserie {
	
	/**
	 * Nom par défaut du fichier de configuration
	 */
	private static final String configPath = "./config.xml";

	/**
	 * Thread de régulation
	 */
	private static ScheduledExecutorService execService;
	
	private static OneWireMaster master;
	
	private static MqttClient client;
	

	
	public Brasserie() {
		
		DS2480B.openPort("/dev/ttyAMA0", 9600);
		
		master = new OneWireMaster();
		
		// Charge le fichier config.xml
		loadData();

		// Lance un process de régulation toute les 10sec
		execService = Executors.newScheduledThreadPool(1);
		execService.scheduleAtFixedRate(() -> {
			regulation();
		}, 1, 10, TimeUnit.SECONDS);
		
		try {
			client = new MqttClient("tcp://localhost:1883", // URI
					MqttClient.generateClientId(), // ClientId
					new MemoryPersistence());// Persistence

			MqttConnectOptions options = new MqttConnectOptions();
			options.setUserName("username");
			options.setPassword("password".toCharArray());
			client.connect(options);
			client.subscribe("onewire/refreshList", new MQTTList1WireAddr());
			client.subscribe("onewire/refreshTemp", new MQTTRefreshTemp());
			client.subscribe("onewire/setPio", new MQTTSetSwitch());
			client.subscribe("onewire/setOWElement", new MQTTSetOWElement());
			client.subscribe("slave/add", new MQTTAddSlave());
			client.subscribe("slave/setSlave", new MQTTSetSlave());
			client.subscribe("slave/setControl", new MQTTSetControl());

		} catch ( MqttException e ) {
			e.printStackTrace();
		}
		   
		publishOneWireElements();
		
		// Thread qui attends la fin d'execution pour se lancer (permet de tout fermer clean)
		// Fonctionne très bien avec le ctrl+c d'une console, à essayer sur un service
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {

				if( execService == null )
					return;
				
				execService.shutdown();
				Brasserie.saveData();

				// Attente de la fin du Thread pour éviter les erreurs de config.xml ou autre
				// à voir si on obtient plus d'erreur par la suite (mis en place avant prod 0)
				try {
					execService.awaitTermination(5, TimeUnit.SECONDS);
				} catch ( InterruptedException e ) {
					//Log.log(Level.INFO, e.toString());
				}
				
				System.out.println("In shutdown hook");
				try {
					client.disconnect();
					client.close();
				} catch ( MqttException e ) {
					e.printStackTrace();
				}
				DS2480B.close();
				
			}
		}, "Shutdown-thread"));
	}
	
	/**
	 * Cycle de régulation interne (class en daemon)
	 */
	private  void regulation() {		
		
		/**
		 * ballade dans la liste des slave et gérer en fonction du type défini
		 * */
		synchronized ( master ) {
			
			// 1 : convertion de T°C sur toutes les sondes (750ms)
			DS18B20.convert(null, false, null);
			try {
				Thread.sleep(750);
			} catch ( InterruptedException e ) {
				Logger.getLogger(DS2480B.class.getName()).log(Level.WARNING, "Sleep problem");
			}
			
			// 2 : lire toutes les sondes de T°C
			for ( OneWireElement sm : Brasserie.getElements() ) {
				
				if( sm.getClass().equals(TemperatureSensor.class) ) {
					
					((TemperatureSensor) sm).readTemperature();
				}
			}
			
			// 3 : réguler les cuves
			boolean atLeastOne = false;
			ArrayList<PowerSwitch> circulateurList = new ArrayList<>();
			
			for ( SlaveModule sm : master.getModules() ) {

				if( sm.getType() == Cuve.typeCuve ) {

					Cuve c = (Cuve) sm;
					
					if( c.isRegulation() ) {
						
						// Allumer si T°C >= consigne
						if( c.getDs18b20().getTemp() >= c.getConsigne() ) {
							c.setActive(true);
							atLeastOne = true;
							circulateurList.add(c.getCirculateur());
						} 
						// Eteindre si T°C + hyst < consigne
						else if ( (c.getDs18b20().getTemp() + OneWireMaster.tempHysteresis) < c.getConsigne() ) {
							c.setActive(false);
						}
						
						if( c.isActive() ) {
							atLeastOne = true;
							circulateurList.add(c.getCirculateur());
						}
					} 
					else {
						c.setActive(false);
					}
				}
			}
			
			// 3bis : faire une pause avant de lancer le circulateur si au moins une pompe est active
			// 4 : lancer la liste de "circulateurs" crée au dessus
			// faut éclaircir la structure des données ici (pas vraiment de slave module circulateur
			// on peut aussi juste parcourir la liste est allumer tous les circulateurs
			if( atLeastOne ) {
				
				// Tempo avant la mise en route du circulateur pour attendre ques les vannes soient ouvertes
				try {
					Thread.sleep(3000);
				} catch ( InterruptedException e ) {
					Logger.getLogger(DS2480B.class.getName()).log(Level.WARNING, "Sleep problem");
				}
				
				// Allumer circulateur
				for ( SlaveModule sm : master.getModules() ) {

					if( sm.getType() == Cuve.typeCuve ) {
						
						Cuve c = (Cuve) sm;
						
						c.getCirculateur().setPioB(true);
						break;
					}
				}
				
			} else {
				
				// éteindre circulateur
				for ( SlaveModule sm : master.getModules() ) {

					if( sm.getType() == Cuve.typeCuve ) {
						
						Cuve c = (Cuve) sm;
						
						c.getCirculateur().setPioB(false);
						break;
					}
				}
			}

			// 5 : envoyer les nouvelles valeurs sur le MQTT
			publishOneWireElements();
		}
	}


	public static ArrayList<SlaveModule> getModules() {
		return master.getModules();
	}
	
	public static ArrayList<OneWireElement> getElements() {
		return master.getElts();
	}
	
	public static OneWireElement oneWireElementExist(byte[] address) {
		
		for( OneWireElement elt : Brasserie.getElements() ) {
			
			if( Arrays.equals(elt.getAddress(), address) ) {
				return elt;
			}
		}
		return null;
	}
	

	public static void publishOneWireElements() {
		
		MqttMessage reqMessage = new MqttMessage(new Gson().toJson(Brasserie.getElements()).getBytes());
		reqMessage.setRetained(true);
		reqMessage.setQos(2);
		try {
			client.publish("onewire/elements", reqMessage);
		} catch ( MqttPersistenceException e ) {
			e.printStackTrace();
		} catch ( MqttException e ) {
			e.printStackTrace();
		}
		publishSlaves();
	}
	
	
	public static void publishLog(String log) {
		
		MqttMessage reqMessage = new MqttMessage(new Gson().toJson(log).getBytes());
		reqMessage.setRetained(false);
		reqMessage.setQos(2);
		
		try {
			client.publish("onewire/console", reqMessage);
		} catch ( MqttPersistenceException e ) {
			e.printStackTrace();
		} catch ( MqttException e ) {
			e.printStackTrace();
		}
	}
	
	public static void publishSlaves() {
		
		MqttMessage reqMessage = new MqttMessage(new Gson().toJson(Brasserie.getModules()).getBytes());
		reqMessage.setRetained(true);
		reqMessage.setQos(2);
		try {
			client.publish("slave/elements", reqMessage);
		} catch ( MqttPersistenceException e ) {
			e.printStackTrace();
		} catch ( MqttException e ) {
			e.printStackTrace();
		}
	}
	 
	
	public void loadData() {

		try {			
			XStream xstream = new XStream(new XppDriver());
			Class<?>[] classes = new Class[] { OneWireMaster.class, TemperatureSensor.class, PowerSwitch.class, Cuve.class };
			xstream.allowTypes(classes);
			
			// Définition des Alias
			xstream.alias("brasserie", OneWireMaster.class);
			xstream.alias("ds18b20", TemperatureSensor.class);
			xstream.alias("ds2413", PowerSwitch.class);
			xstream.alias("cuve", Cuve.class);
			
			FileInputStream fis = null;
			File fichier = new File(configPath);
			
			// Si le fichier n'existe pas on le crée pour éviter les erreurs
			if( !fichier.canRead() ) {
				return;
			}
			
			InputStreamReader isr = new InputStreamReader(new FileInputStream(fichier), "UTF-8");

			// Lecture du fichier XML
			try {
				OneWireMaster tmp = (OneWireMaster) xstream.fromXML(isr);

				master.setElts(tmp.getElts());
				master.setModules(tmp.getModules());
			} finally {
				if( fis != null )
					isr.close();
			}

		} catch ( UnsupportedEncodingException e ) {
			Log.log(Level.INFO, e.toString());
		} catch ( FileNotFoundException e ) {
			Log.log(Level.INFO, e.toString());
		} catch ( IOException e ) {
			Log.log(Level.INFO, e.toString());
		}
	}
	
	
	
	
	// Sauvegarde les données en XML
	public synchronized static void saveData() {

		try {
			XStream xstream = new XStream(new XppDriver());
			Class<?>[] classes = new Class[] { OneWireMaster.class, TemperatureSensor.class, PowerSwitch.class, Cuve.class };
			xstream.allowTypes(classes);

			// Définition des Alias
			xstream.alias("brasserie", OneWireMaster.class);
			xstream.alias("ds18b20", TemperatureSensor.class);
			xstream.alias("ds2413", PowerSwitch.class);
			xstream.alias("cuve", Cuve.class);

			// Ouverture du fichier avec buffer
			File fichier = new File(configPath);

			// Si le fichier n'existe pas on le crée pour éviter les erreurs
			if( !fichier.canWrite() ) {
				fichier.createNewFile();
			}

			OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(fichier), "UTF-8");

			// Enregistrement des données
			try {
				xstream.toXML(master, osw);
			} finally {
				osw.close();
			}
		} catch ( UnsupportedEncodingException e ) {
			Log.log(Level.INFO, e.toString());
		} catch ( FileNotFoundException e ) {
			Log.log(Level.INFO, e.toString());
		} catch ( IOException e ) {
			Log.log(Level.INFO, e.toString());
		}

	}
	
	
	// Unit testing
	public static void main( String[] args ) throws Exception {
		
		
		new Brasserie();
		
		//String s = new String("{\"id\":\"0\",\"name\":\"\",\"thermometer\":\"[40,-86,57,70,26,19,2,54]\",\"valve\":\"[58,-126,-9,65,0,0,0,-125]\",\"pump\":\"[58,-126,-9,65,0,0,0,-125]\"}");
		String s = "123";
		
		MqttMessage msg = new MqttMessage(s.getBytes());
		new MQTTAddSlave().messageArrived("", msg);
		new MQTTAddSlave().messageArrived("", msg);
		
		Brasserie.saveData();
	}
}
