package com.example.isuyo_000.myapplication;

import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;




import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {

    GraphView graph;
    ArrayList<DataPoint> dataPoints = new ArrayList<DataPoint>();
    DataPoint[] dataPointsArray;

    //stores history of the graph for undo settings
    //linked lists used as stacks
    private LinkedList<ArrayList<Coordinates>> undoStack = new LinkedList<ArrayList<Coordinates>>();
    private LinkedList<ArrayList<Coordinates>> redoStack = new LinkedList<ArrayList<Coordinates>>();
    private static final int threshold = 10;
    private Button undo;
    private Button redo;

    //Controls for adding and removing points
    private Button addPoint;
    private Button removePoint;


    //selectedIndex default value -1 represents no Index is selected
    Integer selectedIndex = -1;
    Integer lastSelectedIndex = -1;
    Integer minX = 0;
    Integer minY = 0;
    Integer range = 1;
    Integer domain = -1;
    Float rangeDivisor = 100f;
    Float domainDivisor = 50f;


    //scaling for pinch movements
    Float scale = 1f;
    ScaleGestureDetector gestureDetector;
    boolean isScaling = false;
    boolean finishScaling = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        //set up graph
        double y, x;
        x = 0;

        graph = (GraphView) findViewById(R.id.graph);
        LineGraphSeries lineGraph = new LineGraphSeries<DataPoint>();
        DataPoint currentPoint;

        //insert points
        for (int i = 0; i <= 5; i++) {
            domain+=1;
            y = (Math.sin(x) + 1) /2;
            currentPoint = new DataPoint(x, y);
            dataPoints.add(currentPoint);
            lineGraph.appendData(currentPoint, true, 500);
            x = x + 1;
        }


        //draw points
        drawPoints(lineGraph);

        //initializes undo and redo for the application
        createUndoRedo();

        //initializes
        createAddRemove();


        //detector for pinch
        gestureDetector = new ScaleGestureDetector(this, new ScaleListener());
    }

    /**draw points onto graph
     *  @param lineGraph : the series of data points to draw (specifically for a line graph)
     */
    private void drawPoints(LineGraphSeries lineGraph){
        lineGraph.setDrawDataPoints(true);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(minX);
        graph.getViewport().setMaxX(domain + minX);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(minY);
        graph.getViewport().setMaxY(range + minY);
        graph.addSeries(lineGraph);
        graph.refreshDrawableState();
        graph.setOnTouchListener(new movePoint());
    }


    /**
     * creates undo and redo buttons functionality and sets first undo/redo link
     */
    private void createUndoRedo(){
        undo =  (Button) findViewById(R.id.undo);
        redo =  (Button) findViewById(R.id.redo);
        //links undo button to undo method
        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                undo();
            }
        });
        //links redo button to redo method
        redo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redo();
            }
        });
        undo.setEnabled(false);
        redo.setEnabled(false);
    }

    /**
     * creates add and remove for points on the graph
     */
    private void createAddRemove(){
        addPoint =  (Button) findViewById(R.id.add);
        removePoint =  (Button) findViewById(R.id.remove);
        //links addPoint button to addPoint action
        addPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPoint();
            }
        });

        //links removePoint button to removePoint action
        removePoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removePoint();
            }
        });


    }

    /**
     * calls to addRemovePoints methods with private parameters
     */
    private void addPoint(){
        logUndo();
        dataPoints = addRemovePoints.addPoint(dataPoints);
        LineGraphSeries lineGraph = new LineGraphSeries<DataPoint>();
        for(DataPoint dataPoint : dataPoints) {
            lineGraph.appendData(dataPoint, true, 500);
        }
        lineGraph.setDrawDataPoints(true);
        graph.removeAllSeries();
        graph.addSeries(lineGraph);
    }

    private void removePoint(){
        try {
            logUndo();
            dataPoints = addRemovePoints.removePoint(dataPoints, lastSelectedIndex);
            LineGraphSeries lineGraph = new LineGraphSeries<DataPoint>();
            for (DataPoint dataPoint : dataPoints) {
                lineGraph.appendData(dataPoint, true, 500);
            }
            lineGraph.setDrawDataPoints(true);
            graph.removeAllSeries();
            graph.addSeries(lineGraph);
            
            //reset selected index
            selectedIndex = -1;
        }
        catch(IllegalArgumentException e){
            //TODO: add error handling
            Toast toast = Toast.makeText(
                    getApplicationContext(),
                    "invalid index to remove:  " + lastSelectedIndex,
                    Toast.LENGTH_LONG
            );
            toast.show();
        }
    }

    /**
     * enables current state of data points to be copied into the data point undo Queue
     * I.E. adds 1 to the undoStack
     */
    private void logUndo(){
        //resets the redo storage log after new changes are implemented in between undo's
        redoStack = new LinkedList<ArrayList<Coordinates>>();

        //copies current data points over into Coordinate representation
        ArrayList<Coordinates> dataPointList = convertDataPointsToCoordinates(this.dataPoints);

        //adds all converted data points to undo stack
        if(undoStack.size() >= threshold){
            undoStack.remove(1);
            undoStack.addLast(dataPointList);
        }
        else{
            undoStack.addLast(dataPointList);
        }


        undo.setEnabled(true);
        redo.setEnabled(false);
    }

    /**
     * converts an array of data points into a list of Coordinates
     * @param dataPoints: the collection or set of data points that will be converted
     * @return : the converted list of output items with just x and y coordinates
     */
    private ArrayList<Coordinates> convertDataPointsToCoordinates(Collection<DataPoint> dataPoints){
        ArrayList<Coordinates> output = new ArrayList<Coordinates>();
        for(DataPoint dataPoint : dataPoints){
            output.add(new Coordinates(dataPoint.getX(), dataPoint.getY()));
        }
        return output;
    }

    /**
     * method returns graph to a previous state
     * should only be called by the button's press
     */
    private void undo(){
        if(undoStack.isEmpty()){
            undo.setEnabled(false);
            return;
        }
        //instantiate variables
        LineGraphSeries lineGraph = new LineGraphSeries<DataPoint>();
        ArrayList<Coordinates> coordinates =  undoStack.removeLast();
        redoStack.addLast(convertDataPointsToCoordinates(this.dataPoints));
        DataPoint currentPoint;
        dataPoints = new ArrayList<DataPoint>();
        selectedIndex = -1;

        //converts all coordinates to data points
        for(Coordinates coordinate : coordinates){
            currentPoint = new DataPoint(coordinate.x, coordinate.y);
            dataPoints.add(currentPoint);
            lineGraph.appendData(currentPoint, true, 500);
        }

        //redraws graph with new data points
        lineGraph.setDrawDataPoints(true);
        graph.removeAllSeries();
        graph.addSeries(lineGraph);

        //checks if undo is available
        //if the undo stack is empty, disables button
        if(undoStack.isEmpty())
            undo.setEnabled(false);
        redo.setEnabled(true);
    }

    /**
     * method returns graph to a previous state changed by undo button
     * should only be called by the redo button's press
     */
    private void redo(){
        if(redoStack.isEmpty()){
            redo.setEnabled(false);
            return;
        }

        //instantiate variables
        LineGraphSeries lineGraph = new LineGraphSeries<DataPoint>();
        ArrayList<Coordinates> coordinates =  redoStack.removeLast();
        undoStack.addLast(convertDataPointsToCoordinates(this.dataPoints));
        DataPoint currentPoint;
        dataPoints = new ArrayList<DataPoint>();
        selectedIndex = -1;

        //converts all coordinates to data points
        for(Coordinates coordinate : coordinates){
            currentPoint = new DataPoint(coordinate.x, coordinate.y);
            dataPoints.add(currentPoint);
            lineGraph.appendData(currentPoint, true, 500);
        }

        //redraws graph with new data points
        lineGraph.setDrawDataPoints(true);
        graph.removeAllSeries();
        graph.addSeries(lineGraph);

        if(redoStack.isEmpty())
            redo.setEnabled(false);
        undo.setEnabled(true);
    }


    //moves single point on touch
    private class movePoint implements View.OnTouchListener {

        Toast toast;

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            /*
            if(event.getAction() == MotionEvent.ACTION_UP){

                    Log.d("State", "X:   " + event.getX() + "   Y:    ");
                    Log.d("State", "Width:   " + graph.getGraphContentWidth() + "  Height:   " + graph.getGraphContentHeight());
                    Log.d("State", "Data Point Index:   " + selectedIndex);
            }
            */
            gestureDetector.onTouchEvent(event);
            switch (event.getAction()) {
                case (MotionEvent.ACTION_DOWN):
                    if ((toast != null))
                        toast.cancel();
                    matchDataPoint(event.getX(), event.getY());

                    toast = Toast.makeText(
                            getApplicationContext(),
                            //"X: " + event.getX() + " Y: " + event.getY() + " Width: " + graph.getGraphContentWidth() + " Height: " + graph.getGraphContentHeight(),
                            "selectedIndex:  " + selectedIndex,
                            Toast.LENGTH_SHORT
                    );
                    toast.show();
                    break;
                //changes data point location if one is selected
                case MotionEvent.ACTION_UP:
                    if(!isScaling) {
                        changeDataPoint(event.getX(), event.getY(), selectedIndex);

                        //reset and track last selected index
                        lastSelectedIndex = selectedIndex;
                        selectedIndex = -1;
                    }
                    isScaling = false;
                    break;
                case MotionEvent.ACTION_MOVE:

                    break;
            }

            return true;

        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener{
        private float initSpanX;
        private float initSpanY;
        private float adjustX;
        private float adjustY;
        Toast toast;

        // Detects that new pointers are going down.
        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            finishScaling = false;
            isScaling = true;
            initSpanX = ScaleGestureDetectorCompat.
                    getCurrentSpanX(scaleGestureDetector);
            initSpanY = ScaleGestureDetectorCompat.getCurrentSpanY(scaleGestureDetector);
            adjustX = 1;
            adjustY = 1;

            toast = Toast.makeText(
                    getApplicationContext(),
                    //"X: " + event.getX() + " Y: " + event.getY() + " Width: " + graph.getGraphContentWidth() + " Height: " + graph.getGraphContentHeight(),
                    "adjustX: " + adjustX + "  adjustY: " + adjustY + "  spanX: " + initSpanX + "  spanY: " + initSpanY,
                    Toast.LENGTH_LONG
            );
            toast.show();
            return true;
        }
        /*
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            float spanX = ScaleGestureDetectorCompat.
                    getCurrentSpanX(scaleGestureDetector);
            float spanY = ScaleGestureDetectorCompat.
                    getCurrentSpanY(scaleGestureDetector);
            adjustX = spanX / initSpanX;
            adjustY =  spanY / initSpanY;
            return true;
        }
        */

        @Override
        public void onScaleEnd(ScaleGestureDetector detector){
            float spanX = ScaleGestureDetectorCompat.
                    getCurrentSpanX(detector);
            float spanY = ScaleGestureDetectorCompat.
                    getCurrentSpanY(detector);
            adjustX = spanX / initSpanX;
            adjustY =  spanY / initSpanY;
            scaleDataPoints(adjustX, adjustY);
            toast = Toast.makeText(
                    getApplicationContext(),
                    //"X: " + event.getX() + " Y: " + event.getY() + " Width: " + graph.getGraphContentWidth() + " Height: " + graph.getGraphContentHeight(),
                    "adjustX: " + adjustX + "  adjustY: " + adjustY + "  spanX: " + spanX + "  spanY: " + spanY,
                    Toast.LENGTH_LONG
            );
            toast.show();
        }


    }


    //Finds selected index is touch-press is close enough to the location of that data point
    protected void matchDataPoint(float x, float y){
        //conversions to screen settings positional space
        float xPos =     (x- graph.getGraphContentLeft()) * domain / graph.getGraphContentWidth();
        float yPos =     (graph.getGraphContentHeight() - (y - graph.getGraphContentTop()))  * range / graph.getGraphContentHeight();
        int i= 0;
        for(DataPoint dataPoint : dataPoints){
            if((Math.abs(xPos -  dataPoint.getX()) <= domain/ domainDivisor) && (Math.abs(yPos -  dataPoint.getY()) <= range/ rangeDivisor)){
                if(selectedIndex != i)
                    selectedIndex = i;
                else
                    //selectedIndex = -1;
                    selectedIndex = i;
                return;
            }
            i++;
        }
        //selectedIndex = -1;
    }



    //finds selected data point and changes its position
    protected void changeDataPoint(float x, float y, int selectedIndex){
        if(selectedIndex >= 0){
            //add save state to undo recovery
            logUndo();

            float xPos = (x- graph.getGraphContentLeft()) * domain / graph.getGraphContentWidth();
            //constrain bounds to localed inside graph
            if (selectedIndex == 0)
                xPos = minX;
            else if(selectedIndex == dataPoints.size() - 1)
                xPos = domain;
            else if(xPos < minX)
                xPos = minX;
            else if(xPos > domain)
                xPos = domain;
            float yPos = (graph.getGraphContentHeight() - (y - graph.getGraphContentTop()))  * range / graph.getGraphContentHeight();
            if (yPos > range)
                yPos = range;
            if(yPos < minY)
                yPos = minY;
            dataPoints.set(selectedIndex, new DataPoint(xPos, yPos));
            dataPoints.sort(DataPointComparator);
            graph.removeAllSeries();
            //adds all old points plus new point to the graph
            LineGraphSeries lineGraph = new LineGraphSeries<DataPoint>();
            for(DataPoint dataPoint : dataPoints){
                lineGraph.appendData(dataPoint, true, 500);
            }
            lineGraph.setDrawDataPoints(true);
            graph.addSeries(lineGraph);



        }
    }

    //scales the data points for pinch-changes
    //takes in X and Y scaling values from 0 < x,y < infinity : where 1 is the same distance apart, <1 is smaller, and >1 is larger
    protected void scaleDataPoints(float scaleX, float scaleY){
        //add save state to undo recovery
        logUndo();

        //changes  every data point relative to selected index data point
        if(selectedIndex >= 0){
            int i =0;
            for(DataPoint dataPoint : dataPoints){
                if(i != selectedIndex){
                    distanceToBounds(dataPoint, scaleX, scaleY, i);
                }
                i++;
            }
        }
        //redraw graph
        graph.removeAllSeries();
        LineGraphSeries lineGraph = new LineGraphSeries<DataPoint>();
        for(DataPoint dataPoint : dataPoints){
            lineGraph.appendData(dataPoint, true, 500);
        }
        lineGraph.setDrawDataPoints(true);
        graph.addSeries(lineGraph);
    }

    //global variables for quicker access
    private double distanceX;
    private double distanceY;
    private double edgeX;
    private double edgeY;

    //helper function to get the distance of a point to the edge of the graph
    private void distanceToBounds(DataPoint dataPoint, float scaleX, float scaleY, int i){
        if(i < selectedIndex) {
            edgeX = minX;
            edgeY = minY;
        }
        else if(i > selectedIndex){
            edgeX = minX + domain;
            edgeY = minY + range;
        }
        else
            return;

        //get X coordinate (first two conditions are bounds to end of graph
        if(i == 0){
            distanceX = 0;
        }
        else if(i == dataPoints.size() - 1){
            distanceX = edgeX;
        }
        else if(scaleX < 1){
            distanceX = (dataPoints.get(selectedIndex).getX() - dataPoint.getX());
            distanceX = dataPoint.getX() + distanceX * scaleX;
        }
        else if (scaleX > 1){
            distanceX = (dataPoints.get(i).getX() - edgeX);
            distanceX = edgeX + distanceX /scaleX;
        }
        else{
            distanceX = dataPoints.get(i).getX();
        }

        //get Y coordinate
        if(scaleY < 1){
            distanceY = (dataPoints.get(selectedIndex).getY() - dataPoint.getY());
            distanceY = dataPoint.getY() + distanceY * scaleY;
        }
        else if (scaleX > 1){
            distanceY = (dataPoints.get(i).getY() - edgeY);
            distanceY = edgeY + distanceY / scaleY;
        }
        else{
            distanceY = dataPoints.get(i).getY();
        }

        dataPoints.set(i, new DataPoint(distanceX, distanceY));
    }
    //Comparator for sorting data points; Sorts by X coordinate
    public static Comparator<DataPoint> DataPointComparator = new Comparator<DataPoint>() {
        @Override
        public int compare(DataPoint o1, DataPoint o2) {
            if(o1.getX() < o2.getX())
                return -1;
            else if(o1.getX() > o2.getX())
                return 1;
            else
                return 0;
        }
    };




}

/**
    Notes:
    graph.getGraphContentLeft() provides the margin until the graph starts from the left
    (event.getX - graph.getGraphContentLeft()) * graphDomainLength / graph.getGraphContentWidth()  = X position of point

    graph.getGraphContentTop() providess the margin until the graph ends from the top
    (graph.getGraphContentHeight() - (event.getY - graph.getGraphContentTop())  * graphRangeHeight / graph.getGraphContentHeight() = Y position of a point
 */
