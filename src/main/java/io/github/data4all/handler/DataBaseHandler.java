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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.github.data4all.logger.Log;
import io.github.data4all.model.data.AbstractDataElement;
import io.github.data4all.model.data.ClassifiedTag;
import io.github.data4all.model.data.Node;
import io.github.data4all.model.data.PolyElement;
import io.github.data4all.model.data.PolyElement.PolyElementType;
import io.github.data4all.model.data.Tag;
import io.github.data4all.model.data.Tags;
import io.github.data4all.model.data.Track;
import io.github.data4all.model.data.TrackPoint;
import io.github.data4all.model.data.User;
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
 * 
 */
public class DataBaseHandler extends SQLiteOpenHelper { // NOSONAR

    private static final String TAG = "DataBaseHandler";

    // Database Version
    private static final int DATABASE_VERSION = 4;

    // Database Name
    private static final String DATABASE_NAME = "Data4AllDB";

    // Table Names
    private static final String TABLE_NODE = "nodes";
    private static final String TABLE_DATAELEMENT = "dataelements";
    private static final String TABLE_TAGMAP = "tagmap";
    private static final String TABLE_POLYELEMENT = "polyelements";
    private static final String TABLE_USER = "users";
    private static final String TABLE_WAY = "ways";
    private static final String TABLE_GPSTRACK = "gpstracks";
    private static final String TABLE_TRACKPOINT = "trackpoints";
    private static final String TABLE_LASTCHOICE = "lastChoice";

    // General Column Names
    private static final String KEY_OSMID = "osmid";
    private static final String KEY_INCID = "incid";

    // DataElement Column Names
    private static final String KEY_TAGIDS = "tagids";

    // Node Column Names
    private static final String KEY_LAT = "lat";
    private static final String KEY_LON = "lon";

    // TagMap Column Names
    private static final String KEY_ID = "id";
    private static final String KEY_DATAELEMENT = "element";
    private static final String KEY_TAGID = "tagid";
    private static final String KEY_VALUE = "value";

    // LastChoice Column names
    private static final String TAG_IDS = "tagIds";
    private static final String TYPE = "type";

    // PolyElement Column Names
    private static final String KEY_TYPE = "type";
    private static final String KEY_NODEIDS = "nodeids";

    // User Column Names
    private static final String KEY_USERNAME = "username";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_TOKENSECRET = "tokensecret";

    // GPS Track Column Names
    private static final String KEY_TRACKNAME = "trackname";
    private static final String KEY_TRACKPOINTS = "trackpointids";
    private static final String FLAG_FINISHED = "finished";

    // GPS Trackpoint Column Names
    private static final String KEY_ALT = "altitude";
    private static final String KEY_TIME = "timestamp";

    private long rowID;

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
        final String CREATE_DATAELEMENTS_TABLE = "CREATE TABLE "
                + TABLE_DATAELEMENT + " (" + KEY_OSMID
                + " INTEGER PRIMARY KEY," + KEY_TAGIDS + " TEXT" + ")";
        final String CREATE_NODES_TABLE = "CREATE TABLE " + TABLE_NODE + " ("
                + KEY_OSMID + " INTEGER PRIMARY KEY," + KEY_LAT + " REAL,"
                + KEY_LON + " REAL" + ")";
        final String CREATE_TAGMAP_TABLE = "CREATE TABLE " + TABLE_TAGMAP
                + " (" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_DATAELEMENT
                + " INTEGER," + KEY_TAGID + " INTEGER," + KEY_VALUE + " TEXT"
                + ")";
        final String CREATE_POLYELEMENT_TABLE = "CREATE TABLE "
                + TABLE_POLYELEMENT + " (" + KEY_OSMID
                + " INTEGER PRIMARY KEY," + KEY_TYPE + " TEXT," + KEY_NODEIDS
                + " TEXT" + ")";
        final String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USER + " ("
                + KEY_USERNAME + " TEXT PRIMARY KEY," + KEY_TOKEN + " TEXT,"
                + KEY_TOKENSECRET + " TEXT" + ")";
        final String CREATE_GPSTRACK_TABLE = "CREATE TABLE " + TABLE_GPSTRACK
                + " (" + KEY_INCID + " INTEGER PRIMARY KEY," + KEY_TRACKNAME
                + " TEXT," + KEY_TRACKPOINTS + " TEXT," + FLAG_FINISHED
                + " INTEGER" + ")";
        final String CREATE_TRACKPOINT_TABLE = "CREATE TABLE "
                + TABLE_TRACKPOINT + " (" + KEY_INCID + " INTEGER PRIMARY KEY,"
                + KEY_LAT + " REAL," + KEY_LON + " REAL," + KEY_ALT + " REAL,"
                + KEY_TIME + " REAL" + ")";

        // create table lastChoice
        final String CREATE_LASTCHOICE_TABLE = "CREATE TABLE "
                + TABLE_LASTCHOICE + " (" + TAG_IDS + " TEXT," + TYPE
                + " INTEGER" + ")";

        db.execSQL(CREATE_DATAELEMENTS_TABLE);
        db.execSQL(CREATE_NODES_TABLE);
        db.execSQL(CREATE_TAGMAP_TABLE);
        db.execSQL(CREATE_POLYELEMENT_TABLE);
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_GPSTRACK_TABLE);
        db.execSQL(CREATE_TRACKPOINT_TABLE);
        // Lastchoice
        db.execSQL(CREATE_LASTCHOICE_TABLE);

        Log.i(TAG, "Tables have been created.");
    }

    // Database handling on upgrade
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop tables that already exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TAGMAP);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WAY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NODE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATAELEMENT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POLYELEMENT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GPSTRACK);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRACKPOINT);

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LASTCHOICE);

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
     * This method returns the data for a specific user stored in the database
     * and creates the corresponding {@link User} object.
     * 
     * @param username
     *            the name of the desired user.
     * @return a {@link User} object for the desired user.
     */
    public User getUser(String username) {
        final SQLiteDatabase db = getReadableDatabase();

        final Cursor cursor = db.query(TABLE_USER, new String[] { KEY_USERNAME,
                KEY_TOKEN, KEY_TOKENSECRET }, KEY_USERNAME + "=?",
                new String[] { username }, null, null, null, null);

        String uName = "";
        String token = "";
        String tokenSecret = "";
        if (cursor != null && cursor.moveToFirst()) {
            uName = cursor.getString(0);
            token = cursor.getString(1);
            tokenSecret = cursor.getString(2);
        }
        final User user = new User(uName, token, tokenSecret);

        cursor.close();

        return user;
    }

    /**
     * This method deletes a specific user from the database.
     * 
     * @param user
     *            the {@link User} object whose data should be deleted.
     */
    public void deleteUser(User user) {
        final SQLiteDatabase db = getWritableDatabase();

        db.delete(TABLE_USER, KEY_USERNAME + "=?",
                new String[] { user.getUsername() });
    }

    /**
     * This method deletes a specific user from the database via a given ID.
     * 
     * @param username
     *            the ID of the {@link User} object whose data should be
     *            deleted.
     */
    public void deleteUserByID(String username) {
        final SQLiteDatabase db = getWritableDatabase();

        db.delete(TABLE_USER, KEY_USERNAME + "=?", new String[] { username });
    }

    /**
     * This method returns the number of users currently stored in the database.
     * 
     * @return the number of users.
     */
    public int getUserCount() {
        final SQLiteDatabase db = getReadableDatabase();

        final Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USER, null);
        final int count = cursor.getCount();
        cursor.close();

        return count;
    }

    /**
     * This method updates the data for a specific user stored in the database.
     * 
     * @param user
     *            the {@link User} object for which the data should be updated
     * @return the number of rows that have been updated
     */
    public int updateUser(User user) {
        final SQLiteDatabase db = getWritableDatabase();

        final ContentValues values = new ContentValues();

        values.put(KEY_USERNAME, user.getUsername());
        values.put(KEY_TOKEN, user.getOAuthToken());
        values.put(KEY_TOKENSECRET, user.getOauthTokenSecret());

        return db.update(TABLE_USER, values, KEY_USERNAME + "=?",
                new String[] { user.getUsername() });
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
        final Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USER, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                final User user = new User(cursor.getString(0),
                        cursor.getString(1), cursor.getString(2));
                users.add(user);
            }
        }
        Log.i(TAG, users.size() + " users were retrieved from the database.");
        return users;
    }

    /**
     * This method deletes all entries of the {@link User} table.
     */
    public void deleteAllUser() {
        final SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_USER, null, null);
    }

    // -------------------------------------------------------------------------
    // NODE CRUD

    /**
     * Inserts a {@link Node} into the database.
     * 
     * @param n
     *            The node object.
     * @param id
     *            The ID of the node.
     */
    private void createNode(Node n, long id) {
        final SQLiteDatabase db = getWritableDatabase();
        Log.i(TAG, "trying to add node with " + id);
        final ContentValues values = new ContentValues();
        values.put(KEY_OSMID, id);
        values.put(KEY_LAT, n.getLat());
        values.put(KEY_LON, n.getLon());
        long rowID = db.insert(TABLE_NODE, null, values);
        n.setOsmId(rowID);
    }

    /**
     * This method returns the data for a specific node stored in the database
     * and creates the corresponding {@link Node} object.
     * 
     * @param id
     *            the id of the desired node.
     * @return a {@link Node} object for the desired node.
     */
    private Node getNode(long id) {
        final SQLiteDatabase db = getReadableDatabase();
        final Cursor cursor = db.query(TABLE_NODE, new String[] { KEY_OSMID,
                KEY_LAT, KEY_LON }, KEY_OSMID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        long osmid = 0;
        double lat = 0;
        double lon = 0;

        if (cursor != null && cursor.moveToFirst()) {
            osmid = cursor.getLong(0);
            lat = cursor.getDouble(1);
            lon = cursor.getDouble(2);
        }
        final Node node = new Node(osmid, lat, lon);

        cursor.close();

        return node;
    }

    /**
     * This method deletes a specific node from the database.
     * 
     * @param node
     *            the {@link Node} object whose data should be deleted.
     */
    public void deleteNode(Node node) {
        final SQLiteDatabase db = getWritableDatabase();

        db.delete(TABLE_NODE, KEY_OSMID + "=?",
                new String[] { String.valueOf(node.getOsmId()) });
    }

    /**
     * This method deletes a specific node from the database via a given ID.
     * 
     * @param id
     *            the ID of the {@link Node} object whose data should be
     *            deleted.
     */
    public void deleteNodeByID(long id) {
        final SQLiteDatabase db = getWritableDatabase();

        db.delete(TABLE_NODE, KEY_OSMID + "=?",
                new String[] { String.valueOf(id) });
    }

    /**
     * This method returns the number of nodes currently stored in the database.
     * 
     * @return the number of nodes
     */
    public int getNodeCount() {
        final SQLiteDatabase db = getReadableDatabase();

        final Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NODE, null);
        final int count = cursor.getCount();
        cursor.close();

        return count;
    }

    /**
     * This method updates the data for a specific node stored in the database.
     * 
     * @param node
     *            the {@link Node} object for which the data should be updated.
     * @return the number of rows that have been updated.
     */
    public int updateNode(Node node) {
        final SQLiteDatabase db = getWritableDatabase();
        final ContentValues values = new ContentValues();

        values.put(KEY_LAT, node.getLat());
        values.put(KEY_LON, node.getLon());
        return db.update(TABLE_NODE, values, KEY_OSMID + "=?",
                new String[] { String.valueOf(node.getOsmId()) });
    }

    /**
     * This method returns a list of all nodes stored in the database and
     * creates corresponding {@link Node} objects.
     * 
     * @return a list of nodes.
     */
    public List<Node> getAllNode() {
        final List<Node> nodes = new ArrayList<Node>();

        final SQLiteDatabase db = getReadableDatabase();
        final Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NODE, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                final Node node = new Node(Long.parseLong(cursor.getString(0)),
                        Double.parseDouble(cursor.getString(1)),
                        Double.parseDouble(cursor.getString(2)));
                nodes.add(node);
            }
        }
        Log.i(TAG, nodes.size() + " nodes were retrieved from the database.");
        return nodes;
    }

    /**
     * This method deletes all entries of the {@link Node} table.
     */
    public void deleteAllNode() {
        final SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NODE, null, null);
    }

    // -------------------------------------------------------------------------
    // POLY ELEMENT CRUD

    /**
     * Stores the given {@link PolyElement} in the database.
     * 
     * @param elem
     *            The {@link PolyElement}.
     * @param nextId
     *            The next valid database id.
     */
    private void createPolyElement(PolyElement elem, long nextId) {
        final SQLiteDatabase db = getWritableDatabase();
        final ContentValues values = new ContentValues();

        this.createPolyElementNodes(elem, nextId);

        final List<Long> nodeIDs = new ArrayList<Long>();
        for (Node node : elem.getNodes()) {
            nodeIDs.add(node.getOsmId());
        }

        final JSONObject json = new JSONObject();
        try {
            json.put("nodeIDarray", new JSONArray(nodeIDs));
        } catch (JSONException e) {
            // ignore exception
        }
        final String arrayList = json.toString();

        values.put(KEY_OSMID, elem.getOsmId());
        values.put(KEY_TYPE, elem.getType().toString());
        values.put(KEY_NODEIDS, arrayList);
        long rowID = db.insert(TABLE_POLYELEMENT, null, values);
        Log.i(TAG, "PolyElement " + rowID + " has been added.");
    }

    /**
     * Creates all stored {@link Node} objects for the {@link PolyElement}.
     * 
     * @param elem
     *            The given {@link PolyElement}.
     * @param nextId
     *            The next valid database id.
     */
    private void createPolyElementNodes(PolyElement elem, long nextId) {
        for (Node node : elem.getNodes()) {
            this.createNode(node, nextId);
            node.setOsmId(nextId);
            nextId++;
        }
        elem.setOsmId(nextId);
    }

    /**
     * This method returns the data for a specific poly element stored in the
     * database and creates the corresponding {@link PolyElement} object.
     * 
     * @param id
     *            the id of the desired poly element.
     * @return a {@link PolyElement} object for the desired poly element.
     */
    private PolyElement getPolyElement(long id) {

        final SQLiteDatabase db = getReadableDatabase();

        final Cursor cursor = db.query(TABLE_POLYELEMENT, new String[] {
                KEY_OSMID, KEY_TYPE, KEY_NODEIDS, }, KEY_OSMID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);

        long osmid = 0;
        PolyElementType type = PolyElementType.AREA;
        String toJson = "";
        if (cursor != null && cursor.moveToFirst()) {
            osmid = cursor.getLong(0);
            type = PolyElementType.valueOf(cursor.getString(1));
            toJson = cursor.getString(2);
        }
        final PolyElement polyElement = new PolyElement(osmid, type);

        final List<Node> nodes = new ArrayList<Node>();
        try {
            final JSONObject json = new JSONObject(toJson);
            final JSONArray jArray = json.optJSONArray("nodeIDarray");

            for (int i = 0; i < jArray.length(); i++) {
                final long nodeID = jArray.optLong(i);
                final Node node = this.getNode(nodeID);
                nodes.add(node);
            }
        } catch (JSONException e) {
            // ignore exception
        }

        polyElement.addNodes(nodes, false);

        cursor.close();

        return polyElement;
    }

    /**
     * This method deletes a specific poly element from the database.
     * 
     * @param polyElement
     *            the {@link PolyElement} object whose data should be deleted.
     */
    private void deletePolyElement(PolyElement polyElement) {

        final SQLiteDatabase db = getWritableDatabase();

        db.delete(TABLE_POLYELEMENT, KEY_OSMID + "=?",
                new String[] { String.valueOf(polyElement.getOsmId()) });

        for (Node node : polyElement.getNodes()) {
            this.deleteNode(node);
        }
    }

    /**
     * This method deletes a specific PolyElement from the database via a given
     * ID.
     * 
     * @param id
     *            the ID of the {@link PolyElement} object whose data should be
     *            deleted.
     */
    private void deletePolyElementByID(long id) {
        final SQLiteDatabase db = getWritableDatabase();

        final PolyElement pE = getPolyElement(id);
        for (Node n : pE.getNodes()) {
            this.deleteNodeByID(n.getOsmId());
        }

        db.delete(TABLE_POLYELEMENT, KEY_OSMID + "=?",
                new String[] { String.valueOf(id) });
    }

    /**
     * This method returns the number of poly elements currently stored in the
     * database.
     * 
     * @return the number of poly elements.
     */
    public int getPolyElementCount() {
        final SQLiteDatabase db = getReadableDatabase();

        final Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_POLYELEMENT,
                null);
        final int count = cursor.getCount();
        cursor.close();

        return count;
    }

    /**
     * This method updates the data for a specific poly element stored in the
     * database.
     * 
     * @param polyElement
     *            the {@link PolyElement} object for which the data should be
     *            updated.
     * @return the number of rows that have been updated.
     */
    public int updatePolyElement(PolyElement polyElement) {

        final SQLiteDatabase db = getWritableDatabase();
        final ContentValues values = new ContentValues();

        if (polyElement.getOsmId() != -1) {
            values.put(KEY_OSMID, polyElement.getOsmId());
        }
        values.put(KEY_TYPE, polyElement.getType().toString());

        final List<Long> nodeIDs = new ArrayList<Long>();

        for (Node node : polyElement.getNodes()) {
            nodeIDs.add(node.getOsmId());
            if (this.checkIfRecordExists(TABLE_NODE, KEY_OSMID, node.getOsmId())) {
                this.updateNode(node);
            } else {
                this.createNode(node, this.getNextId());
            }
        }

        final JSONObject json = new JSONObject();
        try {
            json.put("nodeIDarray", new JSONArray(nodeIDs));
        } catch (JSONException e) {
            // TODO: handle exception
        }
        final String arrayList = json.toString();

        values.put(KEY_NODEIDS, arrayList);

        return db.update(TABLE_POLYELEMENT, values, KEY_OSMID + "=?",
                new String[] { String.valueOf(polyElement.getOsmId()) });
    }

    /**
     * This method returns a list of all poly elements stored in the database
     * and creates corresponding {@link PolyElement} objects.
     * 
     * @return a list of poly elements.
     */
    public List<PolyElement> getAllPolyElements() {

        final List<PolyElement> polyElements = new ArrayList<PolyElement>();

        final SQLiteDatabase db = getReadableDatabase();
        final Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_POLYELEMENT,
                null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                final PolyElement polyElement = new PolyElement(
                        Long.parseLong(cursor.getString(0)),
                        PolyElementType.valueOf(cursor.getString(1)));

                final List<Node> nodes = new ArrayList<Node>();
                try {
                    final JSONObject json = new JSONObject(cursor.getString(2));
                    final JSONArray jArray = json.optJSONArray("nodeIDarray");

                    for (int i = 0; i < jArray.length(); i++) {
                        final long nodeID = jArray.optLong(i);
                        final Node node = this.getNode(nodeID);
                        nodes.add(node);
                    }
                } catch (JSONException e) {
                    // ignore exception
                }

                polyElement.addNodes(nodes, false);
                polyElements.add(polyElement);
            }
        }
        Log.i(TAG, polyElements.size()
                + " poly elements were retrieved from the database.");
        return polyElements;
    }

    /**
     * This method deletes all entries of the {@link PolyElement} table.
     */
    public void deleteAllPolyElements() {
        final SQLiteDatabase db = getWritableDatabase();

        final List<PolyElement> pEs = this.getAllPolyElements();

        for (PolyElement pE : pEs) {
            for (Node n : pE.getNodes()) {
                this.deleteNodeByID(n.getOsmId());
            }
        }

        db.delete(TABLE_POLYELEMENT, null, null);
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
    public void createDataElement(AbstractDataElement dataElement) { // NOSONAR

        final SQLiteDatabase db = getWritableDatabase();

        final Map<Tag, String> tagMap = dataElement.getTags();
        final List<Integer> tagIDs = new ArrayList<Integer>();
        final ContentValues values = new ContentValues();

        if (dataElement instanceof Node) {
            this.createNode((Node) dataElement, getNextId());
        } else {
            this.createPolyElement((PolyElement) dataElement, getNextId());
        }

        for (Map.Entry<Tag, String> tag : tagMap.entrySet()) {
            tagIDs.add(tag.getKey().getId());
        }

        this.createTagMap(dataElement.getOsmId(), tagMap);

        final JSONObject json = new JSONObject();
        try {
            json.put("tagIDarray", new JSONArray(tagIDs));
        } catch (JSONException e) {
            // ignore exception
        }
        final String arrayList = json.toString();

        values.put(KEY_OSMID, dataElement.getOsmId());
        values.put(KEY_TAGIDS, arrayList);

        long rowID = db.insert(TABLE_DATAELEMENT, null, values);
        Log.i(TAG, "DataElement " + rowID + " has been added.");
    }

    /**
     * Returns the last used ID from the database.
     * 
     * @return the last used ID.
     */
    private long getNextId() {
        final SQLiteDatabase db = getReadableDatabase();
        final Cursor cursor = db.rawQuery("SELECT " + KEY_OSMID + " FROM "
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
     * This method returns the data for a specific data element stored in the
     * database and creates the corresponding {@link AbstractDataElement}
     * object.
     * 
     * @param id
     *            the id of the desired data element
     * @return a {@link AbstractDataElement} object for the desired data element
     */
    public AbstractDataElement getDataElement(long id) { // NOSONAR

        final SQLiteDatabase db = getReadableDatabase();

        AbstractDataElement dataElement; // NOSONAR

        final Cursor cursor = db.query(TABLE_DATAELEMENT, new String[] {
                KEY_OSMID, KEY_TAGIDS, }, KEY_OSMID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);

        if (this.checkIfRecordExists(TABLE_POLYELEMENT, KEY_OSMID, id)) {
            dataElement = this.getPolyElement(id);
        } else {
            dataElement = this.getNode(id);
        }

        if (cursor != null && cursor.moveToFirst()) {

            final List<Integer> tagIDs = new ArrayList<Integer>();
            try {
                final JSONObject json = new JSONObject(cursor.getString(1));
                final JSONArray jArray = json.optJSONArray("tagIDarray");

                for (int i = 0; i < jArray.length(); i++) {
                    final int tagID = jArray.optInt(i);
                    tagIDs.add(tagID);
                }
            } catch (JSONException e) {
                // ignore exception
            }

            final Map<Tag, String> tagMap = this.getTagMap(tagIDs);
            dataElement.addTags(tagMap);

            cursor.close();
        }

        return dataElement;
    }

    /**
     * This method deletes a specific data element from the database.
     * 
     * @param dataElement
     *            the {@link AbstractDataElement} object whose data should be
     *            deleted.
     */
    public void deleteDataElement(AbstractDataElement dataElement) { // NOSONAR

        final SQLiteDatabase db = getWritableDatabase();

        db.delete(TABLE_DATAELEMENT, KEY_OSMID + "=?",
                new String[] { String.valueOf(dataElement.getOsmId()) });

        final List<Integer> tagIDs = new ArrayList<Integer>();

        for (Map.Entry<Tag, String> tag : dataElement.getTags().entrySet()) {
            tagIDs.add(tag.getKey().getId());
        }
        this.deleteTagMap(dataElement.getOsmId());

        if (dataElement instanceof PolyElement) {
            this.deletePolyElement((PolyElement) dataElement);
        } else {
            this.deleteNode((Node) dataElement);
        }
    }

    /**
     * This method deletes a specific DataElement from the database via a given
     * ID.
     * 
     * @param id
     *            the ID of the {@link AbstractDataElement} object whose data
     *            should be deleted.
     */
    public void deleteDataElementByID(long id) {
        final SQLiteDatabase db = getWritableDatabase();

        AbstractDataElement dE = getDataElement(id);
        List<Integer> tagIDs = new ArrayList<Integer>();

        for (Map.Entry<Tag, String> tag : dE.getTags().entrySet()) {
            tagIDs.add(tag.getKey().getId());
        }

        db.delete(TABLE_DATAELEMENT, KEY_OSMID + "=?",
                new String[] { String.valueOf(id) });
        this.deleteTagMap(dE.getOsmId());

        if (dE instanceof PolyElement) {
            this.deletePolyElementByID(id);
        } else {
            this.deleteNodeByID(id);
        }
    }

    /**
     * This method returns the number of data elements currently stored in the
     * database.
     * 
     * @return the number of data elements.
     */
    public int getDataElementCount() {

        final SQLiteDatabase db = getReadableDatabase();

        final Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_DATAELEMENT,
                null);
        final int count = cursor.getCount();
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
        this.deleteDataElement(dataElement);
        this.createDataElement(dataElement);
    }

    /**
     * This method returns a list of all data elements stored in the database
     * and creates corresponding {@link AbstractDataElement} objects.
     * 
     * @return a list of data elements.
     */
    public List<AbstractDataElement> getAllDataElements() {

        final List<AbstractDataElement> dataElements = new ArrayList<AbstractDataElement>();

        final SQLiteDatabase db = getReadableDatabase();
        final Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_DATAELEMENT,
                null);

        AbstractDataElement dataElement; // NOSONAR

        if (cursor != null) {
            while (cursor.moveToNext()) {
                if (this.checkIfRecordExists(TABLE_POLYELEMENT, KEY_OSMID,
                        cursor.getInt(0))) {
                    dataElement = this.getPolyElement(cursor.getInt(0));
                } else {
                    dataElement = this.getNode(cursor.getInt(0));
                }

                final List<Integer> tagIDs = new ArrayList<Integer>();
                try {
                    final JSONObject json = new JSONObject(cursor.getString(1));
                    final JSONArray jArray = json.optJSONArray("tagIDarray");

                    for (int i = 0; i < jArray.length(); i++) {
                        final int tagID = jArray.optInt(i);
                        tagIDs.add(tagID);
                    }
                } catch (JSONException e) {
                    // ignore exceptions
                }

                final Map<Tag, String> tagMap = this.getTagMap(dataElement
                        .getOsmId());
                dataElement.addTags(tagMap);

                dataElements.add(dataElement);
            }
        }

        Log.i(TAG, dataElements.size()
                + " data elements were retrieved from the database.");
        return dataElements;
    }

    /**
     * This method deletes all entries of the {@link AbstractDataElement} table.
     */
    public void deleteAllDataElements() {
        final SQLiteDatabase db = getWritableDatabase();
        this.deleteAllNode();
        this.deleteAllPolyElements();
        this.deleteAllTagMap();
        db.delete(TABLE_DATAELEMENT, null, null);
    }

    // -------------------------------------------------------------------------
    // TAG MAP CRUD

    /**
     * This method creates and stores a new tag map in the database. The data is
     * taken from the {@link Map} object that is passed to the method.
     * 
     * @param dataElementId
     *            The ID of the data element.
     * @param tagMap
     *            the {@link Map} object from which the data will be taken
     */
    public void createTagMap(long dataElementId, Map<Tag, String> tagMap) {

        final SQLiteDatabase db = getWritableDatabase();

        final ContentValues values = new ContentValues();

        for (Map.Entry<Tag, String> tag : tagMap.entrySet()) {
            values.put(KEY_DATAELEMENT, dataElementId);
            values.put(KEY_TAGID, tag.getKey().getId());
            values.put(KEY_VALUE, tag.getValue());
            final long rowID = db.insert(TABLE_TAGMAP, null, values);
            Log.i(TAG, "Tag " + rowID + " has been added.");
        }
    }

    /**
     * This method returns the data for specific tags stored in the database and
     * creates the corresponding {@link Map} object.
     * 
     * @param tagIDs
     *            the IDs of the desired tags.
     * @return a {@link Map} object for the desired tags.
     */
    public Map<Tag, String> getTagMap(List<Integer> tagIDs) {

        final SQLiteDatabase db = getReadableDatabase();

        final Map<Tag, String> tagMap = new LinkedHashMap<Tag, String>();

        for (int id : tagIDs) {
            final Cursor cursor = db
                    .query(TABLE_TAGMAP, new String[] { KEY_ID,
                            KEY_DATAELEMENT, KEY_TAGID, KEY_VALUE }, KEY_ID
                            + "=?", new String[] { String.valueOf(id) }, null,
                            null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                tagMap.put(Tags.getTagWithId(Integer.parseInt(cursor
                        .getString(2))), cursor.getString(3));
                cursor.close();
            }
        }
        Log.i(TAG, tagMap.size() + " tags were retrieved from the database.");
        return tagMap;
    }

    /**
     * This method returns the data for specific tags stored in the database and
     * creates the corresponding {@link Map} object.
     * 
     * @param dataElementId
     *            The ID of the data element.
     * @return A {@link Map} of tags.
     */
    public Map<Tag, String> getTagMap(long dataElementId) {

        final SQLiteDatabase db = getReadableDatabase();
        final Map<Tag, String> tagMap = new LinkedHashMap<Tag, String>();

        final Cursor cursor = db.query(TABLE_TAGMAP, new String[] { KEY_ID,
                KEY_DATAELEMENT, KEY_TAGID, KEY_VALUE, }, KEY_DATAELEMENT
                + "=?", new String[] { String.valueOf(dataElementId) }, null,
                null, null, null);
        while (cursor.moveToNext()) {
            tagMap.put(
                    Tags.getTagWithId(Integer.parseInt(cursor.getString(2))),
                    cursor.getString(3));
        }
        cursor.close();
        Log.i(TAG, tagMap.size() + " tags were retrieved from the database.");
        return tagMap;
    }

    /**
     * This method deletes specific tags from the database.
     * 
     * @param dataElementId
     *            The ID of the data element.
     * @param tagIDs
     *            The list of tag IDs. the tags whose data should be deleted
     */
    private void deleteTagMap(long dataElementId) {
        final SQLiteDatabase db = getWritableDatabase();

        db.delete(TABLE_TAGMAP, KEY_DATAELEMENT + "=?",
                new String[] { String.valueOf(dataElementId) });

    }

    /**
     * This method returns the number of tags currently stored in the database.
     * 
     * @param dataElement
     *            The ID of the data element.
     * @return the number of tags.
     */
    public int getTagMapCount(long dataElement) {
        final SQLiteDatabase db = getReadableDatabase();

        final Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TAGMAP
                + " WHERE " + KEY_DATAELEMENT + "=?",
                new String[] { String.valueOf(dataElement) });
        final int count = cursor.getCount();
        Log.d(TAG, "getTagMapCount for dataElement: " + dataElement
                + " count: " + count);
        cursor.close();
        return count;
    }

    /**
     * This method deletes all entries of the TagMap table.
     */
    public void deleteAllTagMap() {
        final SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_TAGMAP, null, null);
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
    public void createGPSTrack(Track track) {

        final SQLiteDatabase db = getWritableDatabase();
        final ContentValues values = new ContentValues();

        if (track.getID() != -1) {
            values.put(KEY_INCID, track.getID());
        }
        values.put(KEY_TRACKNAME, track.getTrackName());

        final List<Long> trackPointIDs = this.createTrackPoints(track
                .getTrackPoints());

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

        final Cursor cursor = db.query(TABLE_GPSTRACK, new String[] {
                KEY_INCID, KEY_TRACKNAME, KEY_TRACKPOINTS, FLAG_FINISHED, }, KEY_INCID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);

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

            final List<TrackPoint> trackPoints = this
                    .getTrackPoints(trackPointIDs);
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

        final Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_GPSTRACK,
                null);
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
        
        for(TrackPoint point : track.getTrackPoints()){
            if(point.getID() != -1){                
                currentTrackPointIDs.add(point.getID());
            }
        }
        
        final List<Long> addedTrackPointIDs = this.updateTrackPoints(track.getTrackPoints());
        
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
            count += db.update(TABLE_GPSTRACK, values, KEY_INCID + "=?",
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
        final Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_GPSTRACK,
                null);


        if (cursor != null) {
            while (cursor.moveToNext()) {

                final Track track = new Track();
                final List<Long> trackPointIDs = new ArrayList<Long>();
                try {
                    final JSONObject json = new JSONObject(cursor.getString(2));
                    final JSONArray jArray = json
                            .optJSONArray("trackpointarray");

                    for (int i = 0; i < jArray.length(); i++) {
                        final long id = jArray.optInt(i);
                        trackPointIDs.add(id);
                    }
                } catch (JSONException e) {
                    // ignore exception
                }

                final List<TrackPoint> trackPoints = this
                        .getTrackPoints(trackPointIDs);
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
            if (point.getID() != -1) {
                values.put(KEY_INCID, point.getID());
            }
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
            final Cursor cursor = db.query(TABLE_TRACKPOINT, new String[] {
                    KEY_INCID, KEY_LAT, KEY_LON, KEY_ALT, KEY_TIME, },
                    KEY_INCID + "=?", new String[] { String.valueOf(id) },
                    null, null, null, null);

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

        final Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TRACKPOINT,
                null);
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
        final Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TRACKPOINT,
                null);

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
        final String query = "SELECT * FROM " + tableName + " WHERE " + field
                + " = " + value;
        final Cursor cursor = db.rawQuery(query, null);

        if (cursor.getCount() <= 0) {
            return false;
        }
        return true;
    }

    // --------------------------------------------------------------------------------------------------------------
    // lastChoice
    /**
     * get lastchoice from a Type(node,track,area,building)
     * 
     * @param kategorie
     * @return
     * @author Steeve
     */
    public List<Integer> getLastChoiceId(Integer kategorie) {

        final SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT " + TAG_IDS + " FROM " + TABLE_LASTCHOICE
                + " WHERE " + TYPE + " = " + kategorie;
        final Cursor cursor = db.rawQuery(query, null);
        String tagIds = null;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                tagIds = cursor.getString(0);
            }
        }
        if (tagIds == null) {
            return null;
        }
        List<Integer> result = new LinkedList<Integer>();
        String[] ids = tagIds.split(",");
        for (String id : ids) {
            result.add(Integer.valueOf(id));
        }

        return result;
    }

    /**
     * insert a lastchoice for a given Type
     * 
     * @param kategorie
     * @param tagIds
     * @return
     * @author Steeve
     */
    public void insertOrUpdateLastChoice(Integer kategorie, List<Integer> tagIds) {
        final SQLiteDatabase db = getReadableDatabase();
        final ContentValues values = new ContentValues();
        if (getLastChoiceId(kategorie) == null) {
            values.put(KEY_TAGID, kategorie);
            values.put(KEY_VALUE, join(tagIds, ","));
            rowID = db.insert(TABLE_TAGMAP, null, values);
        } else {
            values.put(KEY_TAGID, kategorie);
            values.put(KEY_VALUE, join(tagIds, ","));
            // update lastchoice set TYPE=kategorie, TAG_IDS=tagIds where
            // TYPE=kategorie
            rowID = db.update(TABLE_TAGMAP, values, KEY_TAGID + "=?",
                    new String[] { String.valueOf(kategorie) });

        }
    }

    /*
     * /**
     * 
     * @param kategorie
     * 
     * @return
     *//*
        * public Map<Tag, String> getTagMapLastChoice(Integer kategorie){
        * List<Integer> tagid=getLastChoiceId(kategorie); if(tagid==null){
        * return null; } return getTagMap(tagid); }
        */
    /**
     * separate elements with a given separator
     * 
     * @param elements
     * @param separator
     * @return
     * @autthor steeve
     */
    public String join(List<Integer> elements, String separator) {

        if (elements == null || elements.isEmpty()) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (Iterator<Integer> iterator = elements.iterator(); iterator
                .hasNext();) {
            Integer integer = (Integer) iterator.next();
            builder.append(integer.toString());
            if (iterator.hasNext()) {
                builder.append(separator);
            }
        }
        return builder.toString();
    }
}
