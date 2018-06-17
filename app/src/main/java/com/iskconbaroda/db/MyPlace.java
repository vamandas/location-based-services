package com.iskconbaroda.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

public class MyPlace {

    public static final int RADIUS_DEFAULT = 100;
    public static final int IS_FENCE_ADDED = 1;
    public static final int DEFAULT_IS_FENCE_ADDED = 0;

    private String address;
    private long dbId;
    private String title;
    private String reminder;
    private String message;
    private int actionType;
    private int fenceStatus;
    private String contactNo;
    private double latitude;
    private double longitude;
    private int isAddedToFence;
    private long createdTime;
    private int radius;

    public MyPlace() {
    }

    public MyPlace(Cursor dbCursor) {
        dbId = dbCursor.getLong(dbCursor.getColumnIndex(MyPlaceColumns._ID));
        title = dbCursor.getString(dbCursor.getColumnIndex(MyPlaceColumns.TITLE));
        reminder = dbCursor.getString(dbCursor.getColumnIndex(MyPlaceColumns.REMINDER));
        message = dbCursor.getString(dbCursor.getColumnIndex(MyPlaceColumns.MESSAGE));
        contactNo = dbCursor.getString(dbCursor.getColumnIndex(MyPlaceColumns.CONTACT_NO));
        actionType = dbCursor.getInt(dbCursor.getColumnIndex(MyPlaceColumns.ACTION_TYPE));
        fenceStatus = dbCursor.getInt(dbCursor.getColumnIndex(MyPlaceColumns.FENCE_STATUS));
        address = dbCursor.getString(dbCursor.getColumnIndex(MyPlaceColumns.ADDRESS));
        latitude = dbCursor.getDouble(dbCursor.getColumnIndex(MyPlaceColumns.LATITUDE));
        longitude = dbCursor.getDouble(dbCursor.getColumnIndex(MyPlaceColumns.LONGITUDE));
        isAddedToFence = dbCursor.getInt(dbCursor.getColumnIndex(MyPlaceColumns.IS_ADDED_FENCE));
        createdTime = dbCursor.getLong(dbCursor.getColumnIndex(MyPlaceColumns.CREATED_TIME));
        radius = dbCursor.getInt(dbCursor.getColumnIndex(MyPlaceColumns.FENCE_RADIUS));
    }
    public MyPlace(String address, String title, String message, String reminder , String contactNo, int actionType, int fenceStatus, double latitude, double longitude, boolean isAddedToFence, long createdTime, int radius) {
        super();
        this.address = address;
        this.title = title;
        this.reminder = reminder;
        this.message = message;
        this.contactNo = contactNo;
        this.actionType = actionType;
        this.fenceStatus = fenceStatus;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isAddedToFence = (isAddedToFence) ? IS_FENCE_ADDED : DEFAULT_IS_FENCE_ADDED;
        this.createdTime = createdTime;
        this.radius = radius;
    }

    public MyPlace(String address, String title, String message, String reminder, String contactNo, int actionType, int fenceStatus, double latitude, double longitude, long createdTime) {
        this(address, title, message, reminder, contactNo,actionType, fenceStatus, latitude, longitude, false, createdTime, RADIUS_DEFAULT);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReminder() {
        return reminder;
    }

    public void setReminder(String reminder) {
        this.reminder = reminder;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public int getActionType() {
        return actionType;
    }

    public void setActionType(int actionType) {
        this.actionType = actionType;
    }

    public int getFenceStatus() {
        return fenceStatus;
    }

    public void setFenceStatus(int fenceStatus) {
        this.fenceStatus = fenceStatus;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean isAddedToFence() {
        return isAddedToFence == IS_FENCE_ADDED;
    }

    public void setIsAddedToFence(boolean isAddedToFence) {
        this.isAddedToFence = isAddedToFence ? IS_FENCE_ADDED : DEFAULT_IS_FENCE_ADDED;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public long getDbId() {
        return dbId;
    }

    public void setDbId(long dbId) {
        this.dbId = dbId;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public ContentValues toContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MyPlaceColumns.REMINDER, reminder);
        contentValues.put(MyPlaceColumns.MESSAGE, message);
        contentValues.put(MyPlaceColumns.ADDRESS, address);
        contentValues.put(MyPlaceColumns.CONTACT_NO, contactNo);
        contentValues.put(MyPlaceColumns.ACTION_TYPE, actionType);
        contentValues.put(MyPlaceColumns.FENCE_STATUS, fenceStatus);
        contentValues.put(MyPlaceColumns.LATITUDE, latitude);
        contentValues.put(MyPlaceColumns.LONGITUDE, longitude);
        contentValues.put(MyPlaceColumns.IS_ADDED_FENCE, isAddedToFence);
        contentValues.put(MyPlaceColumns.FENCE_RADIUS, radius);
        contentValues.put(MyPlaceColumns.CREATED_TIME, createdTime);
        contentValues.put(MyPlaceColumns.TITLE, title);
        return contentValues;
    }

    @Override
    public boolean equals(Object o) {

        return this.dbId == ((MyPlace) o).dbId;
    }

    public Uri getShareUri() {
        return Uri.parse("geo:" + getLatitude() + "," + longitude);
    }

    public static interface MyPlaceTable {
        public static final String TABLE_NAME = "my_palces";
        public static final String CREATE_QUERY = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( " +
                MyPlaceColumns._ID + " " + DBConstants.DB_TYPE_PRIMARY_KEY + "," +
                MyPlaceColumns.TITLE + " " + DBConstants.DB_TYPE_TEXT + "," +
                MyPlaceColumns.MESSAGE + " " + DBConstants.DB_TYPE_TEXT + "," +
                MyPlaceColumns.REMINDER + " " + DBConstants.DB_TYPE_TEXT + "," +
                MyPlaceColumns.CONTACT_NO + " " + DBConstants.DB_TYPE_TEXT + "," +
                MyPlaceColumns.ADDRESS + " " + DBConstants.DB_TYPE_TEXT + "," +
                MyPlaceColumns.LATITUDE + " " + DBConstants.DB_TYPE_REAL + "," +
                MyPlaceColumns.LONGITUDE + " " + DBConstants.DB_TYPE_REAL + "," +
                MyPlaceColumns.ACTION_TYPE + " " + DBConstants.DB_TYPE_INTEGER + "," +
                MyPlaceColumns.FENCE_STATUS + " " + DBConstants.DB_TYPE_INTEGER + "," +
                MyPlaceColumns.CREATED_TIME + " " + DBConstants.DB_TYPE_INTEGER + "," +
                MyPlaceColumns.FENCE_RADIUS + " " + DBConstants.DB_TYPE_INTEGER + "," +
                MyPlaceColumns.IS_ADDED_FENCE + " " + DBConstants.DB_TYPE_INTEGER + " DEFAULT " + DEFAULT_IS_FENCE_ADDED +
                ");";
        public static final String DROP_QUERY = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static interface MyPlaceColumns extends BaseColumns {
        public static final String ADDRESS = "address";
        public static final String REMINDER = "reminder";
        public static final String MESSAGE = "message";
        public static final String CONTACT_NO = "contact_no";
        public static final String TITLE = "title";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String ACTION_TYPE = "action_type";
        public static final String FENCE_STATUS = "fence_status";
        public static final String IS_ADDED_FENCE = "is_fence";
        public static final String CREATED_TIME = "created_time";
        public static final String FENCE_RADIUS = "radius";


        public static final String[] COLUMN_ALL = {_ID, TITLE, MESSAGE, REMINDER, CONTACT_NO, ADDRESS, LATITUDE, LONGITUDE, ACTION_TYPE, FENCE_STATUS, IS_ADDED_FENCE, CREATED_TIME, FENCE_RADIUS};

    }

    @Override
    public String toString() {
        return "MyPlace{" +
                "address='" + address + '\'' +
                ", dbId=" + dbId +
                ", title='" + title + '\'' +
                ", message='" + message + '\'' +
                ", reminder='" + reminder + '\'' +
                ", actionType=" + actionType +
                ", fenceStatus=" + fenceStatus +
                ", contactNo='" + contactNo + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", isAddedToFence=" + isAddedToFence +
                ", createdTime=" + createdTime +
                ", radius=" + radius +
                '}';
    }
}
