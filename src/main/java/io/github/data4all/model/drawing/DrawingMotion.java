package io.github.data4all.model.drawing;

import java.util.ArrayList;
import java.util.List;

import android.graphics.PointF;

/**
 * The DrawingMotion stores the path of a motion and provides methods to
 * determine the behavior to the motion<br/>
 * <br/>
 * It is used by the painting component to store the user input<br/>
 * Also its used by the MotionInterpreters to interpret the user input
 * 
 * @author tbrose
 */
public class DrawingMotion {
	/**
	 * The default tolerance for a Point
	 */
	public static final int POINT_TOLERANCE = 5;

	private List<PointF> points = new ArrayList<PointF>();

	/**
	 * Adds a Point to the DrawingMotion
	 * 
	 * @param x
	 *            the x value of the point
	 * @param y
	 *            the y value of the point
	 */
	public void addPoint(float x, float y) {
		points.add(new PointF(x, y));
	}

	/**
	 * Calculates if this DrawingMotion is a Path <br/>
	 * A DrawingMotion with zero entries is not a Path <br/>
	 * A DrawingMotion with more entries is a Path if it is not a point
	 * 
	 * @return true - if the motion has a path-size over zero and is not a point <br/>
	 *         false otherwise
	 * 
	 * @see DrawingMotion#isPoint()
	 * @see DrawingMotion#POINT_TOLERANCE
	 */
	public boolean isPath() {
		if (getPathSize() == 0) {
			return false;
		} else {
			return !isPoint();
		}
	}

	/**
	 * Calculates if this DrawingMotion is a Point <br/>
	 * A DrawingMotion with zero entries is not a Point <br/>
	 * A DrawingMotion with more entries is a Point, if all the Points describes
	 * a spot on the screen with at least {@link DrawingMotion#POINT_TOLERANCE
	 * POINT_TOLERANCE} difference from the start-point of the motion
	 * 
	 * @return true - if all Points in the motion are on the given tolerance
	 *         spot around the starting point <br/>
	 *         false otherwise
	 * 
	 * @see DrawingMotion#POINT_TOLERANCE
	 */
	public boolean isPoint() {
		if (getPathSize() == 0) {
			return false;
		}
		float startLength = getStart().length();
		for (PointF p : points) {
			if (Math.abs(startLength - p.length()) > POINT_TOLERANCE) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the first point of this DrawingMotion if there is at least one
	 * point in this motion
	 * 
	 * @return the first point of the motion or null if there is no point in the
	 *         motion
	 */
	public PointF getStart() {
		if (points.isEmpty()) {
			return null;
		} else {
			return points.get(0);
		}
	}

	/**
	 * Returns the last point of this DrawingMotion if there is at least one
	 * point in this motion
	 * 
	 * @return the last point of the motion or null if there is no point in the
	 *         motion
	 */
	public PointF getEnd() {
		if (points.isEmpty()) {
			return null;
		} else {
			return points.get(points.size() - 1);
		}
	}

	/**
	 * Returns the number of points in this DrawingMotion
	 * 
	 * @return the number of points
	 */
	public int getPathSize() {
		return points.size();
	}
}
