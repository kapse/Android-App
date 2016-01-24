package com.eew.balloonride.model;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by KTSL-NILESH on 02-11-2015.
 */
public class GetLanLagModel {

    private static GetLanLagModel myObj;
    private static ArrayList<LatLng> arraLatLan;

    /**
     * Create private constructor
     */
    private GetLanLagModel(){

    }
    /**
     * Create a static method to get instance.
     */
    public static GetLanLagModel getInstance(){
        if(myObj == null){
            myObj = new GetLanLagModel();
        }
        return myObj;
    }


    public  ArrayList<LatLng> getArraLatLan() {
        return arraLatLan;
    }

    public  void setArraLatLan(ArrayList<LatLng> arraLatLan) {
        GetLanLagModel.arraLatLan = arraLatLan;
    }


}
