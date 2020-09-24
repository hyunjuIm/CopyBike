package com.example.copybike.ui;

import android.graphics.drawable.Drawable;

import java.util.ArrayList;

public class SideListViewItem {
    private Drawable icon;
    private String title;
    public ArrayList<String> menu = new ArrayList<String>();

    public SideListViewItem(String titile, Drawable icon){
        this.title = titile;
        this.icon = icon;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
