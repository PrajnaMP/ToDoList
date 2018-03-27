package com.mobinius.myapplicationlist;

import java.util.Date;
import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;



/**
 * Created by prajna on 30/5/17.
 */


public class TaskClass extends RealmObject{
    private String name, description, time,image,location;
    private Date isCompleted, date;
    private double lattitude,longituge;

    @PrimaryKey
    private String id=UUID.randomUUID().toString();

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(Date isCompleted) {
        this.isCompleted = isCompleted;
    }


    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String descriptiom) {
        this.description = descriptiom;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getLattitude() {
        return lattitude;
    }

    public void setLattitude(double lattitude) {
        this.lattitude = lattitude;
    }

    public double getLongituge() {
        return longituge;
    }

    public void setLongituge(double longituge) {
        this.longituge = longituge;
    }
}

