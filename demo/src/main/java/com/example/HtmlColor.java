package com.example;

public class HtmlColor {
    private String name;
    private String hexCode;
    private String RGBcode;

    public HtmlColor(String name, String hexCode, String RGBcode){
        this.name = name;
        this.RGBcode = RGBcode;
        this.hexCode = hexCode;
    }

    public String getName(){
        return this.name;
    }

    public String getHexCode(){
        return this.hexCode;
    }

    public String getRGBCode(){
        return this.RGBcode;
    }
    
    public static HtmlColor DarkRed(){
        return new HtmlColor("DarkRed", "#8B0000", "rgb(139, 0, 0)");
    }

    public static HtmlColor Red(){
        return new HtmlColor("Red", "#FF0000", "rgb(255, 0, 0)");
    }

    public static HtmlColor Black(){
        return new HtmlColor("Black", "#000000", "rgb(0, 0, 0)");
    }

    public static HtmlColor LightGreen(){
        return new HtmlColor("LightGreen", "#90EE90", "rgb(144, 238, 144)");
    }

    public static HtmlColor DarkGreen(){
        return new HtmlColor("DarkGreen", "#006400", "rgb(0, 100, 0)");
    }
}
