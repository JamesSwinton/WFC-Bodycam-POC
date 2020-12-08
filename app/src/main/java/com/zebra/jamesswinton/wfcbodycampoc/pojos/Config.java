package com.zebra.jamesswinton.wfcbodycampoc.pojos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Config {

    @SerializedName("api_password")
    @Expose
    private String apiPassword;
    @SerializedName("api_url")
    @Expose
    private String apiUrl;
    @SerializedName("eid")
    @Expose
    private int eid;

    public Config(String apiPassword, String apiUrl, int eid) {
        this.apiPassword = apiPassword;
        this.apiUrl = apiUrl;
        this.eid = eid;
    }

    public String getApiPassword() {
        return apiPassword;
    }

    public void setApiPassword(String apiPassword) {
        this.apiPassword = apiPassword;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public int getEid() {
        return eid;
    }

    public void setEid(int eid) {
        this.eid = eid;
    }
}
