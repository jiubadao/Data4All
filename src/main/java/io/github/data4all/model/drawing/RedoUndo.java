package io.github.data4all.model.drawing;

import io.github.data4all.logger.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author vkochno
 *
 */
public class RedoUndo {

	/**
	 * List of all added Points
	 */
	private List<Point> motions;
	private int maxCount, currentCount;
	private boolean isUndo = false;

	public RedoUndo() {
		motions = new ArrayList<Point>();
		maxCount = 0;
		currentCount = 0;
	}

	public RedoUndo(List<Point> points) {
		if (motions == null) {
			if(points != null){
				motions = points;
				maxCount = points.size();
				currentCount = points.size();
			}else{
			motions = new ArrayList<Point>();
			maxCount = 0;
			currentCount = 0;
			}
		} else {
			if(points != null){
				addList(points);
			}
		}
	}

	public void addMotion(Point point) {
		Log.d(this.getClass().getSimpleName(), "ADD: " + currentCount + ":"
				+ maxCount);
		if (maxCount == currentCount) {
			maxCount++;
			currentCount++;
		} else {
			for(int i = currentCount; i<=maxCount;i++){
				motions.remove(i);
			}
			currentCount++;
			maxCount = currentCount;
		}
		motions.add(point);
	}

	public void addList(List<Point> newPoly) {
		for (Point p : newPoly) {
			motions.clear();
			Log.d(this.getClass().getSimpleName(),
					"motions size" + motions.size());
			addMotion(p);
		}
	}

	public List<Point> undo() {
		Log.d(this.getClass().getSimpleName(), "UNDO: " + currentCount + ":"
				+ maxCount);
		if (currentCount != 0 && currentCount <= maxCount) {
			currentCount--;
			List<Point> relist = new ArrayList<Point>();
			for (int i = 0; i < currentCount; i++) {
				relist.add(motions.get(i));
			}
			return relist;
		}
		return motions;
	}

	public List<Point> redo() {
		Log.d(this.getClass().getSimpleName(), "REDO: " + currentCount + ":"
				+ maxCount);
		if (currentCount < maxCount) {
			currentCount++;
			List<Point> relist = new ArrayList<Point>();
			for (int i = 0; i < currentCount; i++) {
				relist.add(motions.get(i));
			}
			return relist;
		}
		return motions;
	}

	public int getCurrent() {
		return currentCount;
	}

	public int getMax() {
		return maxCount;
	}
}
