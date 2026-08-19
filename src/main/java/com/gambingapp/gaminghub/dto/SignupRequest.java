// Data Transfer Object for the signup request
package com.gambingapp.gaminghub.dto;

public class SignupRequest {
    private String username;
    private String password;
    private String email;

    public SignupRequest(){
    }

    public String getUsername(){
        return username;
    }

    public String getPassword(){
        return password;
    }

    public void setUsername(String username){
        this.username = username;
    }
    
    public void setPassword(String password){
        this.password = password;
    }

    public String getEmail(){
        return email;
    }

    public void setEmail(String email){
        this.email = email;
    }
}