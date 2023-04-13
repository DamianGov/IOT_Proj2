package com.example.iot_proj2;

public class UserIDStatic {
    private static UserIDStatic instance = null;
    private String userId;


    private UserIDStatic()
    {

    }

    public static UserIDStatic getInstance()
    {
        if (instance == null)
        {
            instance = new UserIDStatic();
        }
        return instance;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public String getUserId()
    {
        return userId;
    }

}
