package com.virtualrobe.virtualrobe.virtualrobe_app.model;


public class Clothes{
    private String Brand;
    private String color;
    private String description;
    private String full_image;
    private String thumbnail;
    private int laundry_status;
    private boolean isSelected;

    public Clothes(String full_image,String thumbnail,String description,String color, String brand, int laundry_status){
        this.full_image = full_image;
        this.thumbnail = thumbnail;
        this.description = description;
        this.color = color;
        this.Brand = brand;
        this.laundry_status = laundry_status;
    }
    public Clothes(){}

    public String getBrand(){return Brand;}
    public void setBrand(String Brand){this.Brand = Brand;}

    public String getColor(){return color;}
    public void setColor(String color){this.color = color;}

    public String getDescription(){return description;}
    public void setDescription(String description){this.description = description;}

    public String getFull_image(){return  full_image;}
    public void  setFull_image(String full_image){this.full_image = full_image;}

    public String getThumbnail(){return thumbnail;}
    public void  setThumbnail(String thumbnail){this.thumbnail = thumbnail;}

    public int getLaundry_status(){return laundry_status;}
    public void setLaundry_status(int status){laundry_status = status;}

    public boolean getSelected() {
        return isSelected;
    }
    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}