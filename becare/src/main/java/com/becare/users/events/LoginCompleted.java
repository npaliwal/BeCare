package com.becare.users.events;

/**
 * Created by neerajpaliwal on 31/08/16.
 */
public class LoginCompleted {
    private String name;

    public LoginCompleted(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }
}
