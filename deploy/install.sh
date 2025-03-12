#!/bin/bash

# Scripts d'installation pour la thermorégul v2
echo "quel est nom d'utilisateur ?"


# MAJ du système avant tout
sudo apt update && sudo apt upgrade -y
read username


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
sudo apt-get install mosquitto
#sudo cp ./mosquitto.conf /etc/mosquitto/mosquitto.conf
sudo cp ./Thermo-backend/deploy/mosquitto.conf /etc/mosquitto/mosquitto.conf



# Installation de java et du backend
sudo apt install -y openjdk-11-jdk

#git clone https://github.com/npfs666/Thermo-backend.git
cp ./Thermo-backend/target/Thermoregulation2024-0.0.1-SNAPSHOT-jar-with-dependencies.jar /home/$username/thermoregulation.jar 

#sudo cp ./thermoregulation.service /lib/systemd/system/thermoregulation.service
sudo cp ./Thermo-backend/deploy/thermoregulation.service /lib/systemd/system/thermoregulation.service

sudo systemctl daemon-reload
sudo systemctl enable thermoregulation.service
sudo service thermoregulation start



# Installation du serveur WEB
sudo apt install nginx

sudo cp ./Thermo-backend/deploy/default /etc/nginx/sites-enabled/default
mkdir /home/$username/www

# trick pour les droits de nginx
gpasswd -a www-data $username
chmod g+x /home && chmod g+x /home/$username && chmod g+x /home/$username/www

sudo service nginx restart



# Déploiement du frontend WEB
git clone https://github.com/npfs666/Thermo-frontend.git
mv Thermo-frontend/build/* /home/$username/www
#tar -xzf thermo-frontend.tar.gz -C /home/pi/www