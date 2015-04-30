/*
 * Copyright (c) 2014, 2015 Data4All
 * 
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 * 
 * <p>Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.github.data4all.handler;

import io.github.data4all.logger.Log;
import io.github.data4all.model.data.AbstractDataElement;
import io.github.data4all.model.data.Node;
import io.github.data4all.model.data.PolyElement;
import io.github.data4all.model.data.PolyElement.PolyElementType;
import io.github.data4all.model.data.Tag;
import io.github.data4all.model.data.Tags;
import io.github.data4all.model.data.Track;
import io.github.data4all.model.data.TrackPoint;
import io.github.data4all.model.data.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

/**
 * This class handles all database requests for the OSM objects that have to be
 * saved, such as create, read, update and delete.
 * 
 * @author Kristin Dahnken
 * @author fkirchge
 * @author sbrede
 * 
 */
public class DataBaseHandler extends SQLiteOpenHelper { // NOSONAR

    private static final String SELECT_ALL = "SELECT * FROM ";

    private static final String TAG = "DataBaseHandler";

    // Database Version
    private static final int DATABASE_VERSION = 5;

    // Database Name
    private static final String DATABASE_NAME = "Data4AllDB";

    // Table Names
    private static final String TABLE_USER = "users";
    private static final String TABLE_DATAELEMENT = "dataelements";
    private static final String TABLE_POLYELEMENT = "polyelements";
    private static final String TABLE_NODE = "nodes";
    private static final String TABLE_TAGMAP = "tagmap";
    private static final String TABLE_LASTCHOICE = "lastChoice";
    private static final String TABLE_GPSTRACK = "gpstracks";
    private static final String TABLE_TRACKPOINT = "trackpoints";

    // User Column Names
    private static final String KEY_USERNAME = "username";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_TOKENSECRET = "tokensecret";

    // General Column Names
    private static final String KEY_OSMID = "osmid";
    private static final String KEY_INCID = "incid";
    private static final String KEY_TYPE = "type";
    private static final String KEY_DATAELEMENT = "element";

    // Node Column Names
    private static final String KEY_LAT = "lat";
    private static final String KEY_LON = "lon";

    // TagMap Column Names
    private static final String KEY_TAGID = "tagid";
    private static final String KEY_VALUE = "value";

    // GPS Track Column Names
    private static final String KEY_TRACKNAME = "trackname";
    private static final String KEY_TRACKPOINTS = "trackpointids";
    private static final String FLAG_FINISHED = "finished";

    // GPS Trackpoint Column Names
    private static final String KEY_ALT = "altitude";
    private static final String KEY_TIME = "timestamp";

    /**
     * Default constructor for the database handler.
     * 
     * @param context
     *            the application.
     */
    public DataBaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Table creation
    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_USERS_TABLE =
                "CREATE TABLE " + TABLE_USER + " (" + KEY_USERNAME
                        + " TEXT PRIMARY KEY," + KEY_TOKEN + " TEXT,"
                        + KEY_TOKENSECRET + " TEXT" + ")";

        final String CREATE_DATAELEMENTS_TABLE =
                "CREATE TABLE " + TABLE_DATAELEMENT + " (" + KEY_OSMID
                        + " INTEGER PRIMARY KEY, " + KEY_TYPE + " INTEGER)";
        final String CREATE_POLYELEMENT_TABLE =
                "CREATE TABLE " + TABLE_POLYELEMENT + " (" + KEY_OSMID
                        + " INTEGER PRIMARY KEY," + KEY_TYPE + " INTEGER)";
        final String CREATE_NODES_TABLE =
                "CREATE TABLE " + TABLE_NODE + " (" + KEY_OSMID
                        + " INTEGER PRIMARY KEY," + KEY_DATAELEMENT
                        + " INTEGER," + KEY_LAT + " REAL," + KEY_LON + " REAL)";
        final String CREATE_TAGMAP_TABLE =
                "CREATE TABLE " + TABLE_TAGMAP + " (" + KEY_DATAELEMENT
                        + " INTEGER," + KEY_TAGID + " INTEGER," + KEY_VALUE
                        + " TEXT)";
        final String CREATE_LASTCHOICE_TABLE =
                "CREATE TABLE " + TABLE_LASTCHOICE + " (" + KEY_TYPE
                        + " INTEGER," + KEY_TAGID + " INTEGER," + KEY_VALUE
                        + " TEXT)";

        final String CREATE_GPSTRACK_TABLE =
                "CREATE TABLE " + TABLE_GPSTRACK + " (" + KEY_INCID
                        + " INTEGER PRIMARY KEY," + KEY_TRACKNAME + " TEXT,"
                        + KEY_TRACKPOINTS + " TEXT," + FLAG_FINISHED
                        + " INTEGER" + ")";
        final String CREATE_TRACKPOINT_TABLE =
                "CREATE TABLE " + TABLE_TRACKPOINT + " (" + KEY_INCID
                        + " INTEGER PRIMARY KEY," + KEY_LAT + " REAL,"
                        + KEY_LON + " REAL," + KEY_ALT + " REAL," + KEY_TIME
                        + " REAL" + ")";

        db.execSQL(CREATE_USERS_TABLE);

        db.execSQL(CREATE_DATAELEMENTS_TABLE);
        db.execSQL(CREATE_POLYELEMENT_TABLE);
        db.execSQL(CREATE_NODES_TABLE);
        db.execSQL(CREATE_TAGMAP_TABLE);
        db.execSQL(CREATE_LASTCHOICE_TABLE);

        db.execSQL(CREATE_GPSTRACK_TABLE);
        db.execSQL(CREATE_TRACKPOINT_TABLE);

        Log.i(TAG, "Tables have been created.");
    }

    // Database handling on upgrade
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        final String drop = "DROP TABLE IF EXISTS ";

        // Drop tables that already exist
        db.execSQL(drop + TABLE_USER);

        db.execSQL(drop + TABLE_DATAELEMENT);
        db.execSQL(drop + TABLE_POLYELEMENT);
        db.execSQL(drop + TABLE_NODE);
        db.execSQL(drop + TABLE_TAGMAP);
        db.execSQL(drop + TABLE_LASTCHOICE);

        db.execSQL(drop + TABLE_GPSTRACK);
        db.execSQL(drop + TABLE_TRACKPOINT);

        Log.i(TAG, "Tables have been dropped and will be recreated.");

        // Recreate tables
        this.onCreate(db);
    }

    // USER CRUD

    /**
     * This method creates and stores a new user in the database. The data is
     * taken from the {@link User} object that is passed to the method.
     * 
     * @param user
     *            the {@link User} object from which the data will be taken.
     */
    public void createUser(User user) {
        final SQLiteDatabase db = getWritableDatabase();

        final ContentValues values = new ContentValues();
        values.put(KEY_USERNAME, user.getUsername());
        values.put(KEY_TOKEN, user.getOAuthToken());
        values.put(KEY_TOKENSECRET, user.getOauthTokenSecret());

        final long rowID = db.insert(TABLE_USER, null, values);
        Log.i(TAG, "User " + rowID + " has been added.");
    }

    /**
     * This method deletes a specific user from the database.
     * 
     * @param user
     *            the {@link User} object whose data should be deleted.
     */
    public void deleteUser(User user) {
        final SQLiteDatabase db = getWritableDatabase();

        db.delete(TABLE_USER, KEY_USERNAME + "=" + user.getUsername(), null);
    }

    /**
     * This method returns a list of all users stored in the database and
     * creates corresponding {@link User} objects.
     * 
     * @return a list of users.
     */
    public List<User> getAllUser() {
        final List<User> users = new ArrayList<User>();

        final SQLiteDatabase db = getReadableDatabase();
        final Cursor cursor = db.rawQuery(SELECT_ALL + TABLE_USER, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                final User user =
                        new User(cursor.getString(0), cursor.getString(1),
                                cursor.getString(2));
                users.add(user);
            }
        }
        Log.i(TAG, users.size() + " users were retrieved from the database.");
        return users;
    }

    // -------------------------------------------------------------------------
    // DATA ELEMENT CRUD

    /**
     * This method creates and stores a new data element in the database. The
     * data is taken from the {@link AbstractDataElement} object that is passed
     * to the method.
     * 
     * @param dataElement
     *            the {@link AbstractDataElement} object from which the data
     *            will be taken.
     */
    public void createDataElement(AbstractDataElement dataElement) {
        final SQLiteDatabase db = getWritableDatabase();
        long nextId = getNextId();

        if (dataElement instanceof Node) {
            final Node node = (Node) dataElement;

            // Add the Node
            final ContentValues nodeValues = new ContentValues(4);
            nodeValues.put(KEY_OSMID, nextId);
            nodeValues.put(KEY_DATAELEMENT, nextId);
            nodeValues.put(KEY_LAT, node.getLat());
            nodeValues.put(KEY_LON, node.getLon());

            db.insert(TABLE_NODE, null, nodeValues);
        } else if (dataElement instanceof PolyElement) {
            final PolyElement poly = (PolyElement) dataElement;

            // Add the Nodes of the PolyElement
            List<Node> polyNodes = poly.getNodes();
            final long finalId = nextId + polyNodes.size();
            for (Node node : polyNodes) {
                node.setOsmId(nextId);
                
                final ContentValues nodeValues = new ContentValues(4);
                nodeValues.put(KEY_OSMID, nextId++);
                nodeValues.put(KEY_DATAELEMENT, finalId);
                nodeValues.put(KEY_LAT, node.getLat());
                nodeValues.put(KEY_LON, node.getLon());

                db.insert(TABLE_NODE, null, nodeValues);
            }

            // Add the PolyElement
            final ContentValues polyValues = new ContentValues(2);
            polyValues.put(KEY_OSMID, nextId);
            polyValues.put(KEY_TYPE, poly.getType().getId());

            db.insert(TABLE_POLYELEMENT, null, polyValues);
        } else {
            throw new IllegalArgumentException("Unknown subtype of "
                    + AbstractDataElement.class.getSimpleName() + ": "
                    + dataElement.getClass().getName());
        }

        // Add the DataElement
        dataElement.setOsmId(nextId);
        
        final ContentValues elementValues = new ContentValues(2);
        elementValues.put(KEY_OSMID, nextId);
        elementValues.put(KEY_TYPE, getElementType(dataElement));
        db.insert(TABLE_DATAELEMENT, null, elementValues);

        // Add the Tags
        final ContentValues tagInitial = new ContentValues(1);
        tagInitial.put(KEY_DATAELEMENT, nextId);
        putTags(TABLE_TAGMAP, tagInitial, dataElement.getTags());
    }

    /**
     * This method deletes a specific data element from the database.
     * 
     * @param dataElement
     *            the {@link AbstractDataElement} object whose data should be
     *            deleted.
     */
    public void deleteDataElement(AbstractDataElement dataElement) {
        final SQLiteDatabase db = getWritableDatabase();

        final String isId = "=" + dataElement.getOsmId();
        db.delete(TABLE_TAGMAP, KEY_DATAELEMENT + isId, null);
        db.delete(TABLE_NODE, KEY_DATAELEMENT + isId, null);
        db.delete(TABLE_POLYELEMENT, KEY_OSMID + isId, null);
        db.delete(TABLE_DATAELEMENT, KEY_OSMID + isId, null);
    }

    /**
     * This method returns the number of data elements currently stored in the
     * database.
     * 
     * @return the number of data elements.
     */
    public int getDataElementCount() {
        final Cursor cursor =
                getReadableDatabase().rawQuery(
                        "SELECT COUNT(1) FROM " + TABLE_DATAELEMENT, null);
        cursor.moveToNext();
        final int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    /**
     * This method updates the data for a specific data element stored in the
     * database.
     * 
     * @param dataElement
     *            the {@link AbstractDataElement} object for which the data
     *            should be updated.
     * @return the number of rows that have been updated.
     */
    public void updateDataElement(AbstractDataElement dataElement) {
        // Maybe there is an intelligent way to do this ...
        deleteDataElement(dataElement);
        createDataElement(dataElement);
    }

    /**
     * This method returns a list of all data elements stored in the database
     * and creates corresponding {@link AbstractDataElement} objects.
     * 
     * @return a list of data elements.
     */
    public List<AbstractDataElement> getAllDataElements() {
        List<AbstractDataElement> elements =
                new ArrayList<AbstractDataElement>();

        final SQLiteDatabase db = getReadableDatabase();
        final Cursor elementCursor =
                db.rawQuery(SELECT_ALL + TABLE_DATAELEMENT, null);

        // Read all DataElements
        while (elementCursor.moveToNext()) {
            AbstractDataElement element = null;
            final int elementId = elementCursor.getInt(0);
            final Class<? extends AbstractDataElement> elementClass =
                    getElementClass(elementCursor.getInt(1));

            if (elementClass == Node.class) {
                final Cursor nodeCursor =
                        db.rawQuery("SELECT " + KEY_LAT + "," + KEY_LON
                                + " from " + TABLE_NODE + " WHERE "
                                + KEY_DATAELEMENT + "=" + elementId, null);
                if (nodeCursor.moveToNext()) {
                    element =
                            new Node(elementId, nodeCursor.getDouble(0),
                                    nodeCursor.getDouble(1));
                }
                nodeCursor.close();
            } else if (elementClass == PolyElement.class) {
                final Cursor polyCursor =
                        db.rawQuery("SELECT " + KEY_TYPE + " from "
                                + TABLE_POLYELEMENT + " WHERE " + KEY_OSMID
                                + "=" + elementId, null);
                if (polyCursor.moveToNext()) {
                    final PolyElementType type =
                            PolyElementType.fromId(polyCursor.getInt(0));
                    PolyElement polyElement = new PolyElement(elementId, type);

                    final Cursor nodeCursor =
                            db.rawQuery("SELECT " + KEY_OSMID + "," + KEY_LAT
                                    + "," + KEY_LON + " from " + TABLE_NODE
                                    + " WHERE " + KEY_DATAELEMENT + "="
                                    + elementId, null);
                    while (nodeCursor.moveToNext()) {
                        polyElement.addNode(new Node(nodeCursor.getLong(0),
                                nodeCursor.getDouble(1), nodeCursor
                                        .getDouble(2)));
                    }
                    nodeCursor.close();
                    element = polyElement;
                }
                polyCursor.close();

            } else {
                throw new IllegalStateException("Unknown subtype of "
                        + AbstractDataElement.class.getSimpleName() + ": id="
                        + elementCursor.getInt(1));
            }

            if (element == null) {
                throw new IllegalStateException(
                        AbstractDataElement.class.getSimpleName()
                                + " with the id " + elementId
                                + " cannot be read");
            } else {
                Map<Tag, String> tags =
                        buildTags("SELECT " + KEY_TAGID + "," + KEY_VALUE
                                + " FROM " + TABLE_TAGMAP + " WHERE "
                                + KEY_DATAELEMENT + "=" + elementId);
                element.setTags(tags);
                elements.add(element);
            }
        }

        elementCursor.close();

        return elements;
    }

    /**
     * This method deletes all entries of the {@link AbstractDataElement} table.
     */
    public void deleteAllDataElements() {
        final SQLiteDatabase db = getWritableDatabase();

        db.delete(TABLE_TAGMAP, null, null);
        db.delete(TABLE_NODE, null, null);
        db.delete(TABLE_POLYELEMENT, null, null);
        db.delete(TABLE_DATAELEMENT, null, null);
    }

    /**
     * Returns the last ID used for DataElements.
     * 
     * @author tbrose
     * 
     * @return the last used ID.
     */
    private long getNextId() {
        final SQLiteDatabase db = getReadableDatabase();
        final Cursor cursor =
                db.rawQuery("SELECT " + KEY_OSMID + " FROM "
                        + TABLE_DATAELEMENT + " order by " + KEY_OSMID
                        + " DESC limit 1", null);
        long lastId = 0;
        if (cursor.moveToNext()) {
            lastId = cursor.getLong(0);
            Log.d(TAG, "LAST ID: " + lastId);
        }
        cursor.close();
        return lastId + 1;
    }

    /**
     * TODO
     * 
     * @param element
     * @return
     */
    private static int getElementType(AbstractDataElement element) {
        if (element instanceof PolyElement) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * TODO
     * 
     * @param elementType
     * @return
     */
    private static Class<? extends AbstractDataElement> getElementClass(
            int elementType) {
        if (elementType == 1) {
            return PolyElement.class;
        } else {
            return Node.class;
        }
    }

    /**
     * Executes the query and builds a tagMap from the result of the query.
     * 
     * In the result the first column needs to be the tagId and the second
     * column needs to be the value.
     * 
     * @param query
     *            Well formed SQL query
     * @return The received tagMap
     */
    private Map<Tag, String> buildTags(String query) {
        final Map<Tag, String> tagMap = new LinkedHashMap<Tag, String>();
        final Cursor cursor = getReadableDatabase().rawQuery(query, null);

        while (cursor.moveToNext()) {
            tagMap.put(Tags.getTagWithId(cursor.getInt(0)), cursor.getString(1));
        }

        cursor.close();
        Log.i(TAG, tagMap.size() + " tags were retrieved from the database.");
        return tagMap;
    }

    /**
     * Executes an insert for each tag-pair of the given map.
     * 
     * @param table
     *            The table to insert to
     * @param initialValues
     *            The initial content values for each insertion
     * @param tagMap
     *            The tags to be saved
     */
    private void putTags(String table, ContentValues initialValues,
            Map<Tag, String> tagMap) {
        final SQLiteDatabase db = getWritableDatabase();

        final ContentValues values = new ContentValues();
        values.putAll(initialValues);

        for (Map.Entry<Tag, String> tag : tagMap.entrySet()) {
            values.put(KEY_TAGID, tag.getKey().getId());
            values.put(KEY_VALUE, tag.getValue());

            db.insert(table, null, values);
        }
        Log.i(TAG, "Tags has been added.");
    }

    // -------------------------------------------------------------------------
    // GPS-TRACK CRUD

    /**
     * This method creates and stores a new GPS track in the database. The data
     * is taken from the {@link Track} object that is passed to the method.
     * 
     * @param track
     *            the {@link Track} object from which the data will be taken.
     */
    public long createGPSTrack(Track track) {

        final SQLiteDatabase db = getWritableDatabase();
        final ContentValues values = new ContentValues();

        if (track.getID() != -1) {
            values.put(KEY_INCID, track.getID());
        }
        values.put(KEY_TRACKNAME, track.getTrackName());

        final List<Long> trackPointIDs =
                this.createTrackPoints(track.getTrackPoints());

        final JSONObject json = new JSONObject();
        try {
            json.put("trackpointarray", new JSONArray(trackPointIDs));
        } catch (JSONException e) {
            // ignore exception
        }
        final String arrayList = json.toString();

        values.put(KEY_TRACKPOINTS, arrayList);
        values.put(FLAG_FINISHED, (track.isFinished() ? 1 : 0));

        final long rowID = db.insert(TABLE_GPSTRACK, null, values);
        Log.d(TAG, "New Track with name: " + track.getTrackName() + " created.");
        Log.i(TAG, "GPSTrack " + rowID + " has been added.");
        return rowID;
    }

    /**
     * This method returns the data for a specific GPS track stored in the
     * database and creates the corresponding {@link Track} object.
     * 
     * @param id
     *            the id of the desired GPS track.
     * @return a {@link Track} object for the desired GPS track.
     */
    public Track getGPSTrack(long id) { // NOSONAR

        final SQLiteDatabase db = getReadableDatabase();

        final Track track = new Track();

        final Cursor cursor =
                db.query(TABLE_GPSTRACK, new String[] { KEY_INCID,
                        KEY_TRACKNAME, KEY_TRACKPOINTS, FLAG_FINISHED, },
                        KEY_INCID + "=?", new String[] { String.valueOf(id) },
                        null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            track.setID(cursor.getLong(0));
            track.setTrackName(cursor.getString(1));

            final List<Long> trackPointIDs = new ArrayList<Long>();
            try {
                final JSONObject json = new JSONObject(cursor.getString(2));
                final JSONArray jArray = json.optJSONArray("trackpointarray");

                for (int i = 0; i < jArray.length(); i++) {
                    final long trackPointID = jArray.optInt(i);
                    trackPointIDs.add(trackPointID);
                }
            } catch (JSONException e) {
                // ignore exception
            }

            final List<TrackPoint> trackPoints =
                    this.getTrackPoints(trackPointIDs);
            track.setTrackPoints(trackPoints);

            boolean finishflag = cursor.getInt(3) > 0;
            track.setStatus(finishflag);

            cursor.close();
        }
        return track;
    }

    /**
     * This method deletes a specific GPS track from the database.
     * 
     * @param track
     *            the {@link Track} object whose data should be deleted.
     */
    public void deleteGPSTrack(Track track) {

        final SQLiteDatabase db = getWritableDatabase();

        db.delete(TABLE_GPSTRACK, KEY_INCID + "=?",
                new String[] { String.valueOf(track.getID()) });

        final List<Long> trackPointIDs = new ArrayList<Long>();

        for (TrackPoint trackpoint : track.getTrackPoints()) {
            trackPointIDs.add(trackpoint.getID());
        }
        this.deleteTrackPoints(trackPointIDs);
    }

    /**
     * This method returns the number of GPS tracks currently stored in the
     * database.
     * 
     * @return the number of GPS tracks.
     */
    public int getGPSTrackCount() {

        final SQLiteDatabase db = getReadableDatabase();

        final Cursor cursor = db.rawQuery(SELECT_ALL + TABLE_GPSTRACK, null);
        final int count = cursor.getCount();
        cursor.close();

        return count;
    }

    /**
     * This method updates the data for a specific GPS track stored in the
     * database.
     * 
     * @param track
     *            the {@link Track} object for which the data should be updated.
     * @return the number of rows that have been updated.
     */
    public int updateGPSTrack(Track track) {

        final SQLiteDatabase db = getWritableDatabase();
        final ContentValues values = new ContentValues();

        int count = 0;

        final List<Long> currentTrackPointIDs = new ArrayList<Long>();

        for (TrackPoint point : track.getTrackPoints()) {
            if (point.getID() != -1) {
                currentTrackPointIDs.add(point.getID());
            }
        }

        final List<Long> addedTrackPointIDs =
                this.updateTrackPoints(track.getTrackPoints());

        currentTrackPointIDs.addAll(addedTrackPointIDs);
        Collections.sort(currentTrackPointIDs);

        final JSONObject json = new JSONObject();
        try {
            json.put("trackpointarray", new JSONArray(currentTrackPointIDs));
        } catch (JSONException e) {
            // ignore exception
        }
        final String arrayList = json.toString();

        values.put(KEY_TRACKNAME, track.getTrackName());

        values.put(KEY_TRACKPOINTS, arrayList);

        values.put(FLAG_FINISHED, (track.isFinished() ? 1 : 0));

        if (this.checkIfRecordExists(TABLE_GPSTRACK, KEY_INCID, track.getID())) {
            count +=
                    db.update(TABLE_GPSTRACK, values, KEY_INCID + "=?",
                            new String[] { String.valueOf(track.getID()) });
        } else {
            db.insert(TABLE_GPSTRACK, null, values);
        }

        return count;
    }

    /**
     * This method returns a list of all GPS tracks stored in the database and
     * creates corresponding {@link Track} objects.
     * 
     * @return a list of GPS tracks.
     */
    public List<Track> getAllGPSTracks() {

        final List<Track> gpsTracks = new ArrayList<Track>();

        final SQLiteDatabase db = getReadableDatabase();
        final Cursor cursor = db.rawQuery(SELECT_ALL + TABLE_GPSTRACK, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {

                final Track track = new Track();
                final List<Long> trackPointIDs = new ArrayList<Long>();
                try {
                    Log.d(TAG, "getAllGPSTracks: cursor.getString(0): "
                            + cursor.getString(0) + " cursor.getString(1): "
                            + cursor.getString(1) + " cursor.getString(2): "
                            + cursor.getString(2));
                    final JSONObject json = new JSONObject(cursor.getString(2));
                    final JSONArray jArray =
                            json.optJSONArray("trackpointarray");

                    for (int i = 0; i < jArray.length(); i++) {
                        final long id = jArray.optInt(i);
                        trackPointIDs.add(id);
                    }
                } catch (JSONException e) {
                    // ignore exception
                }

                final List<TrackPoint> trackPoints =
                        this.getTrackPoints(trackPointIDs);
                track.setTrackPoints(trackPoints);

                boolean finishflag = cursor.getInt(3) > 0;
                track.setStatus(finishflag);

                long trackID = cursor.getLong(0);
                track.setID(trackID);
                String trackName = cursor.getString(1);
                track.setTrackName(trackName);

                gpsTracks.add(track);
            }
        }
        cursor.close();
        Log.i(TAG, gpsTracks.size()
                + " GPS tracks were retrieved from the database.");
        return gpsTracks;
    }

    /**
     * This method deletes all entries of the {@link Track} table.
     */
    public void deleteAllGPSTracks() {
        final SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_GPSTRACK, null, null);
        deleteAllTrackPoints();
    }

    // -------------------------------------------------------------------------
    // TRACKPOINT CRUD

    /**
     * This method creates and stores new trackpoints in the database. The data
     * is taken from the {@link TrackPoint} objects that are passed to the
     * method.
     * 
     * @param trackPoints
     *            the {@link List} from which the trackpoints will be taken.
     */
    public List<Long> createTrackPoints(List<TrackPoint> trackPoints) {

        final SQLiteDatabase db = getWritableDatabase();

        final ContentValues values = new ContentValues();

        List<Long> trackPointIDs = new ArrayList<Long>();

        for (TrackPoint point : trackPoints) {
            // if (point.getID() != -1) {
            // values.put(KEY_INCID, point.getID());
            // }
            values.put(KEY_LAT, point.getLat());
            values.put(KEY_LON, point.getLon());
            values.put(KEY_ALT, point.getAlt());
            values.put(KEY_TIME, point.getTime());
            long rowID = db.insert(TABLE_TRACKPOINT, null, values);
            trackPointIDs.add(rowID);
            Log.i(TAG, "TrackPoint " + rowID + " has been added.");
        }
        return trackPointIDs;
    }

    /**
     * This method returns the data for specific trackpoints stored in the
     * database and creates a list of corresponding {@link TrackPoint} objects.
     * 
     * @param trackPointIDs
     *            the ids of the desired trackpoints.
     * @return a {@link List} of the desired trackpoints.
     */
    public List<TrackPoint> getTrackPoints(List<Long> trackPointIDs) {

        final SQLiteDatabase db = getReadableDatabase();

        final List<TrackPoint> trackPoints = new ArrayList<TrackPoint>();

        for (long id : trackPointIDs) {
            final Cursor cursor =
                    db.query(TABLE_TRACKPOINT, new String[] { KEY_INCID,
                            KEY_LAT, KEY_LON, KEY_ALT, KEY_TIME, }, KEY_INCID
                            + "=?", new String[] { String.valueOf(id) }, null,
                            null, null, null);

            final Location loc = new Location("provider");
            if (cursor != null && cursor.moveToFirst()) {
                loc.setLatitude(cursor.getDouble(1));
                loc.setLongitude(cursor.getDouble(2));
                loc.setAltitude(cursor.getDouble(3));
                loc.setTime(cursor.getLong(4));
            }

            final TrackPoint point = new TrackPoint(loc);
            point.setID(id);

            trackPoints.add(point);
            cursor.close();
        }
        return trackPoints;
    }

    /**
     * This method deletes specific trackpoints from the database.
     * 
     * @param trackPointIDs
     *            the ids of the trackpoints that should be deleted.
     */
    public void deleteTrackPoints(List<Long> trackPointIDs) {
        final SQLiteDatabase db = getWritableDatabase();

        for (long id : trackPointIDs) {
            db.delete(TABLE_TRACKPOINT, KEY_INCID + "=?",
                    new String[] { String.valueOf(id) });
        }
    }

    /**
     * This method returns the number of trackpoints currently stored in the
     * database.
     * 
     * @return the number of trackpoints
     */
    public int getTrackPointCount() {
        final SQLiteDatabase db = getReadableDatabase();

        final Cursor cursor = db.rawQuery(SELECT_ALL + TABLE_TRACKPOINT, null);
        final int count = cursor.getCount();
        cursor.close();

        return count;
    }

    /**
     * This method updates the data for specific trackpoints stored in the
     * database.
     * 
     * @param trackPoints
     *            a list of {@link TrackPoint}s that should be updated.
     * @return the number of rows that have been updated.
     */
    public List<Long> updateTrackPoints(List<TrackPoint> trackPoints) {
        final SQLiteDatabase db = getWritableDatabase();

        final ContentValues values = new ContentValues();

        List<Long> trackPointIDs = new ArrayList<Long>();

        for (TrackPoint point : trackPoints) {
            values.put(KEY_LAT, point.getLat());
            values.put(KEY_LON, point.getLon());
            values.put(KEY_ALT, point.getAlt());
            values.put(KEY_TIME, point.getTime());

            if (this.checkIfRecordExists(TABLE_TRACKPOINT, KEY_INCID,
                    point.getID())) {
                db.update(TABLE_TRACKPOINT, values, KEY_INCID + "=?",
                        new String[] { String.valueOf(point.getID()) });
            } else {
                long insertedID = db.insert(TABLE_TRACKPOINT, null, values);
                trackPointIDs.add(insertedID);
            }
        }
        return trackPointIDs;
    }

    /**
     * This method returns a list of all trackpoints stored in the database and
     * creates corresponding {@link TrackPoint} objects.
     * 
     * @return a list of trackpoints.
     */
    public List<TrackPoint> getAllTrackPoints() {
        final List<TrackPoint> trackPoints = new ArrayList<TrackPoint>();

        final SQLiteDatabase db = getReadableDatabase();
        final Cursor cursor = db.rawQuery(SELECT_ALL + TABLE_TRACKPOINT, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {

                final Location loc = new Location("provider");
                loc.setLatitude(cursor.getDouble(1));
                loc.setLongitude(cursor.getDouble(2));
                loc.setAltitude(cursor.getDouble(3));
                loc.setTime(cursor.getLong(4));

                final TrackPoint point = new TrackPoint(loc);
                point.setID(cursor.getLong(0));
                trackPoints.add(point);
            }
        }
        Log.i(TAG, trackPoints.size()
                + " track points were retrieved from the database.");
        return trackPoints;
    }

    /**
     * This method deletes all entries of the {@link TrackPoint} table.
     */
    public void deleteAllTrackPoints() {
        final SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_TRACKPOINT, null, null);
    }

    // ---auxiliary functions---------------------------------------------------

    /**
     * This method checks if a given record exists in a table.
     * 
     * @param tableName
     *            the name of the table.
     * @param field
     *            the column that will be searched.
     * @param value
     *            the given record.
     * @return true if the given record exists, false otherwise.
     */
    private boolean checkIfRecordExists(String tableName, String field,
            long value) {

        final SQLiteDatabase db = getReadableDatabase();
        final String query =
                SELECT_ALL + tableName + " WHERE " + field + " = " + value;
        final Cursor cursor = db.rawQuery(query, null);

        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    // -------------------------------------------------------------------------
    // lastChoice
    /**
     * get lastchoice from a Type(node,track,area,building). TODO
     * 
     * @author tbrose
     * 
     * @param category
     * @return
     */
    public Map<Tag, String> getLastChoice(int category) {
        return buildTags("SELECT " + KEY_TAGID + "," + KEY_VALUE + " FROM "
                + TABLE_LASTCHOICE + " WHERE " + KEY_TYPE + "=" + category);
    }

    /**
     * insert a lastchoice for a given Type. TODO
     * 
     * @author tbrose
     * 
     * @param category
     * @param tags
     */
    public void setLastChoice(int category, Map<Tag, String> tags) {
        final ContentValues categoryValue = new ContentValues();
        categoryValue.put(KEY_TYPE, category);
        putTags(TABLE_LASTCHOICE, categoryValue, tags);
    }
}
