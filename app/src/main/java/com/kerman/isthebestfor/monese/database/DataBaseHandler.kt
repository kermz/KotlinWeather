package com.kerman.isthebestfor.monese.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import com.kerman.isthebestfor.monese.models.Main
import com.kerman.isthebestfor.monese.models.Weather
import com.kerman.isthebestfor.monese.models.WeatherModel
import com.kerman.isthebestfor.monese.models.Wind
import java.io.ByteArrayOutputStream

val DATABASE_NAME ="kermanMadeThis.db"
val TABLE_NAME="Weather"
val COL_TEMPERATURE = "temperature"
val COL_WEATHERDESCRIPTION = "weatherDescription"
val COL_WEATHERICONNAME = "weatherIcon"
val COL_WINDSPEED = "windSpeed"
val COL_WINDDIRECTION = "windDirection"
val COL_DATE = "date"
val COL_ICON = "icon"
val COL_ID = "id"

class DataBaseHandler(var context: Context) : SQLiteOpenHelper(context, DATABASE_NAME,null,1){
    override fun onCreate(db: SQLiteDatabase?) {

        val createTable = "CREATE TABLE " + TABLE_NAME +" (" +
                COL_ID +" INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_WEATHERDESCRIPTION + " TEXT," +
                COL_TEMPERATURE +" DOUBLE," +
                COL_WEATHERICONNAME +" TEXT," +
                COL_WINDSPEED +" DOUBLE," +
                COL_WINDDIRECTION +" DOUBLE," +
                COL_DATE +" TEXT," +
                COL_ICON +" TEXT)"

        db?.execSQL(createTable)

    }

    override fun onUpgrade(db: SQLiteDatabase?,oldVersion: Int,newVersion: Int) {
        // Drop older table if existed
        db?.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        // Create tables again
        onCreate(db);
    }

    fun insertData(weather: WeatherModel){
        val db = this.writableDatabase
        var cv = ContentValues()
        cv.put(COL_WEATHERDESCRIPTION, weather.weather!!.get(0).description.toString())
        cv.put(COL_WEATHERICONNAME, weather.weather!!.get(0).icon.toString())
        cv.put(COL_WINDSPEED, weather.wind!!.speed!!.toDouble())
        cv.put(COL_WINDDIRECTION, weather.wind!!.deg!!.toDouble())
        //Convert from date to string for database storage
        val format = SimpleDateFormat("yyyy.MM.dd HH:mm:ss")
        val dateString = format.format(weather.date!!.time)
        cv.put(COL_DATE, dateString)
        var iconString = ""
        if(weather.iconBitmap != null)  {
            iconString = this!!.bitMapToString(weather.iconBitmap!!)!!

        }
        if(iconString == null) iconString = ""
        cv.put(COL_ICON, iconString)
        cv.put(COL_TEMPERATURE, weather.main!!.temp)
        var result = db.insert(TABLE_NAME,null,cv)
        if(result == -1.toLong())
            Toast.makeText(context,"Failed",Toast.LENGTH_SHORT).show()
        else
            Toast.makeText(context,"Success",Toast.LENGTH_SHORT).show()
    }

    fun readData() : MutableList<WeatherModel>{
        var list : MutableList<WeatherModel> = ArrayList()

        val db = this.readableDatabase
        val query = "Select * from " + TABLE_NAME
        val result = db.rawQuery(query,null)
        if(result.moveToFirst()){
            var weatherModel = WeatherModel()
            var weather = Weather()
            var wind = Wind()
            var main = Main()
            weather.description = result.getString(result.getColumnIndex(COL_WEATHERDESCRIPTION))
            weather.icon = result.getString(result.getColumnIndex(COL_WEATHERICONNAME))
            main.temp = result.getString(result.getColumnIndex(COL_TEMPERATURE)).toDouble()
            wind.deg = result.getString(result.getColumnIndex(COL_WINDDIRECTION)).toDouble()
            wind.speed = result.getString(result.getColumnIndex(COL_WINDSPEED)).toDouble()
            //Convert from string to date
            val format = SimpleDateFormat("yyyy.MM.dd HH:mm:ss")
            var dateString = result.getString(result.getColumnIndex(COL_DATE)).toString()
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

    fun deleteData(){
        val db = this.writableDatabase
        db.delete(TABLE_NAME,null,null)
        db.close()
    }

    fun bitMapToString(bitmap: Bitmap): String? {
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

    fun stringToBitMap(image: String): Bitmap? {
        try {
            val encodeByte = android.util.Base64.decode(image, android.util.Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
        } catch (e: Exception) {
            e.message
            return null
        }

    }

}