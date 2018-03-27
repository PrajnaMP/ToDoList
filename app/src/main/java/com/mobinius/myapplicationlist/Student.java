package com.mobinius.myapplicationlist;

import io.realm.RealmObject;

/**
 * Created by prajna on 12/6/17.
 */

public class Student  extends RealmObject {
    private int roll_no;
    private String name;

    public int getRoll_no() {
        return roll_no;
    }

    public void setRoll_no(int roll_no) {
        this.roll_no = roll_no;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
