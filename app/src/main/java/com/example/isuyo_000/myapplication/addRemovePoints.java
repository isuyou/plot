package com.example.isuyo_000.myapplication;

import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;

/**
 * Created by isuyo_000 on 7/5/2017.
 */

//adding/removing points from a data point set
//default adds to the end of the set
//static methods for use as set functions
public class addRemovePoints{


    /**
     * adds a data  point to the end of the list; pushes all other points over via their x-axis; maintains y value of previous anchor
     * @param dataPoints : the initial set of data points
     * @return a new set of Data with an extra point appended to the end
     */
    private static ArrayList<DataPoint> addPoint(ArrayList<DataPoint> dataPoints){
        double lastY = 0;
        double lastX = 0;
        ArrayList<DataPoint> newPoints = new ArrayList<DataPoint>();
        int index = 0;

        //Copy all points over with adjusted X values
        for(DataPoint dataPoint : dataPoints){
            lastY = dataPoint.getY();
            lastX = dataPoint.getX();
            if(index == 0)
                newPoints.add(new DataPoint(lastX,lastY));
            else
                newPoints.add(new DataPoint(lastX*(dataPoints.size()-1)/dataPoints.size(), lastY));
            index++;
        }
        //add last/new point
        newPoints.add(new DataPoint(lastX, lastY));
        return newPoints;
    }

    /**
     * remove a data point from the list; pushes all other points to fill in the gaps left behind (invertible from addPoint)
     * @param dataPoints  : initial set of data points
     * @param selectedIndex  : which data point to remove;  does not allow change of the anchors
     * @return a new set of Data with the selectedd point removed
     */
    private static ArrayList<DataPoint> removePoint(ArrayList<DataPoint> dataPoints, int selectedIndex)throws IllegalArgumentException{
        //makes sure selectedIndex is valid
        if((selectedIndex <= 0) || (selectedIndex >= (dataPoints.size() - 1))){
            throw new IllegalArgumentException("invalid selection index in set of Data Points");
        }

        double lastY = 0;
        double lastX = 0;
        ArrayList<DataPoint> newPoints = new ArrayList<DataPoint>();
        int index = 0;


        //Copy all points over with adjusted X values (except data point to be removed)
        for(DataPoint dataPoint : dataPoints){
            if(index != selectedIndex){
                lastY = dataPoint.getY();
                lastX = dataPoint.getX();
                if(index == 0 || index == (dataPoints.size() - 1))
                    newPoints.add(new DataPoint(lastX, lastY));
                else
                    newPoints.add(new DataPoint(lastX * (dataPoints.size()+1) / dataPoints.size(), lastY));
            }
            index++;
        }

        return newPoints;
    }


}
