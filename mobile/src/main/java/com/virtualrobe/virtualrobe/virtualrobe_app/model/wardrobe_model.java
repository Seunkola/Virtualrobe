package com.virtualrobe.virtualrobe.virtualrobe_app.model;

public class wardrobe_model {
    private String image_name;
    private String image;

    public wardrobe_model(){}

    public wardrobe_model(String image,String image_name){
        this.image = image;
        this.image_name = image_name;
    }

    public String getImage_name(){return  image_name;}

    public void  setImage_name(String name){image_name = name;}

    public  String getImage(){return  image;}

    public void  setImage(String image){this.image = image;}
}
