/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.turnimator.fide.events;

import com.turnimator.fide.ConnectionId;

/**
 *
 * @author atle
 */
public abstract class UploadEvent {
    public abstract void upload(ConnectionId id, String text);
}
