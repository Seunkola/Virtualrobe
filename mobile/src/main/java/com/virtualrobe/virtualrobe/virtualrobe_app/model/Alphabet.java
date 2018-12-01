package com.virtualrobe.virtualrobe.virtualrobe_app.model;

public class Alphabet {
    private String Character;

    public Alphabet(){

    }

    public Alphabet(String character){
        Character = character;
    }

    public String getCharacter(){
        return Character;
    }

    public void setCharacter (String Character){this.Character = Character;}
}
