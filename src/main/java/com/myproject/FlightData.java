package com.myproject;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FlightData {
    @JsonProperty("timestamp")
    private int timestamp;
    @JsonProperty("altitude")
    private double altitude;
    @JsonProperty("accelX")
    private double accelX;
    @JsonProperty("accelY")
    private double accelY;
    @JsonProperty("accelZ")
    private double accelZ;
    @JsonProperty("temperature")
    private double temperature;
    @JsonProperty("event")
    private int event;

    // Default constructor for JSON deserialization
    public FlightData() {}

    // Constructor for saving data
    public FlightData(int timestamp, double altitude, double accelX, double accelY, double accelZ, double temperature, int event) {
        this.timestamp = timestamp;
        this.altitude = altitude;
        this.accelX = accelX;
        this.accelY = accelY;
        this.accelZ = accelZ;
        this.temperature = temperature;
        this.event = event;
    }

    // Getters and Setters
    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getAccelX() {
        return accelX;
    }

    public void setAccelX(double accelX) {
        this.accelX = accelX;
    }

    public double getAccelY() {
        return accelY;
    }

    public void setAccelY(double accelY) {
        this.accelY = accelY;
    }

    public double getAccelZ() {
        return accelZ;
    }

    public void setAccelZ(double accelZ) {
        this.accelZ = accelZ;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getEvent() {
        return event;
    }

    public void setEvent(int event) {
        this.event = event;
    }
}
