#!/bin/bash
rm -r -f ./www
rm -r -f ./Thermo-frontend
mkdir www
git clone https://github.com/npfs666/Thermo-frontend.git
mv "Thermo-frontend/build"/* /home/$username/www