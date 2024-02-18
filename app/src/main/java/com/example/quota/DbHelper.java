    package com.example.quota;

    import android.annotation.SuppressLint;
    import android.content.ContentValues;
    import android.content.Context;
    import android.database.Cursor;
    import android.database.SQLException;
    import android.database.sqlite.SQLiteConstraintException;
    import android.database.sqlite.SQLiteDatabase;
    import android.database.sqlite.SQLiteOpenHelper;
    import android.util.Log;

    import androidx.annotation.Nullable;

    public class DbHelper extends SQLiteOpenHelper {
        private static final int VERSION = 2;

        //class table
        private static final String CLASS_TABLE_NAME = "CLASS_TABLE";
        public static final String C_ID = "_CID";
        public static final String SECTION_NAME_KEY = "SECTION_NAME";
        public static final String GRADE_LEVEL_KEY = "GRADE_LEVEL";

        private static final String CREATE_CLASS_TABLE =
                "CREATE TABLE " + CLASS_TABLE_NAME + "(" +
                        C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        SECTION_NAME_KEY + " TEXT NOT NULL, " +
                        GRADE_LEVEL_KEY + " TEXT NOT NULL, " +
                        " UNIQUE (" + SECTION_NAME_KEY + ", " + GRADE_LEVEL_KEY + ")" +
                        ");";

        private static final String DROP_CLASS_TABLE = "DROP TABLE IF EXISTS " + CLASS_TABLE_NAME;
        private static final String SELECT_CLASS_TABLE = "SELECT * FROM " + CLASS_TABLE_NAME;

        //student table

        private static final String STUDENT_TABLE_NAME = "STUDENT_TABLE";
        public static final String S_ID = "_SID";
        public static final String STUDENT_NAME_KEY = "STUDENT_NAME";
        public static final String STUDENT_ROLL_KEY = "ROLL";

        private static final String CREATE_STUDENT_TABLE =
                "CREATE TABLE " + STUDENT_TABLE_NAME +
                        "( " +
                        S_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        C_ID + " INTEGER NOT NULL, " +
                        STUDENT_NAME_KEY + " TEXT NOT NULL, " +
                        STUDENT_ROLL_KEY + " INTEGER, " +
                        " FOREIGN KEY ( " + C_ID + ") REFERENCES " + CLASS_TABLE_NAME + "(" + C_ID + ")" +
                        ");";

        private static final String DROP_STUDENT_TABLE = "DROP TABLE IF EXISTS " + STUDENT_TABLE_NAME;
        private static final String SELECT_STUDENT_TABLE = "SELECT * FROM " + STUDENT_TABLE_NAME;


        //STATUS TABLE

        private static final String STATUS_TABLE_NAME = "STATUS_TABLE";
        public static final String STATUS_ID = "_STATUS_ID";
        public static final String DATE_KEY = "STATUS_DATE";
        public static final String STATUS_KEY = "STATUS";
        public static final String STUDENT_NAME = "STUDENT_NAME";

        private static final String CREATE_STATUS_TABLE =
                "CREATE TABLE " + STATUS_TABLE_NAME +
                        "(" +
                        STATUS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        S_ID + " INTEGER NOT NULL, " +
                        C_ID + " INTEGER NOT NULL, " +
                        DATE_KEY + " DATE NOT NULL, " +
                        STATUS_KEY + " TEXT NOT NULL, " +
                        STUDENT_NAME + " TEXT NOT NULL, " +  // This line defines the STUDENT_NAME column
                        " UNIQUE (" + S_ID + "," + DATE_KEY + ")," +
                        " FOREIGN KEY (" + S_ID + ") REFERENCES " + STUDENT_TABLE_NAME + "( " + S_ID + ")," +
                        " FOREIGN KEY (" + C_ID + ") REFERENCES " + CLASS_TABLE_NAME + "( " + C_ID + ")" +
                        ");";

        private static final String DROP_STATUS_TABLE = "DROP TABLE IF EXISTS " + STATUS_TABLE_NAME;
        private static final String SELECT_STATUS_TABLE = "SELECT * FROM " + STATUS_TABLE_NAME;


        public DbHelper(@Nullable Context context) {
            super(context, "Attendance.db", null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                Log.i("DbHelper", "Creating tables...");
                db.execSQL(CREATE_CLASS_TABLE);
                Log.i("DbHelper", "CLASS_TABLE created successfully");
                db.execSQL(CREATE_STUDENT_TABLE);
                Log.i("DbHelper", "STUDENT_TABLE created successfully");
                db.execSQL(CREATE_STATUS_TABLE);
                Log.i("DbHelper", "STATUS_TABLE created successfully");
            } catch (SQLException e) {
                // Log the error or handle it appropriately
                Log.e("DbHelper", "Error creating tables: " + e.getMessage());
            }
        }


        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
                Log.i("DbHelper", "Upgrading database from version " + oldVersion + " to " + newVersion);
                db.execSQL(DROP_CLASS_TABLE);
                db.execSQL(DROP_STUDENT_TABLE);
                db.execSQL(DROP_STATUS_TABLE);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        long addClass(String sectionName, String gradeLevel) {
            SQLiteDatabase database = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(SECTION_NAME_KEY, sectionName);
            values.put(GRADE_LEVEL_KEY, gradeLevel);

            return database.insert(CLASS_TABLE_NAME, null, values);
        }

        Cursor getClassTable() {
            SQLiteDatabase database = this.getReadableDatabase();

            return database.rawQuery(SELECT_CLASS_TABLE, null);
        }

        int deleteClass(long cid) {
            SQLiteDatabase database = this.getReadableDatabase();
            return database.delete(CLASS_TABLE_NAME, C_ID + "=?", new String[]{String.valueOf(cid)});
        }

        long updateClass(long Cid, String sectionName, String gradeLevel) {
            SQLiteDatabase database = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(SECTION_NAME_KEY, sectionName);
            values.put(GRADE_LEVEL_KEY, gradeLevel);

            return database.update(CLASS_TABLE_NAME, values, C_ID + "=?", new String[]{String.valueOf(Cid)});
        }

        long addStudent(long Cid, int roll, String name) {
            SQLiteDatabase database = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(C_ID, Cid);
            values.put(STUDENT_ROLL_KEY, roll);
            values.put(STUDENT_NAME_KEY, name);
            return database.insert(STUDENT_TABLE_NAME, null, values);
        }

        Cursor getStudentTable(long Cid) {
            SQLiteDatabase database = this.getReadableDatabase();
            return database.query(STUDENT_TABLE_NAME, null, C_ID + "=?", new String[]{String.valueOf(Cid)}, null, null, STUDENT_ROLL_KEY);
        }

        int deleteStudent(long sid) {
            SQLiteDatabase database = this.getReadableDatabase();
            return database.delete(CLASS_TABLE_NAME, C_ID + "=?", new String[]{String.valueOf(sid)});
        }

        long updateStudent(long sid, String name) {
            SQLiteDatabase database = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(STUDENT_NAME_KEY, name);
            return database.update(STUDENT_TABLE_NAME, values, S_ID + "=?", new String[]{String.valueOf(sid)});
        }

        long addStatus(long sid, long cid, String date, String status, String studentName) {
            SQLiteDatabase database = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(S_ID, sid);
            values.put(C_ID, cid);
            values.put(DATE_KEY, date);
            values.put(STATUS_KEY, status);
            values.put(STUDENT_NAME_KEY, studentName);
            // Inside addStatus method
            Log.d("DbHelper", "Adding status: " + sid + ", " + cid + ", " + date + ", " + status + ", " + studentName);

            // Use replace method to handle insert or update
            return database.replace(STATUS_TABLE_NAME, null, values);
        }


        long updateStatus(long sid,long cid, String date, String status, String studentName) {
            SQLiteDatabase database = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(STATUS_KEY, status);
            values.put(C_ID, cid);
            values.put(STUDENT_NAME_KEY, studentName);
            String whereClause = DATE_KEY + "='" + date + "' AND " + S_ID + "=" + sid;
            // Inside updateStatus method
            Log.d("DbHelper", "Updating status: " + sid + ", " + date + ", " + status);
            return database.update(STATUS_TABLE_NAME, values, whereClause, null);
        }

        @SuppressLint("Range")
        String getStatus(long sid, String date) {
            String status = null;
            SQLiteDatabase database = this.getReadableDatabase();
            String whereClause = S_ID + " = " + sid + " AND " + DATE_KEY + " = '" + date + "'";
            Log.d("DbHelper", "Query: " + SELECT_STATUS_TABLE + " WHERE " + S_ID + "=" + sid + " AND " + DATE_KEY + "='" + date + "'");

            Cursor cursor = database.query(STATUS_TABLE_NAME, null, whereClause, null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst())
                    try {
                            int statusColumnIndex = cursor.getColumnIndex(STATUS_KEY);

                            if (statusColumnIndex != -1) {
                                status = cursor.getString(cursor.getColumnIndex(STATUS_KEY));
                                Log.d("DbHelper", "Status retrieved successfully: " + status);
                            }
                    } catch (Exception e) {
                        Log.e("DbHelper", "Error retrieving status from database", e);
                    }
                else {
                    Log.d("DbHelper", "No matching records found");
                }
            } else {
                Log.d("DbHelper", "cursor is null");
            }
            cursor.close();
            return status;
        }

        Cursor getDistinctMonths(long cid) {
            SQLiteDatabase database = this.getReadableDatabase();
            return database.query(STATUS_TABLE_NAME, new String[]{DATE_KEY}, C_ID + "=" + cid, null, "substr(" + DATE_KEY + ",4,7)", null, null);//01.04.2020
        }
    }
