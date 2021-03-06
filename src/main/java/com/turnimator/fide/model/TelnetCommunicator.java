/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.turnimator.fide.model;

import com.turnimator.fide.events.ProgressEvent;
import com.turnimator.fide.events.ReceiveEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author atle
 */
public class TelnetCommunicator implements CommunicatorInterface {

    Socket _socket = new Socket();

    String _host = "";
    String _port = "23";
    InetSocketAddress _address = null;
    
    String _id;
    String errorText = "";
    ArrayList<String> _ports;

    ArrayList<ReceiveEvent> rcvEventList = new ArrayList<>();

    ArrayList<ProgressEvent> progressEventList = new ArrayList<>();

    public TelnetCommunicator(String host, String port) {
        _host = host;
        _port = port;
        this._ports = new ArrayList<>();
        _ports.add("23");
        _id = "Telnet:" + host + ":" + port;
    }

    public void addProgressEventHandler(ProgressEvent ev) {
        progressEventList.add(ev);
    }

    private void bubbleProgressEvent(int max, int min, int i) {
        for (ProgressEvent ev : progressEventList) {
            ev.progress(max, min, i);
        }
    }

    PrintWriter prw = null;
    BufferedReader brin = null;
    Thread rxThread = null;

    Boolean _stopFlag = false;

    /**
     *
     * @return True if successful, false otherwise
     */
    @Override
    public String connect(String port) {
        _port = port;
        if (_address == null){
            throw new NullPointerException("Address is null. set with setHost()");
        }
        try {
            _socket.connect(_address, Integer.valueOf(_port));
        } catch (IOException ex) {
            errorText = ex.toString();
            Logger.getAnonymousLogger().log(Level.SEVERE, ex.toString());
            return null;
        }
        try {
            prw = new PrintWriter(_socket.getOutputStream());
        } catch (IOException ex) {
            Logger.getAnonymousLogger().log(Level.SEVERE, ex.toString());
            errorText = ex.toString();
            return null;
        }
        try {
            brin = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
        } catch (IOException ex) {
            errorText = ex.toString();
            Logger.getAnonymousLogger().log(Level.SEVERE, ex.toString());
            return null;
        }
        rxThread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (; !_stopFlag;) {
                    String s = "";
                    try {
                        s = brin.readLine();
                    } catch (IOException ex) {
                        Logger.getLogger(TelnetCommunicator.class.getName()).log(Level.SEVERE, null, ex);
                        break;
                    }
                    for (ReceiveEvent evt : rcvEventList) {
                        evt.receive(_id, s);
                    }
                }
                try {
                    _socket.close();
                } catch (IOException ex) {
                    Logger.getLogger(TelnetCommunicator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        });
        rxThread.start();
        errorText = "";
        
        return _id;
    }

    @Override
    public boolean disconnect() {
        _stopFlag = true;
        return true;
    }

    /**
     *
     * @param s
     * @return
     */
    @Override
    public boolean send(final String s) {
        Logger.getAnonymousLogger().log(Level.INFO, "Telnet " + _host + " Sending: " + s);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                if (prw != null) {
                    prw.println(s + "\n"); /// BUG \r\n
                    prw.flush();
                } else {
                    errorText = "Not connected";
                }
            }
        });
        t.start();
        errorText = "Sent";
        try {
            t.join(2000);
        } catch (InterruptedException ex) {
            errorText = ex.getMessage();
            return false;
        }
        return true;
    }

    @Override
    public void addReceiveEventHandler(ReceiveEvent evt) {
        rcvEventList.add(evt);
    }

    @Override
    public boolean isOpen() {
        return _socket.isConnected();
    }

    @Override
    public String getErrorText() {
        return errorText;
    }

    @Override
    public String getId() {
        return _id;
    }

    @Override
    public List<String> getPorts(String host) {
        _ports.clear();
        InetSocketAddress addr;
        for (Integer i = 23; i < 32767; i++) {
            Socket s = new Socket();
            try {
                addr = new InetSocketAddress(host, i);
                s.connect(addr, 20);
                if (s.isConnected()){
                    _ports.add(i.toString());
                    s.close();
                }
            } catch (IOException ex) {
                //Logger.getLogger(TelnetCommunicator.class.getName()).log(Level.SEVERE, null, ex);
            } 
            bubbleProgressEvent(23, i, 32767);
            
        }
        return _ports;
    }

    @Override
    public void setHost(String host) {
        _host = host;
        _address = new InetSocketAddress(host, Integer.valueOf(_port));
    }

    @Override
    public void setPort(String port) {
       _port = port;
       _address = new InetSocketAddress(_host, Integer.valueOf(_port));
    }

    
}
