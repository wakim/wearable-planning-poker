#!/bin/sh

./gradlew assembleRelease

my_path=$(pwd)/
me=$(whoami)

echo -n "Entre com o nome do keystore em /home/$me/.android: "
read -r keystore_location

echo -n "Entre com o alias_name do certificado: "
read -r alias_name


echo -n "Entre com o nome do modulo default: "
read -r project_directory

apk_location=$my_path$project_directory/build/outputs/apk/$project_directory-release-unsigned.apk
apk_location_aligned=$my_path$project_directory/build/outputs/apk/$project_directory-release.apk
keystore_location=/home/$me/.android/$keystore_location.jks

echo 'jarsigner'

jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore "$keystore_location" "$apk_location" $alias_name
echo 'jarsigner finalizado'

/home/$me/android-studio/sdk/build-tools/20.0.0/zipalign -v 4 "$apk_location" "$apk_location_aligned"
echo 'zipalign finalizado'
