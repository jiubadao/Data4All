package io.github.data4all.view;

import io.github.data4all.activity.ShowPictureActivity;
import io.github.data4all.logger.Log;
import io.github.data4all.model.data.OsmElement;
import io.github.data4all.model.drawing.AreaMotionInterpreter;
import io.github.data4all.model.drawing.BuildingMotionInterpreter;
import io.github.data4all.model.drawing.DrawingMotion;
import io.github.data4all.model.drawing.MotionInterpreter;
import io.github.data4all.model.drawing.Point;
import io.github.data4all.model.drawing.RedoUndo;
import io.github.data4all.model.drawing.PointMotionInterpreter;
import io.github.data4all.model.drawing.WayMotionInterpreter;
import io.github.data4all.util.PointToCoordsTransformUtil;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * This TouchView listen to MotionEvents from the user, saves them into
 * DrawingMotions, uses MotionInterpreters to interpret the input and draws the
 * interpreted polygons.
 * 
 * @author tbrose
 *
 * @see MotionEvent
 * @see DrawingMotion
 * @see MotionInterpreter
 */
public class TouchView extends View {


	/**
	 * The paint to draw the path with
	 */
	private final Paint pathPaint = new Paint();
	private final Paint areaPaint = new Paint();

    /**
     * The motion interpreted Polygon
     */
    private List<Point> polygon = new ArrayList<Point>();

    /**
     * The Polygon with the current pending motion
     */
    private List<Point> newPolygon = new ArrayList<Point>();

    /**
     * The current motion the user is typing via the screen
     */
    private DrawingMotion currentMotion;
    
    /**
     * An object for the calculation of the point transformation
     */
    private PointToCoordsTransformUtil pointTrans;

    /**
     * The currently used interpreter
     */
    private MotionInterpreter interpreter;

    /**
	 * The current used RedoUndo object
	 */
	private RedoUndo redoUndo;
	
	ShowPictureActivity show;

	public TouchView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		show = (ShowPictureActivity) context;
	}

	public TouchView(Context context, AttributeSet attrs) {
		super(context, attrs);
		show = (ShowPictureActivity) context;
	}

	public TouchView(Context context) {
		super(context);
		show = (ShowPictureActivity) context;
	}

    /**
     * Remove all recorded DrawingMotions from this TouchView
     */
    public void clearMotions() {
        if (polygon != null) {
            polygon.clear();
        }
    }


	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		interpreter = new WayMotionInterpreter();
		pathPaint.setColor(MotionInterpreter.PATH_COLOR);
		pathPaint.setStrokeWidth(MotionInterpreter.PATH_STROKE_WIDTH);
		areaPaint.setColor(MotionInterpreter.AREA_COLOR);
		areaPaint.setStyle(Paint.Style.FILL);
		areaPaint.setAlpha(100);
		redoUndo = new RedoUndo();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawARGB(0, 0, 0, 0);
		
		Path path = new Path();
		path.reset();
		

		if (newPolygon != null && newPolygon.size()!=0) {
			path.moveTo(newPolygon.get(0).getX(),newPolygon.get(0).getY());
			// first draw all lines
			int limit = newPolygon.size();
			// Don't draw the last line if it is not an area
			limit -= interpreter.isArea() ? 0 : 1;
			for (int i = 0; i < limit; i++) {
				// The next point in the polygon
				Point b = newPolygon.get((i + 1) % newPolygon.size());
				Point a = newPolygon.get(i);
				path.lineTo(a.getX(),a.getY());
				path.lineTo(b.getX(),b.getY());
				canvas.drawLine(a.getX(), a.getY(), b.getX(), b.getY(),
						pathPaint);
			}
			if(interpreter instanceof AreaMotionInterpreter || interpreter instanceof BuildingMotionInterpreter){
			canvas.drawPath(path, areaPaint);
			}
			// afterwards draw the points
			for (Point p : newPolygon) {
				canvas.drawCircle(p.getX(), p.getY(),
						MotionInterpreter.POINT_RADIUS, pathPaint);
			}
			undoUseable();
			redoUseable();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
        case MotionEvent.ACTION_DOWN:
            currentMotion = new DrawingMotion();
            handleMotion(event, "end");
            break;
        case MotionEvent.ACTION_UP:
            handleMotion(event, "start");
            polygon = newPolygon;
            redoUndo = new RedoUndo(polygon);
            break;
        case MotionEvent.ACTION_MOVE:
            handleMotion(event, "move");
            break;
        }
        return true;
    }

    /**
     * Handles the given motion:<br/>
     * Add the point to the current motion<br/>
     * Logs the motion<br/>
     * Causes the view to redraw itself afterwards
     * 
     * @param event
     *            The touch event
     * @param action
     *            the named action which is in progress
     */
    private void handleMotion(MotionEvent event, String action) {
        if (currentMotion != null) {
            currentMotion.addPoint(event.getX(), event.getY());
            newPolygon = interpreter.interprete(polygon, currentMotion);
            Log.d(this.getClass().getSimpleName(),
                    "Motion " + action + ": " + currentMotion.getPathSize()
                            + ", point: " + currentMotion.isPoint());
            postInvalidate();
        }
    }

    public void setInterpretationType(InterpretationType type) {
        switch (type) {
        case AREA:
            interpreter = new AreaMotionInterpreter(pointTrans);
            break;
        case POINT:
            interpreter = new PointMotionInterpreter(pointTrans);
            break;
        case BUILDING:
            interpreter = new BuildingMotionInterpreter(pointTrans);
            break;
        case WAY:
            interpreter = new WayMotionInterpreter(pointTrans);
            break;
        default:
            throw new IllegalArgumentException("'type' cannot be null");
        }
    }

    public static enum InterpretationType {
        AREA, POINT, BUILDING, WAY;
    }

    	public void redo() {
		newPolygon = redoUndo.redo();
		polygon = newPolygon;
		redoUseable();
	}

	public void undo() { 
		newPolygon = redoUndo.undo();
		polygon = newPolygon;
		show.SetRedoEnable(true);
		undoUseable();
	}
	
	public boolean redoUseable(){
		if(redoUndo.getCurrent() == redoUndo.getMax()){
			Log.d(this.getClass().getSimpleName(),
					"false redo");
			show.SetRedoEnable(false);
			return true;
		} else {
			Log.d(this.getClass().getSimpleName(),
					"false redo");
			show.SetRedoEnable(true);
			return false;
		}
	}
	
	public boolean undoUseable(){
		if(redoUndo.getMax() != 0 && redoUndo.getCurrent() != 0){
			Log.d(this.getClass().getSimpleName(),
					"true undo");
			show.SetUndoEnable(true);
			return true;
		} else {
			Log.d(this.getClass().getSimpleName(),
					"false undo");
			show.SetUndoEnable(false);
			return false;
		}
	}
    
    /**
     * Set the actual PointToCoordsTransformUtil with the actual location and camera parameters
     * @param pointTrans the actual object
     */
    public void setTransformUtil(PointToCoordsTransformUtil pointTrans) {
        this.pointTrans = pointTrans;
    }
    
    /**
     * Create an OsmElement from the given polygon
     * @return the created OsmElement (with located nodes)
     */
    public OsmElement create() {
        return interpreter.create(polygon);
    }
}
