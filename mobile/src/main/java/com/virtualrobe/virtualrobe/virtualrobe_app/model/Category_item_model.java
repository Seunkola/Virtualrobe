package com.virtualrobe.virtualrobe.virtualrobe_app.model;


public class Category_item_model {
    private String brand;
    private String color;
    private String description;
    private String full_image;
    private int laundry_status;
    private String size;
    private String thumbnail;

    public Category_item_model(){}

    public Category_item_model(String full_image,String thumbnail, String description,
                               String brand, String color, String size, int laundry_status){
        this.brand = brand;
        this.thumbnail = thumbnail;
        this.description = description;
        this.full_image = full_image;
        this.color = color;
        this.size = size;
        this.laundry_status = laundry_status;
    }

    public String getFull_image(){return full_image;}
    public void setFull_image(String image){full_image = image;}

    public String getThumbnail(){return  thumbnail;}
    public void  setThumbnail(String image){thumbnail = image;}

    public String getDescription(){return  description;}
    public  void  setDescription(String description){this.description = description;}

    public String getBrand(){return brand;}
    public void  setBrand(String brand){this.brand = brand;}

    public String getColor(){return color;}
    public void  setColor(String color){this.color = color;}

    public int getLaundry_status(){return laundry_status;}
    public void setLaundry_status(int status){laundry_status = status;}

    public void setSize(String size) {
        this.size = size;
    }
    public String getSize(){return size;}
}
