package com.virtualrobe.virtualrobe.virtualrobe_app.model;


import java.util.Set;

public class user_model {
    private String username;
    private String email;
    private String fullname;
    private String profilePic;
    private String gender;
    private String address;
    private int private_account;
    private String profilePic_thumbnail;
    private String DOB;

    public user_model(){
    }
    public user_model(String username,String email,String fullname,String profilePic,String gender,String address,
                      int private_account,String DOB){
        this.username = username;
        this.email = email;
        this.fullname = fullname;
        this.profilePic = profilePic;
        this.gender = gender;
        this.address = address;
        this.private_account = private_account;
        this.DOB = DOB;
    }

    public String getUsername(){return username;}
    public void  setUsername(String username){this.username = username;}

    public String getEmail(){return email;}
    public void setEmail(String email){this.email = email;}

    public String getFullname(){return fullname;}
    public void  setFullname(String fullname){this.fullname = fullname;}

    public String getProfilePic(){return profilePic;}
    public void setProfilePic(String profilePic){this.profilePic = profilePic;}

    public String getProfilePic_thumbnail(){return profilePic_thumbnail;}
    public void setProfilePic_thumbnail(String profilePic){this.profilePic_thumbnail = profilePic;}

    public String getGender(){return gender;}
    public void setGender(String gender){this.gender = gender;}

    public String getAddress(){return address;}
    public void setAddress(String address){this.address = address;}

    public int getPrivate_account(){return private_account;}
    public void setPrivate_account(int status){private_account = status;}

    public String getDOB(){return DOB;}
    public void setDOB(String Birthdate){this.DOB = Birthdate;}
}
