package com.example.whatsapplite;


public class Contacts {
    public int id;
    public String name,status,image;

    public Contacts(){

    }

    public Contacts( int id, String name, String status, String image) {
        this.id=id;
        this.name = name;
        this.status = status;
        this.image = image;
    }
    public int getId() {return id;}

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
}

