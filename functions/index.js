const functions = require("firebase-functions");
const admin = require('firebase-admin');
// let serviceAccount = require('D:\\AAATrinity\\Urban Computing\\project3\\serviceAccountKey.json');

// admin.initializeApp({
    // credential: admin.credential.cert(serviceAccount),
    // databaseURL: "https://urban-computing-app-3e91a-default-rtdb.firebaseio.com"
// });

admin.initializeApp(functions.config().firebase);

exports.checkPlantSuitability = functions.https.onCall((data, context) => {
    const plantData = admin.database().ref("plant_collection/"+data.plantId);
    return plantData.once('value').then(plantSnap => {
        const sensorData = admin.database().ref("SensorData").orderByChild("userId").equalTo(data.userId).limitToLast(1);
        return sensorData.once('value').then(sensorSnap => {
            const locationData = admin.database().ref("LocationData").orderByChild("userId").equalTo(data.userId).limitToLast(1);
            return locationData.once('value').then(locationSnap => {
                let result = {};
                plant = plantSnap.val();
                sensorSnap.forEach(function(sensor) {
                    ambientLight = parseInt(sensor.val().ambientLight); 
                    humidity = sensor.val().humidity; 
                    temperature = parseInt(sensor.val().temperature); 

                    locationSnap.forEach(function(location) {
                        if((ambientLight <= plant.maxLight) && (ambientLight >= plant.minLight)){
                            result["lightResult"]="true";
                            result["lightText"] = "Light"+"("+parseInt(ambientLight)+" Lux)" + " is in suitable for " + plant.name + "(" + plant.minLight + " to " + plant.maxLight + " Lux).";
                        } else {
                            result["lightResult"]="false";
                            result["lightText"] = "Light"+"("+parseInt(ambientLight)+" Lux)" + " is not suitable for " + plant.name + "(" + plant.minLight + " to " + plant.maxLight + " Lux).";
                        }
                        if(ambientLight < 800){
                            //take sensor readings
                            if((humidity < plant.maxHumidity) && (humidity > plant.minHumidity)){
                                result["humidityResult"]="true";
                                result["humidityText"] = "Humidity"+"("+humidity+"%)" + " is in suitable for " + plant.name + "(" + plant.minHumidity + "% to " + plant.maxHumidity + "%).";
                            } else {
                                result["humidityResult"]="false";
                                result["humidityText"] = "Humidity"+"("+humidity+"%)" + " is not suitable for " + plant.name + "(" + plant.minHumidity + "% to " + plant.maxHumidity + "%).";
                            }
                            if((temperature <= plant.maxTemp) && (temperature >= plant.minTemp)){
                                result["tempResult"]="true";
                                result["tempText"] = "Temperature"+"("+temperature+"C)" + " is in suitable for " + plant.name + "(" + plant.minTemp + "C to " + plant.maxTemp + "C).";
                            } else {
                                result["tempResult"]="false";
                                result["tempText"] = "Temperature"+"("+temperature+"C)" + " is not suitable for " + plant.name + "(" + plant.minTemp + "C to " + plant.maxTemp + "C).";
                            }
                        } else {
                            locationHumidity = location.val().locationHumidity; 
                            locationTemperature = parseInt(location.val().locationTemperature); 
                            if((locationHumidity < plant.maxHumidity) && (locationHumidity > plant.minHumidity)){
                                result["humidityResult"]="true";
                                result["humidityText"] = "Humidity"+"("+locationHumidity+"%)" + " is in suitable for " + plant.name + "(" + plant.minHumidity + "% to " + plant.maxHumidity + "%).";
                            } else {
                                result["humidityResult"]="false";
                                result["humidityText"] = "Humidity"+"("+locationHumidity+"%)" + " is not suitable for " + plant.name + "(" + plant.minHumidity + "% to " + plant.maxHumidity + "%).";
                            }
                            if((locationTemperature <= plant.maxTemp) && (locationTemperature >= plant.minTemp)){
                                result["tempResult"]="true";
                                result["tempText"] = "Temperature"+"("+locationTemperature+"C)" + " is in suitable for " + plant.name + "(" + plant.minTemp + "C to " + plant.maxTemp + "C).";
                            } else {
                                result["tempResult"]="false";
                                result["tempText"] = "Temperature"+"("+locationTemperature+"C)" + " is not suitable for " + plant.name + "(" + plant.minTemp + "C to " + plant.maxTemp + "C).";
                            }
                        }
                    });
                });
                return result;
            });
        });
    });
});



