#!/bin/bash

# Scripts d'installation pour la thermorégul v2
read -p 'Username: ' username


# MAJ du système avant tout
sudo apt update && sudo apt upgrade -y


# Installation de l'UPS LiFePO4weredPi
sudo apt-get -y install build-essential git libsystemd-dev

git clone https://github.com/xorbit/LiFePO4wered-Pi.git
cd LiFePO4wered-Pi/
make all
sudo make user-install
cd ..

lifepo4wered-cli set auto_boot 4 #reboot auto (sauf en cas d'arret manuel)
lifepo4wered-cli set AUTO_SHDN_TIME 10 #shut down après 10minutes sans tension
lifepo4wered-cli set PI_SHDN_TO 50 #éteinds l'alimention après 50sec (120sec par défaut)



# Installation du broker MQTT
sudo apt-get install mosquitto -y
#sudo cp ./mosquitto.conf /etc/mosquitto/mosquitto.conf
sudo cp ./Thermo-backend/deploy/mosquitto.conf /etc/mosquitto/mosquitto.conf



# Installation de java et du backend
sudo apt install -y openjdk-17-jdk -y

#git clone https://github.com/npfs666/Thermo-backend.git
cp ./Thermo-backend/target/Thermoregulation2024-0.0.1-SNAPSHOT-jar-with-dependencies.jar /home/$username/thermoregulation.jar 

echo "[Unit]
Description=Thermoregulation
Wants=network-online.target
After=network-online.target
 
[Service]
Type=simple
 
User=$username
Group=$username
WorkingDirectory=/home/$username/
 
ExecStart=/usr/bin/java -jar /home/$username/thermoregulation.jar
 
Restart=on-failure
 
[Install]
WantedBy=multi-user.target" > ./thermoregulation.service

sudo mv ./thermoregulation.service /lib/systemd/system/thermoregulation.service


sudo systemctl daemon-reload
sudo systemctl enable thermoregulation.service
sudo service thermoregulation start



# Installation du serveur WEB
sudo apt install nginx -y

# On change le dossier www/ vers le /home
sudo sed -i "s@root /var/www/html;@root /home/$username/www;@" /etc/nginx/sites-enabled/default

# MAJ des règles pour que le react-router fonctionne
sudo sed -i "s@try_files $uri $uri/ =404;@try_files $uri /index.html;@" /etc/nginx/sites-enabled/default

mkdir /home/$username/www

# trick pour les droits de nginx
sudo gpasswd -a www-data $username
sudo chmod g+x /home && chmod g+x /home/$username && chmod g+x /home/$username/www

sudo service nginx restart



# Déploiement du frontend WEB
git clone https://github.com/npfs666/Thermo-frontend.git
mv "Thermo-frontend/build"/* /home/$username/www