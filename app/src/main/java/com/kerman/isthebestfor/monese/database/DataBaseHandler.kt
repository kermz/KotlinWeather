package com.kerman.isthebestfor.monese.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import com.kerman.isthebestfor.monese.models.Main
import com.kerman.isthebestfor.monese.models.Weather
import com.kerman.isthebestfor.monese.models.WeatherModel
import com.kerman.isthebestfor.monese.models.Wind
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

const val DATABASE_NAME = "kermanMadeThis.db"
const val TABLE_NAME = "Weather"
const val COL_TEMPERATURE = "temperature"
const val COL_WEATHERDESCRIPTION = "weatherDescription"
const val COL_WEATHERICONNAME = "weatherIcon"
const val COL_WINDSPEED = "windSpeed"
const val COL_WINDDIRECTION = "windDirection"
const val COL_DATE = "date"
const val COL_ICON = "icon"
const val COL_ID = "id"

class DataBaseHandler(var context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {

        val createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_WEATHERDESCRIPTION + " TEXT," +
                COL_TEMPERATURE + " DOUBLE," +
                COL_WEATHERICONNAME + " TEXT," +
                COL_WINDSPEED + " DOUBLE," +
                COL_WINDDIRECTION + " DOUBLE," +
                COL_DATE + " TEXT," +
                COL_ICON + " TEXT)"

        db?.execSQL(createTable)

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Drop older table if existed
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        // Create tables again
        onCreate(db)
    }

    fun insertData(weather: WeatherModel) {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(COL_WEATHERDESCRIPTION, weather.weather.get(0).description)
        cv.put(COL_WEATHERICONNAME, weather.weather.get(0).icon)
        cv.put(COL_WINDSPEED, weather.wind.speed)
        cv.put(COL_WINDDIRECTION, weather.wind.deg)
        //Convert from date to string for database storage
        val format = SimpleDateFormat("yyyy.MM.dd HH:mm:ss")
        val dateString = format.format(weather.date.time)
        cv.put(COL_DATE, dateString)
        var iconString = ""
        iconString = this.bitMapToString(weather.iconBitmap)
        cv.put(COL_ICON, iconString)
        cv.put(COL_TEMPERATURE, weather.main.temp)
        val result = db.insert(TABLE_NAME, null, cv)
        if (result == (-1).toLong())
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
        else
            Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()
    }

    fun readData(): MutableList<WeatherModel> {
        val list: MutableList<WeatherModel> = ArrayList()

        val db = this.readableDatabase
        val query = "Select * from " + TABLE_NAME
        val result = db.rawQuery(query, null)
        if (result.moveToFirst()) {
            val weatherModel = WeatherModel()
            val weather = Weather()
            val wind = Wind()
            val main = Main()
            weather.description = result.getString(result.getColumnIndex(COL_WEATHERDESCRIPTION))
            weather.icon = result.getString(result.getColumnIndex(COL_WEATHERICONNAME))
            main.temp = result.getString(result.getColumnIndex(COL_TEMPERATURE)).toDouble()
            wind.deg = result.getString(result.getColumnIndex(COL_WINDDIRECTION)).toDouble()
            wind.speed = result.getString(result.getColumnIndex(COL_WINDSPEED)).toDouble()
            //Convert from string to date
            val format = SimpleDateFormat("yyyy.MM.dd HH:mm:ss")
            val dateString = result.getString(result.getColumnIndex(COL_DATE)).toString()
            val date = format.parse(dateString)
            weatherModel.date = date
            weatherModel.iconBitmap = stringToBitMap(result.getString(result.getColumnIndex(COL_ICON)))
            weatherModel.weather = arrayListOf(weather)
            weatherModel.wind = wind
            weatherModel.main = main
            list.add(weatherModel)
        }

        result.close()
        db.close()
        return list
    }

    fun deleteData() {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, null, null)
        db.close()
    }

    private fun bitMapToString(bitmap: Bitmap): String {
        try {
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
            val arr = baos.toByteArray()
            return android.util.Base64.encodeToString(arr, android.util.Base64.DEFAULT)
        } catch (e: Exception) {
            e.message
            return ""
        }
    }

    private fun stringToBitMap(image: String): Bitmap {
        try {
            val encodeByte = android.util.Base64.decode(image, android.util.Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
        } catch (e: Exception) {
            e.message
            val conf = Bitmap.Config.ARGB_8888
            return Bitmap.createBitmap(1, 1, conf)
        }

    }

}