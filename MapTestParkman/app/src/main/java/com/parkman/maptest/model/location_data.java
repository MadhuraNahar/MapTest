package com.parkman.maptest.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DELL on 9/21/2017.
 */
public class location_data {

    public bounds bounds;
    public ArrayList<zones> zones;

    public location_data.bounds getBounds() {
        return bounds;
    }

    public void setBounds(location_data.bounds bounds) {
        this.bounds = bounds;
    }

    public ArrayList<zones> getZones() {
        return zones;
    }

    public void setZones(ArrayList<zones> zones) {
        this.zones = zones;
    }

    public class bounds{
        String north;
        String south;
        String west;
        String east;

        public String getNorth() {
            return north;
        }

        public void setNorth(String north) {
            this.north = north;
        }

        public String getSouth() {
            return south;
        }

        public void setSouth(String south) {
            this.south = south;
        }

        public String getWest() {
            return west;
        }

        public void setWest(String west) {
            this.west = west;
        }

        public String getEast() {
            return east;
        }

        public void setEast(String east) {
            this.east = east;
        }
    }
}
