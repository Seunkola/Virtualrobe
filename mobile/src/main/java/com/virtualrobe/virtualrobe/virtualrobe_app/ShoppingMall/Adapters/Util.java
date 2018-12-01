package com.virtualrobe.virtualrobe.virtualrobe_app.ShoppingMall.Adapters;


import java.util.ArrayList;
import java.util.List;

public class Util {
    public static List<String> getMenWearcategory(){
        List<String> List = new ArrayList<>();
        List.add("Men's Jeans");
        List.add("Men's Nightwear");
        List.add("Men's Shirts");
        List.add("Men's T-Shirts");
        List.add("Men's Trousers and Shorts");
        List.add("Men's Underwear and Socks");
        List.add("Polo Shirts");
        List.add("Suits, Blazers & Jackets");
        List.add("Sweaters and Jumpers (92)");

        return List;
    }

    public static List<String> getMenShoescategory(){
        List<String> List = new ArrayList<>();
        List.add("Men's Casual Shoes");
        List.add("Men's Formal Shoes");
        List.add("Men's Shoe Care & Accessories");
        List.add("Men's Slippers and Sandals");

        return List;
    }

    public static List<String> getMenAccesoriescategory(){
        List<String> List = new ArrayList<>();
        List.add("Men's Bags and Briefcases");
        List.add("Men's Belts and Wallets");
        List.add("Men's Hats and Caps");
        List.add("Men's Jewellery");
        List.add("Men's Ties and Accessories");
        List.add("Suspenders");

        return List;
    }

    public static List<String> getWomenWearcategory(){
        List<String> List = new ArrayList<>();
        List.add("Jumpsuits and Playsuits");
        List.add("Lingerie and Sleepwear");
        List.add("Kimonos");
        List.add("Suits & Blazers");
        List.add("Women's Dresses");
        List.add("Women's Plus Size");
        List.add("Women's Skirts");
        List.add("Women's Tops");
        List.add("Women's Trousers");

        return List;
    }

    public static List<String> getWomenShoescategrory(){
        List<String> List = new ArrayList<>();
        List.add("1.Women's Flat Shoes");
        List.add("Women's Heels");
        List.add("Women's Sandals and Slippers");
        List.add("Women's Shoe Care & Accessories");
        List.add("Women's Sport Shoes");
        List.add("Women's Wedges");

        return List;
    }

    public static List<String> getWomenAccessories(){
        List<String> List = new ArrayList<>();
        List.add("Bags & Accessories");
        List.add("Hair Bows & Bands");
        List.add("Women's Belts & Suspenders");
        List.add("Women's Hats and Scarves");
        List.add("Women's Jewellery");
        List.add("Women's Socks");

        return List;
    }

    public static List<String> getWatchescategory(){
        List<String> List = new ArrayList<>();
        List.add("Bracelet Strap");
        List.add("Leather Straps");
        List.add("Rubber & Fabric Strap");

        return List;
    }


    public static List<String> getBrand(){
        List<String> List = new ArrayList<>();
        List.add("A.P.C.");
        List.add("Acne Studios");
        List.add("Add");
        List.add("adidas");
        List.add("adidas Originals");
        List.add("Aglini");
        List.add("Alberta Ferretti");
        List.add("Alberto Guardiani");
        List.add("Aldo");

        return List;
    }

    public static List<String> getColor(){
        List<String> List = new ArrayList<>();
        List.add("White");
        List.add("Blue");
        List.add("MultiColor");
        List.add("Black");
        List.add("Brown");
        List.add("Pink");
        List.add("Yellow");

        return List;
    }

    public static List<String> getPrice(){
        List<String> List = new ArrayList<>();
        List.add("Under $100");
        List.add("$100 - $250");
        List.add("$250 - $500");
        List.add("$500 - $1000");
        List.add("$1000 & Above");

        return List;
    }
}
