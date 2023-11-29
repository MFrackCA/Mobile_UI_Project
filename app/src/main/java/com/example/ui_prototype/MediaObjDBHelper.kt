import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.ui_prototype.MediaObj


class MediaObjDBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "mediaobj.db"

        // Define table and column names
        private const val TABLE_MEDIA_OBJ = "media_objects"
        private const val COLUMN_ID = "_id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_PROFILE_IMAGE_RES_ID = "profile_image_res_id"
        private const val COLUMN_DESCRIPTION = "description"
        private const val COLUMN_MEDIA_URI = "media_uri"
        private const val COLUMN_MEDIA_TYPE = "media_type"
        private const val COLUMN_LONGITUDE = "longitude"
        private const val COLUMN_LATITUDE = "latitude"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_LOCATION_NAME = "location_name"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableSQL = """
            CREATE TABLE $TABLE_MEDIA_OBJ (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT,
                $COLUMN_PROFILE_IMAGE_RES_ID INTEGER,
                $COLUMN_DESCRIPTION TEXT,
                $COLUMN_MEDIA_URI TEXT,
                $COLUMN_MEDIA_TYPE TEXT,
                $COLUMN_LONGITUDE REAL,
                $COLUMN_LATITUDE REAL,
                $COLUMN_USERNAME TEXT,
                $COLUMN_LOCATION_NAME TEXT
            )
        """.trimIndent()

        db.execSQL(createTableSQL)


    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Handle database upgrades if needed
    }

    // Add a method to insert a MediaObj into the database
    fun insertMediaObj(mediaObj: MediaObj) {
        val values = ContentValues()
        values.put(COLUMN_TITLE, mediaObj.title)
        values.put(COLUMN_PROFILE_IMAGE_RES_ID, mediaObj.profileImageResId)
        values.put(COLUMN_DESCRIPTION, mediaObj.description)
        values.put(COLUMN_MEDIA_URI, mediaObj.mediaUri)
        values.put(COLUMN_MEDIA_TYPE, mediaObj.mediaType)
        values.put(COLUMN_LONGITUDE, mediaObj.long)
        values.put(COLUMN_LATITUDE, mediaObj.latitude)
        values.put(COLUMN_USERNAME, mediaObj.username)
        values.put(COLUMN_LOCATION_NAME, mediaObj.locationName)

        val db = this.writableDatabase
        db.insert(TABLE_MEDIA_OBJ, null, values)
        db.close()
    }


    fun getAllLocations(): List<MediaObj>? {
        val mediaObjList: MutableList<MediaObj> = ArrayList<MediaObj>()
        val db = this.readableDatabase
        val selectQuery = "SELECT  * FROM $TABLE_MEDIA_OBJ"
        val cursor: Cursor = db.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            do {
                val media = MediaObj(
                    title = cursor.getString(1),
                    profileImageResId = cursor.getInt(2),
                    description = cursor.getString(3),
                    mediaUri = cursor.getString(4),
                    mediaType = cursor.getString(5),
                    long = cursor.getDouble(6),
                    latitude = cursor.getDouble(7),
                    username = cursor.getString(8),
                    locationName = cursor.getString(9)
                )
                mediaObjList.add(media)

            } while (cursor.moveToNext())
        }
        cursor.close()
        return mediaObjList
    }

}
