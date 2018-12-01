package com.virtualrobe.virtualrobe.virtualrobe_app.model;

/**
 * Created by seunk on 1/27/2018.
 */

public class Shopping_items {
    private String item_name;
    private String Description;
    private String brand_name;
    private String Color;
    private String image;
    private String image_thumbnail;
    private String price;
    private String merhant_id;
    private String merchant_store;
    private int total_available;

    public Shopping_items(String item_name,String Description,String brand_name,String Color,String image,
                          String image_thumbnail,String price,String merhant_id,String merchant_store,int total_available){
        this.item_name = item_name;
        this.Description = Description;
        this.brand_name = brand_name;
        this.Color = Color;
        this.image = image;
        this.image_thumbnail = image_thumbnail;
        this.price = price;
        this.merhant_id = merhant_id;
        this.merchant_store = merchant_store;
        this.total_available = total_available;
    }

    public Shopping_items(){}

    public String getItem_name(){return item_name;}
    public void  setItem_name(String item_name){this.item_name = item_name;}

    public String getDescription(){return Description;}
    public void setDescription(String Description){this.Description = Description;}

    public String getBrand_name(){return brand_name;}
    public void setBrand_name(String brand_name){this.brand_name = brand_name;}

    public String getColor(){return Color;}
    public void setColor(String Color){this.Color = Color;}

    public String getImage(){return image;}
    public void setImage(String image){this.image = image;}

    public String getImage_thumbnail(){return image_thumbnail;}
    public void setImage_thumbnail(String image_thumbnail){this.image_thumbnail = image_thumbnail;}

    public String getPrice(){return price;}
    public void setPrice(String price){this.price = price;}

    public String getMerhant_id(){return merhant_id;}
    public void setMerhant_id(String merhant_id){this.merhant_id = merhant_id;}

    public String getMerchant_store(){return merchant_store;}
    public void setMerchant_store(String merchant_store){this.merchant_store = merchant_store;}

    public int getTotal_available(){return total_available;}
    public void setTotal_available(int total_available){this.total_available = total_available;}
}
