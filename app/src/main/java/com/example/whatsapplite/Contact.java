package com.example.whatsapplite;


import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.pchmn.materialchips.model.ChipInterface;

import java.io.Serializable;

public class Contact implements ChipInterface, Serializable {
    public String uid;
    public String name, status, image;
    private boolean isSelect;

    public Contact() {

    }

    public Contact(String id, String name, String status, String image) {
        this.uid = id;
        this.name = name;
        this.status = status;
        this.image = image;
    }

    @Override
    public Object getId() {
        return null;
    }

    @Override
    public Uri getAvatarUri() {
        return null;
    }

    @Override
    public Drawable getAvatarDrawable() {
        return null;
    }

    @Override
    public String getLabel() {
        return this.name;
    }

    @Override
    public String getInfo() {
        return null;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }
}

