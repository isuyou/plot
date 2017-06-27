package com.example.isuyo_000.myapplication;

/**
 * Created by isuyo_000 on 6/26/2017.
 */

//stores the X and Y positions for a data point in a streamlined data structure for redo/undo storage
public class Coordinates {
        protected double x;
        protected double y;

        Coordinates(double x, double y){
            this.x = x;
            this.y = y;
        }
}
