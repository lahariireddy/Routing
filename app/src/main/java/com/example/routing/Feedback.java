package com.example.routing;

public class Feedback {
    public long timestamp;
    public String origin;
    public String originLatLng;
    public String dest;
    public String destLatLng;
    public String waypoints;
    public int Q1;
    public boolean Q2, Q3, Q4, Q5;

    public Feedback(){
        this.Q1 = 5;
        this.Q2 = false;
        this.Q3 = false;
        this.Q4 = false;
        this.Q5 = false;
    }

    public Feedback(long timestamp, String or, String originLatLng, String de, String deLatLng, String waypoints, int Q1, boolean Q2, boolean Q3, boolean Q4, boolean Q5){
        this.timestamp = timestamp;
        this.origin = or;
        this.originLatLng = originLatLng;
        this.dest = de;
        this.destLatLng = deLatLng;
        this.waypoints = waypoints;
        this.Q1 = Q1;
        this.Q2 = Q2;
        this.Q3 = Q3;
        this.Q4 = Q4;
        this.Q5 = Q5;
    }
}
