/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.turnimator.fide.events;

/**
 *
 * @author atle
 */
public abstract class SerialConnectionRequestEvent {
    
    public abstract void connect(String serialPort, int bitRate);
}
