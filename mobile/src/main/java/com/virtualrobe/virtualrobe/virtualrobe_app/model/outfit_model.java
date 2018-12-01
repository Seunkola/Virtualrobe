package com.virtualrobe.virtualrobe.virtualrobe_app.model;


public class outfit_model {
    private String username;
    private String datetime;
    private String image;
    private String image_thumbnailbg;
    private String image_thumbnailsm;
    private String describe;
    private String calendar;
    private String calendar_date;
    private int newsfeed_time;
    private int numratings;
    private int gallery;
    private double avgRating;
    private String user_profileimg;
    private String user_profileimg_thumb;

    public outfit_model(){

    }

    public outfit_model( String username, String datetime, String image, String image_thumbnailbg, String image_thumbnailsm,
                   String describe,String calendar, String calendar_date, int newsfeed_time,int numratings,int gallery){
        this.username = username;
        this.datetime = datetime;
        this.image = image;
        this.image_thumbnailbg = image_thumbnailbg;
        this.image_thumbnailsm = image_thumbnailsm;
        this.describe = describe;
        this.calendar = calendar;
        this.calendar_date = calendar_date;
        this.newsfeed_time = newsfeed_time;
        this.numratings = numratings;
        this.gallery = gallery;
    }

    public String getdate() {
        return datetime;
    }

    public void setdate(String datetime){
        this.datetime=datetime;
    }

    public String getuser() {
        return username;
    }

    public void setuser(String username){
        this.username=username;
    }

    public String getUser_profileimg() {
        return user_profileimg;
    }

    public void setUser_profileimg(String image){
        this.user_profileimg=image;
    }

    public String getUser_profileimg_thumb() {
        return user_profileimg;
    }

    public void setUser_profileimg_thumb(String image){
        this.user_profileimg=image;
    }

    public String getImage(){return image;}

    public void setImage(String image){
        this.image=image;
    }

    public String getImage_thumbnailbg(){return image_thumbnailbg;}

    public void setImage_thumbnailbg(String image){
        image_thumbnailbg = image;
    }

    public String getImage_thumbnailsm(){return image_thumbnailsm;}

    public void setImage_thumbnailsm(String image){
        image_thumbnailsm = image;
    }

    public String getDescribe(){return describe;}

    public void setDescribe(String describe){
        this.describe = describe;
    }

    public String getCalendar(){return calendar;}

    public void setCalendar(String calendar){
        this.calendar = calendar;
    }

    public String getCalendar_date(){return calendar_date;}

    public void setCalendar_date(String calendar_date){
        this.calendar_date = calendar_date;
    }

    public int getNewsfeed_time(){return newsfeed_time;}

    public void setNewsfeed_time(int date){
        this.newsfeed_time = date;
    }

    public int getNumRatings(){return numratings;}

    public void setNumRatings(int numRatings){
        this.numratings = numRatings;
    }

    public double getAvgRating() {return avgRating;}

    public void setAvgRating(double avgRating) {
        this.avgRating = avgRating;
    }

    public int getGallery(){return gallery;}

    public void setGallery(int bool){
        this.gallery = bool;
    }
}
