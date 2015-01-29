package io.github.data4all.model.drawing;

import io.github.data4all.model.data.Node;
import io.github.data4all.model.data.OsmElement;
import io.github.data4all.model.data.Way;
import io.github.data4all.util.PointToCoordsTransformUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * This BuildingMotionInterpreter is a MotionInterpreter for buildings.<br/>
 * 
 * It interprets a three dot user input and calculates the fourth point of the
 * building.<br/>
 * If a motion is not a dot, the end point is used.
 * 
 * @author tbrose
 * @version 2
 * @see MotionInterpreter
 */
public class BuildingMotionInterpreter implements MotionInterpreter {

    private PointToCoordsTransformUtil pointTrans;

    /**
     * Creates an BuildingMotionInterpreter with the specified transformation
     * utility.
     * 
     * @param pointTrans the transformation utility
     */
    public BuildingMotionInterpreter(PointToCoordsTransformUtil pointTrans) {
        this.pointTrans = pointTrans;
    }

    /**
     * Calculates the fourth point in dependence of the first three points of
     * the given list.
     * 
     * @param areaPoints
     *            A list with exact three points
     */
    private static void addFourthPoint(List<Point> areaPoints) {
        final Point a = areaPoints.get(0);
        final Point b = areaPoints.get(1);
        final Point c = areaPoints.get(2);

        final float x = a.getX() + (c.getX() - b.getX());
        final float y = a.getY() + (c.getY() - b.getY());

        final Point d = new Point(x, y);
        areaPoints.add(d);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * io.github.data4all.model.drawing.MotionInterpreter#interprete(java.util
     * .List, io.github.data4all.model.drawing.DrawingMotion)
     */
    @Override
    public List<Point> interprete(List<Point> interpreted,
            DrawingMotion drawingMotion) {
        final List<Point> result;

        if (drawingMotion == null) {
            return interpreted;
        } else if (interpreted == null) {
            result = new ArrayList<Point>();
        } else if (interpreted.size() > 3) {
            return interpreted;
        } else {
            result = new ArrayList<Point>(interpreted);
        }

        if (drawingMotion.getPathSize() != 0 && drawingMotion.isPoint()) {
            // for dots use the average of the given points
            result.add(drawingMotion.average());
        } else {
            // for a path use the last point
            result.add(drawingMotion.getEnd());
        }

        if (result.size() == 3) {
            addFourthPoint(result);
        }

        return result;
    }

    /**
     * @author sbollen
     */
    @Override
    public OsmElement create(List<Point> polygon, int rotation) {
        final Way newWay = new Way(-1, 1);

        final List<Node> nodeList = pointTrans.transform(polygon, rotation);
        nodeList.add(nodeList.get(0));
        newWay.addNodes(nodeList, false);
        return newWay;
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.github.data4all.model.drawing.MotionInterpreter#isArea()
     */
    @Override
    public boolean isArea() {
        return true;
    }

}
