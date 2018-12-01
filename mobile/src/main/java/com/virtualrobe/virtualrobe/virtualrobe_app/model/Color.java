package com.virtualrobe.virtualrobe.virtualrobe_app.model;


public class Color {
    private String hex;
    private String name;

    public Color(){
    }

    public Color(String hex, String name){
        this.hex = hex;
        this.name = name;
    }

    public String getHex(){return hex;}
    public void  setHex(String hex){
        this.hex = hex;
    }

    public String getName(){return name;}
    public void setName(String name){
        this.name = name;
    }
}
