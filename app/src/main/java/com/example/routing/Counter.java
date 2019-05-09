package com.example.routing;

public class Counter {
    Integer count;
    public boolean isLocked = false;
    public Counter(){
        this.count = 0;
    }
    public void reset(){
        this.count = 0;
    }

    public void add (Integer count){
        this.count+=count;
    }

    public synchronized void lock()
            throws InterruptedException{
        while(isLocked){
            wait();
        }
        isLocked = true;
    }

    public synchronized void unlock(){
        isLocked = false;
        notify();
    }

}
