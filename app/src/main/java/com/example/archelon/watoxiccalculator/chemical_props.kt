package com.example.archelon.watoxiccalculator

import android.app.AlertDialog
import android.app.Application
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.provider.BaseColumns
import android.support.v4.app.DialogFragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.EditText
import java.lang.ref.WeakReference


fun determine_rank(lc50_value: Float?, ld50_value: Float?, dermal_value: Float?, inhalation_value: Float?): Char{
    var final_rank = 'N'

    Log.d("Ld50: ", ld50_value.toString())

    when (lc50_value) {
        null -> { }
        in 10.0..100.0 -> final_rank = compare_rank('D', final_rank)
        in 1.0..9.999 -> final_rank = compare_rank('C', final_rank)
        in 0.1..0.999 -> final_rank = compare_rank('B', final_rank)
        in 0.01..0.099 -> final_rank = compare_rank('A', final_rank)
        in 0.0000001..0.009 -> final_rank = compare_rank('X', final_rank)
    }

    when (ld50_value) {
        null -> { }
        in 500.0..5000.0 -> final_rank = compare_rank('D', final_rank)
        in 50.0..499.999 -> final_rank = compare_rank('C', final_rank)
        in 5.0..49.999 -> final_rank = compare_rank('B', final_rank)
        in 0.5..4.999 -> final_rank = compare_rank('A', final_rank)
        in 0.0000001..0.499 -> final_rank = compare_rank('X', final_rank)
    }

    when (dermal_value){
        null -> { }
        in 2000.0..20000.0 -> final_rank = compare_rank('D', final_rank)
        in 200.0..1999.999 -> final_rank = compare_rank('C', final_rank)
        in 20.0..199.999 -> final_rank = compare_rank('B', final_rank)
        in 2.0..19.999 -> final_rank = compare_rank('A', final_rank)
        in 0.0000001..1.999 -> final_rank = compare_rank('X', final_rank)
    }

    when (inhalation_value) {
        null -> { }
        in 20.0..200.0 -> final_rank = compare_rank('D', final_rank)
        in 2.0..19.999 -> final_rank = compare_rank('C', final_rank)
        in 0.2..1.999 -> final_rank = compare_rank('B', final_rank)
        in 0.02..0.199 -> final_rank = compare_rank('A', final_rank)
        in 0.0000001..0.019 -> final_rank = compare_rank('X', final_rank)
    }

    return final_rank
}

fun compare_rank(table_rank: Char, current_rank: Char): Char{
    /*Log.d("Table Rank: ", table_rank.toString())
    Log.d("Current Rank: ", current_rank.toString())*/
    if((current_rank > table_rank && current_rank != 'X') || table_rank == 'X'){ return table_rank}
    else{return current_rank}
}

class chemical(var name: String?, var lc50: Float?, var ld50: Float?, var dermal: Float?, var inhalation: Float?, var custom: Boolean = false): Parcelable{

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readValue(Float::class.java.classLoader) as? Float,
        parcel.readValue(Float::class.java.classLoader) as? Float,
        parcel.readValue(Float::class.java.classLoader) as? Float,
        parcel.readValue(Float::class.java.classLoader) as? Float
    ) {
    }

    fun rank(): Char{
        return determine_rank(lc50, ld50, dermal, inhalation)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeValue(lc50)
        parcel.writeValue(ld50)
        parcel.writeValue(dermal)
        parcel.writeValue(inhalation)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<chemical> {
        override fun createFromParcel(parcel: Parcel): chemical {
            return chemical(parcel)
        }

        override fun newArray(size: Int): Array<chemical?> {
            return arrayOfNulls(size)
        }
    }
}

class waste(var stream_name: String?, var chems: ArrayList<chemical> = ArrayList()){


}

class ranking(){
    var sumation: MutableMap<Char,Float> = HashMap<Char,Float>()
    var currentRank: Char = 'N'

    init{
        sumation.put('X', 0.toFloat())
        sumation.put('A', 0.toFloat())
        sumation.put('B', 0.toFloat())
        sumation.put('C', 0.toFloat())
        sumation.put('D', 0.toFloat())
        sumation.put('N', 0.toFloat())
    }

    fun addToSum(concentration: String, baseRank: String){
        if(concentration.trim().isNotEmpty()) {
            when (baseRank.trim().toCharArray()[0]) {
                null -> {
                }
                'X' -> sumation.put('X', (sumation.getValue('X') + concentration.trim().toFloat()))
                'A' -> sumation.put('A', (sumation.getValue('A') + concentration.trim().toFloat()))
                'B' -> sumation.put('B', (sumation.getValue('B') + concentration.trim().toFloat()))
                'C' -> sumation.put('C', (sumation.getValue('C') + concentration.trim().toFloat()))
                'D' -> sumation.put('D', (sumation.getValue('D') + concentration.trim().toFloat()))
                'N' -> sumation.put('N', (sumation.getValue('N') + concentration.trim().toFloat()))
                ' ' -> {
                }
            }
        }
    }

    fun returnDesignation(): String{
        var sumTotal:Float = 0.toFloat()

        sumTotal += sumation.getValue('X').toFloat()
        sumTotal += (sumation.getValue('A').toFloat()/10)
        sumTotal += (sumation.getValue('B').toFloat()/100)
        sumTotal += (sumation.getValue('C').toFloat()/1000)
        sumTotal += (sumation.getValue('D').toFloat()/10000)

        Log.d("sumTotal", sumTotal.toString())

        if(sumTotal > 1.toFloat()){
            return "WT01"
        }else if(sumTotal > 0.001){
            return "WT02"
        }
        return "N/A"
    }

    fun replaceConcentration(oldConcentration: String, newConcentration: String, Rank:String): String{
        var tempConc = oldConcentration.trim()
        if(tempConc.isEmpty()){
            tempConc = "0"
        }
        Log.d("oldConcentration", tempConc)
        Log.d("newConcentration", newConcentration.trim())
        Log.d("Rank", Rank.trim().toCharArray()[0].toString())
        if(newConcentration.trim().isNotEmpty()) {
            sumation.put(
                Rank.trim().toCharArray()[0],
                (sumation.getValue(Rank.trim().toCharArray()[0]) - tempConc.toFloat())
            )
            sumation.put(
                Rank.trim().toCharArray()[0],
                (sumation.getValue(Rank.trim().toCharArray()[0]) + newConcentration.trim().toFloat())
            )
        }
        return returnDesignation()
    }


}

class chemicalHolder: Application(){
    var ChemicalList: HashMap<String, WeakReference<chemical>> = HashMap()

    init{
        preload()
    }

    fun save(name: String, incoming_chem: chemical){
        this.ChemicalList.put(name, WeakReference<chemical>(incoming_chem))
    }

    fun get(name: String): chemical?{
        return this.ChemicalList.get(name)?.get()
    }

    fun preload(){
        this.save("Chem1", chemical("Chem1", 15.toFloat(), null, null, 15.5.toFloat()))
        this.save("Chem2", chemical("Chem2", 2.0.toFloat(), null, null, 100.0.toFloat()))
        this.save("Chem3", chemical("Chem3", null, 25.toFloat(), null, 500.0.toFloat()))
        this.save("Chem4", chemical("Chem4", null, null, 125.89.toFloat(), null))
    }

    fun update(target: String?, update_lc50: Float?, update_ld50: Float?, update_dermal: Float?, update_inhalation: Float?){
        if(update_lc50 != null){ChemicalList[target]?.get()?.lc50 = update_lc50}
        if(update_ld50 != null){ChemicalList[target]?.get()?.ld50 = update_ld50}
        if(update_dermal != null){ChemicalList[target]?.get()?.dermal = update_dermal}
        if(update_inhalation != null){ChemicalList[target]?.get()?.inhalation = update_inhalation}
    }

    fun generateChemList(): MutableList<chemical>{
        val output: MutableList<chemical> = ArrayList()
        output.add(chemical("Add A Chemical", null, null, null, null))
        for ((key,value) in ChemicalList){
            val temp = value.get()
            if(temp != null) {
                output.add(temp)
            }
        }
        return output
    }

}

object FeedReaderContract {
    // Table contents are grouped together in an anonymous object.
    object FeedEntry : BaseColumns {
        const val TABLE_NAME = "Chemical_List"
        const val COLUMN_NAME_TITLE = "Name"
        const val COLUMN_LC50_TITLE = "Lc50"
        const val COLUMN_LD50_TITLE = "Ld50"
        const val COLUMN_DERMAL_TITLE = "Dermal"
        const val COLUMN_INHAL_TITLE = "Inhalation"
        const val COLUMN_CHEM_RANK = "Rank"
    }

    object WasteEntry: BaseColumns{
        const val WASTE_TABLE = "Waste_List"
        const val COLUMN_WASTE_TITLE = "Waste"
        const val COLUMN_CHEM_ID = "Id"
        const val COLUMN_WASTE_RANK = "Rank"
        const val COLUMN_CHEM_CONCETRATION = "Concentration"
    }
}

private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${FeedReaderContract.FeedEntry.TABLE_NAME}"

private const val SQL_CREATE_ENTRIES =
    "CREATE TABLE ${FeedReaderContract.FeedEntry.TABLE_NAME} (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY," +
            "${FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE} TEXT," +
            "${FeedReaderContract.FeedEntry.COLUMN_LC50_TITLE} TEXT," +
            "${FeedReaderContract.FeedEntry.COLUMN_LD50_TITLE} TEXT," +
            "${FeedReaderContract.FeedEntry.COLUMN_DERMAL_TITLE} TEXT," +
            "${FeedReaderContract.FeedEntry.COLUMN_INHAL_TITLE} TEXT," +
            "${FeedReaderContract.FeedEntry.COLUMN_CHEM_RANK} TEXT)"

private const val SQL_DELETE_WASTE = "DROP TABLE IF EXISTS ${FeedReaderContract.WasteEntry.WASTE_TABLE}"

private const val SQL_CREATE_WASTE =
    "CREATE TABLE ${FeedReaderContract.WasteEntry.WASTE_TABLE} (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY," +
            "${FeedReaderContract.WasteEntry.COLUMN_WASTE_TITLE} TEXT," +
            "${FeedReaderContract.WasteEntry.COLUMN_CHEM_ID} TEXT," +
            "${FeedReaderContract.WasteEntry.COLUMN_WASTE_RANK} TEXT," +
            "${FeedReaderContract.WasteEntry.COLUMN_CHEM_CONCETRATION} TEXT)"

class FeedReaderDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
        db.execSQL(SQL_CREATE_WASTE)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES)
        db.execSQL(SQL_DELETE_WASTE)
        onCreate(db)
    }
    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    fun purge(){
        val db = this.writableDatabase
        db.execSQL(SQL_DELETE_ENTRIES)
        db.close()
        //db.execSQL(SQL_DELETE_WASTE)
    }

    fun purgeWastes(){
        val db = this.writableDatabase
        db.execSQL(SQL_DELETE_WASTE)
        db.close()
    }

    fun generate(){
        val db = this.writableDatabase
        db.execSQL(SQL_CREATE_ENTRIES)
        db.close()
        //db.execSQL(SQL_CREATE_WASTE)
    }

    fun generateWastes(){
        val db = this.writableDatabase
        db.execSQL(SQL_CREATE_WASTE)
        db.close()
    }

    fun updateChemicalEntry(incoming_chem: chemical, oldName: String?): Boolean{

       if(oldName in retrieveChemicalNames()) {
           val db = this.writableDatabase
           val values = ContentValues().apply {
               put(FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE, incoming_chem.name)
               put(FeedReaderContract.FeedEntry.COLUMN_LC50_TITLE, incoming_chem.lc50.toString())
               put(FeedReaderContract.FeedEntry.COLUMN_LD50_TITLE, incoming_chem.ld50.toString())
               put(FeedReaderContract.FeedEntry.COLUMN_DERMAL_TITLE, incoming_chem.dermal.toString())
               put(FeedReaderContract.FeedEntry.COLUMN_INHAL_TITLE, incoming_chem.inhalation.toString())
           }
           val selection = "${FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE} LIKE ?"
           val selectionArg = arrayOf(oldName)
           db.update(FeedReaderContract.FeedEntry.TABLE_NAME, values, selection, selectionArg)
           db.close()
           return true
       }
        return false

    }

    companion object {
        // If you change the database schema, you must increment the database version.
        const val DATABASE_VERSION = 3
        const val DATABASE_NAME = "FeedReader.db"
    }

    fun preload_database(){
        val db = this.writableDatabase

        val tempChems: ArrayList<chemical> = ArrayList<chemical>()
        tempChems.addAll(arrayOf(
            chemical("Acetone", 5540.toFloat(), 5800.toFloat(), 7462.toFloat(), 50100.toFloat()),
            chemical("Arsenic(III) Oxide", 0.toFloat(), 14.6.toFloat(), 0.toFloat(), 0.toFloat()),
            chemical("Botulinum Toxin A .1%", 0.toFloat(), 0.1.toFloat(), 0.toFloat(), 0.toFloat()),
            chemical("Capsaicin", 0.toFloat(), 148.1.toFloat(), 512.toFloat(), 0.toFloat()),
            chemical("Glacial Acetic Acid 99%", 1000.toFloat(), 3310.toFloat(), 1112.toFloat(), 11.4.toFloat()),
            chemical("Sodium Cyanide", 0.04.toFloat(), 4.7.toFloat(), 10.4.toFloat(), 0.toFloat()),
            chemical("Water", 0.toFloat(), 0.toFloat(), 0.toFloat(), 0.toFloat())

        ))

        for(thisChem in tempChems){
            val values = ContentValues().apply {
                put(FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE, thisChem.name)
                put(FeedReaderContract.FeedEntry.COLUMN_LC50_TITLE, thisChem.lc50.toString())
                put(FeedReaderContract.FeedEntry.COLUMN_LD50_TITLE, thisChem.ld50.toString())
                put(FeedReaderContract.FeedEntry.COLUMN_DERMAL_TITLE, thisChem.dermal.toString())
                put(FeedReaderContract.FeedEntry.COLUMN_INHAL_TITLE, thisChem.inhalation.toString())
                put(FeedReaderContract.FeedEntry.COLUMN_CHEM_RANK, thisChem.rank().toString())
            }
            Log.d("Name: ", thisChem.name)
            Log.d("Lc50: ", thisChem.lc50.toString())
            Log.d("Ld50: ", thisChem.ld50.toString())
            Log.d("Dermal: ", thisChem.dermal.toString())
            Log.d("Inhal: ", thisChem.inhalation.toString())
            Log.d("Rank: ", thisChem.rank().toString())
            // Insert the new row, returning the primary key value of the new row
            db?.insert(FeedReaderContract.FeedEntry.TABLE_NAME, null, values)
        }

        db.close()
    }

    fun addToChemDB(chemToAdd: chemical){
        val db = this.writableDatabase

        val values = ContentValues().apply {
            put(FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE, chemToAdd.name)
            put(FeedReaderContract.FeedEntry.COLUMN_LC50_TITLE, chemToAdd.lc50.toString())
            put(FeedReaderContract.FeedEntry.COLUMN_LD50_TITLE, chemToAdd.ld50.toString())
            put(FeedReaderContract.FeedEntry.COLUMN_DERMAL_TITLE, chemToAdd.dermal.toString())
            put(FeedReaderContract.FeedEntry.COLUMN_INHAL_TITLE, chemToAdd.inhalation.toString())
            put(FeedReaderContract.FeedEntry.COLUMN_CHEM_RANK, chemToAdd.rank().toString())
        }
        // Insert the new row, returning the primary key value of the new row
        db?.insert(FeedReaderContract.FeedEntry.TABLE_NAME, null, values)

    }

    fun retrieve_table(): MutableList<chemical>{
        val db = this.readableDatabase
        val output: MutableList<chemical> = ArrayList<chemical>()

        val cursor = db.query(
            FeedReaderContract.FeedEntry.TABLE_NAME,   // The table to query
            null,             // The array of columns to return (pass null to get all)
            null,              // The columns for the WHERE clause
            null,          // The values for the WHERE clause
            null,                   // don't group the rows
            null,                   // don't filter by row groups
            null               // The sort order
        )

        with(cursor) {
            while (moveToNext()) {
                val itemId = getLong(getColumnIndexOrThrow(BaseColumns._ID))
                val iName = getString(getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE))
                val iLc50 = getString(getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_LC50_TITLE))
                val iLd50 = getString(getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_LD50_TITLE))
                val iDermal = getString(getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_DERMAL_TITLE))
                val iInhal = getString(getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_INHAL_TITLE))

                //val toast = Toast.makeText(applicationContext, itemId, Toast.LENGTH_LONG)
                //toast.show()
                output.add(chemical(iName,iLc50.toFloat(),iLd50.toFloat(),iDermal.toFloat(),iInhal.toFloat()))

                android.util.Log.d("itemId", itemId.toString())
                android.util.Log.d("Name", iName)
                android.util.Log.d("LC50", iLc50)
                android.util.Log.d("LD50", iLd50)
                android.util.Log.d("Dermal", iDermal)
                android.util.Log.d("Inhal", iInhal)
            }
        }
        db.close()
        return output
    }

    fun retrieveCorrectedTable(wasteName: String): MutableList<chemical>{
        val tempMap = retrieveConstituents(wasteName).values
        val db = this.readableDatabase
        val output: MutableList<chemical> = ArrayList<chemical>()

        val cursor = db.query(
            FeedReaderContract.FeedEntry.TABLE_NAME,   // The table to query
            null,             // The array of columns to return (pass null to get all)
            null,              // The columns for the WHERE clause
            null,          // The values for the WHERE clause
            null,                   // don't group the rows
            null,                   // don't filter by row groups
            null               // The sort order
        )

        with(cursor) {
            while (moveToNext()) {
                val itemId = getLong(getColumnIndexOrThrow(BaseColumns._ID))
                val iName = getString(getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE))
                val iLc50 = getString(getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_LC50_TITLE))
                val iLd50 = getString(getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_LD50_TITLE))
                val iDermal = getString(getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_DERMAL_TITLE))
                val iInhal = getString(getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_INHAL_TITLE))

                //val toast = Toast.makeText(applicationContext, itemId, Toast.LENGTH_LONG)
                //toast.show()
                if(!(iName in tempMap)) {
                    output.add(chemical(iName, iLc50.toFloat(), iLd50.toFloat(), iDermal.toFloat(), iInhal.toFloat()))

                    android.util.Log.d("itemId", itemId.toString())
                    android.util.Log.d("Name", iName)
                    android.util.Log.d("LC50", iLc50)
                    android.util.Log.d("LD50", iLd50)
                    android.util.Log.d("Dermal", iDermal)
                    android.util.Log.d("Inhal", iInhal)
                }
            }
        }
        db.close()
        return output
    }

    fun deleteConstituent(wasteStreamName: String, constituentName: String){
        val chemId: String = retrieveChemId(constituentName)
        val selection = "${FeedReaderContract.WasteEntry.COLUMN_WASTE_TITLE} = ? AND ${FeedReaderContract.WasteEntry.COLUMN_CHEM_ID} = ?"
        val selectionArgs = arrayOf(wasteStreamName,chemId)
        val db = this.writableDatabase

        db.delete(FeedReaderContract.WasteEntry.WASTE_TABLE, selection,selectionArgs)
        close()
    }



    fun retrieveChemicalNames(): MutableList<String>{
        val db = this.readableDatabase
        val output: MutableList<String> = ArrayList<String>()

        val cursor = db.query(
            FeedReaderContract.FeedEntry.TABLE_NAME,   // The table to query
            null,             // The array of columns to return (pass null to get all)
            null,              // The columns for the WHERE clause
            null,          // The values for the WHERE clause
            null,                   // don't group the rows
            null,                   // don't filter by row groups
            null               // The sort order
        )

        with(cursor) {
            while (moveToNext()) {
                val itemId = getLong(getColumnIndexOrThrow(BaseColumns._ID))
                val iName = getString(getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE))

                output.add(iName)

                android.util.Log.d("itemId", itemId.toString())
                android.util.Log.d("Name", iName)
            }
        }
        db.close()
        return output
    }

    fun retrieveWastes(): MutableList<String>{
        val db = this.readableDatabase
        val output: MutableList<String> = ArrayList<String>()

        val cursor = db.query(
            FeedReaderContract.WasteEntry.WASTE_TABLE,   // The table to query
            null,             // The array of columns to return (pass null to get all)
            null,              // The columns for the WHERE clause
            null,          // The values for the WHERE clause
            null,                   // don't group the rows
            null,                   // don't filter by row groups
            null               // The sort order
        )

        with(cursor) {
            while (moveToNext()) {
                val itemId = getLong(getColumnIndexOrThrow(BaseColumns._ID))
                val iName = getString(getColumnIndexOrThrow(FeedReaderContract.WasteEntry.COLUMN_WASTE_TITLE))

                if(!(iName in output)){
                    output.add(iName)

                    android.util.Log.d("itemId", itemId.toString())
                    android.util.Log.d("Name", iName)
                }
            }
        }
        db.close()
        return output
    }

    fun retrieveConstituents(wasteStreamName: String): MutableMap<String, String>{
        //val temp: MutableMap<String, String> = HashMap<String, String>()
        val db = this.readableDatabase
        val output: MutableMap<String, String> = HashMap<String, String>()

        val selection = "${FeedReaderContract.WasteEntry.COLUMN_WASTE_TITLE} = ?"
        val selectionArgs = arrayOf(wasteStreamName)

        val cursor = db.query(
            FeedReaderContract.WasteEntry.WASTE_TABLE,   // The table to query
            null,             // The array of columns to return (pass null to get all)
            selection,              // The columns for the WHERE clause
            selectionArgs,          // The values for the WHERE clause
            null,                   // don't group the rows
            null,                   // don't filter by row groups
            null               // The sort order
        )

        with(cursor) {
            while (moveToNext()) {
                val wasteTableId = getLong(getColumnIndexOrThrow(BaseColumns._ID))
                val chemId = getString(getColumnIndexOrThrow(FeedReaderContract.WasteEntry.COLUMN_CHEM_ID))

                //chemIds.add(chemId)
                android.util.Log.d("Waste Table Id", wasteTableId.toString())
                android.util.Log.d("Chem Id", chemId)
                output.put(wasteTableId.toString(),retrieveChemName(chemId, db))
            }
        }

        /*for(id in chemIds){
            output.add(retrieveChemName(id, db))
        }*/


        db.close()
        return output
    }

    fun retrieveChemRank(chemName: String): String{
        val db = this.readableDatabase
        var output: String = String()

        val projection = arrayOf(FeedReaderContract.FeedEntry.COLUMN_CHEM_RANK)
        val selection = "${FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE} = ?"
        val selectionArgs = arrayOf(chemName)

        val cursor = db.query(
            FeedReaderContract.FeedEntry.TABLE_NAME,   // The table to query
            projection,             // The array of columns to return (pass null to get all)
            selection,              // The columns for the WHERE clause
            selectionArgs,          // The values for the WHERE clause
            null,                   // don't group the rows
            null,                   // don't filter by row groups
            null               // The sort order
        )

        with(cursor) {
            while (moveToNext()) {
                val chemRank = getString(getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_CHEM_RANK))
                //chemIds.add(chemId)
                android.util.Log.d("Rank", chemRank)
                output = chemRank
            }
        }

        /*for(id in chemIds){
            output.add(retrieveChemName(id, db))
        }*/


        db.close()
        return output
    }

    fun retrieveChemName(chemId: String, db: SQLiteDatabase): String{
        val projection = arrayOf(FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE)
        val selection = "${BaseColumns._ID} = ?"
        //val selectionArgs2 = chemIds.toTypedArray()
        val selectionArgs = arrayOf(chemId)
        var output: String = String()

        val cursor = db.query(
            FeedReaderContract.FeedEntry.TABLE_NAME,   // The table to query
            projection,             // The array of columns to return (pass null to get all)
            selection,              // The columns for the WHERE clause
            selectionArgs,          // The values for the WHERE clause
            null,                   // don't group the rows
            null,                   // don't filter by row groups
            null               // The sort order
        )

        with(cursor) {
            while (moveToNext()) {
                //val itemId = getLong(getColumnIndexOrThrow(BaseColumns._ID))
                output = getString(getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE))

                //android.util.Log.d("itemId", itemId.toString())
                android.util.Log.d("Column Name", output)

            }
        }

        return output
    }

    fun retrieveChemId(chemName: String): String{
        val db = this.readableDatabase
        var output: String = ""

        val selection = "${FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE} = ?"
        val selectionArgs = arrayOf(chemName)

        val cursor = db.query(
            FeedReaderContract.FeedEntry.TABLE_NAME,   // The table to query
            null,             // The array of columns to return (pass null to get all)
            selection,              // The columns for the WHERE clause
            selectionArgs,          // The values for the WHERE clause
            null,                   // don't group the rows
            null,                   // don't filter by row groups
            null               // The sort order
        )

        with(cursor) {
            while (moveToNext()) {
                val itemId = getLong(getColumnIndexOrThrow(BaseColumns._ID))
                val iName = getString(getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE))

                output = itemId.toString()

                android.util.Log.d("itemId", itemId.toString())
                android.util.Log.d("Name", iName)
            }
        }

        db.close()
        return output
    }

    fun retrieveConc(wasteStreamName: String, chemName: String): String{

        var output: String = " "

        val chemId = retrieveChemId(chemName)

        val selection = "${FeedReaderContract.WasteEntry.COLUMN_WASTE_TITLE} = ? AND ${FeedReaderContract.WasteEntry.COLUMN_CHEM_ID} = ?"
        val selectionArgs = arrayOf(wasteStreamName, chemId)
        val db = this.readableDatabase
        val cursor = db.query(
            FeedReaderContract.WasteEntry.WASTE_TABLE,   // The table to query
            null,             // The array of columns to return (pass null to get all)
            selection,              // The columns for the WHERE clause
            selectionArgs,          // The values for the WHERE clause
            null,                   // don't group the rows
            null,                   // don't filter by row groups
            null               // The sort order
        )

        with(cursor) {
            while (moveToNext()) {
                val wasteTableId = getLong(getColumnIndexOrThrow(BaseColumns._ID))
                val chemConc = getString(getColumnIndexOrThrow(FeedReaderContract.WasteEntry.COLUMN_CHEM_CONCETRATION))

                //chemIds.add(chemId)
                // android.util.Log.d(chemName + " Conc", chemConc)
                if(chemConc != null){output = chemConc}



            }
        }
        return output
    }

    fun putChemWithWaste(wasteStreamName: String, chemName: String){


        android.util.Log.d("Attemping put", wasteStreamName + " " + chemName)

        val values = ContentValues().apply {
            put(FeedReaderContract.WasteEntry.COLUMN_WASTE_TITLE, wasteStreamName)
            put(FeedReaderContract.WasteEntry.COLUMN_CHEM_ID, retrieveChemId(chemName))
        }
        // Insert the new row, returning the primary key value of the new row
        val db = this.writableDatabase
        db.insert(FeedReaderContract.WasteEntry.WASTE_TABLE, null, values)
        db.close()
    }

    fun updateWasteListChemId(wasteStreamName: String, targetId: String, newChemName: String){

        val newChemId = retrieveChemId(newChemName)
        val values = ContentValues().apply {
            put(FeedReaderContract.WasteEntry.COLUMN_CHEM_ID, newChemId)
        }

        // Which row to update, based on the ID
        val selection = "${BaseColumns._ID} = ?"
        val selectionArgs = arrayOf(targetId)
        val db = this.writableDatabase
        val count = db.update(
            FeedReaderContract.WasteEntry.WASTE_TABLE,
            values,
            selection,
            selectionArgs)
    }

    fun updateWasteListName(oldName: String, newName: String){
        val values = ContentValues().apply {
            put(FeedReaderContract.WasteEntry.COLUMN_WASTE_TITLE, newName)
        }
        Log.d("Updating: " + oldName, " to " + newName)

        // Which row to update, based on the ID
        val selection = "${FeedReaderContract.WasteEntry.COLUMN_WASTE_TITLE} = ?"
        val selectionArgs = arrayOf(oldName)
        val db = this.writableDatabase
        val count = db.update(
            FeedReaderContract.WasteEntry.WASTE_TABLE,
            values,
            selection,
            selectionArgs)
        Log.d("Number of Items Updated", count.toString())
    }

    fun updateConcentration(wasteName: String, chemName: String, inputConcentration: String){
        val values = ContentValues().apply {
            put(FeedReaderContract.WasteEntry.COLUMN_CHEM_CONCETRATION, inputConcentration)
        }
        Log.d("Updating Conc: ", " to " + inputConcentration)

        val chemId = retrieveChemId(chemName)

        // Which row to update, based on the ID
        val selection = "${FeedReaderContract.WasteEntry.COLUMN_WASTE_TITLE} = ? AND ${FeedReaderContract.WasteEntry.COLUMN_CHEM_ID} = ?"
        val selectionArgs = arrayOf(wasteName,chemId)
        val db = this.writableDatabase
        val count = db.update(
            FeedReaderContract.WasteEntry.WASTE_TABLE,
            values,
            selection,
            selectionArgs)
        Log.d("Number of Items Updated", count.toString())

        db.close()
    }
}


class WasteNameDialogFragment : DialogFragment() {

    private var mContext : AppCompatActivity = AppCompatActivity()
    private var oldName: String = String()
    var newName: String = String()
    private var collapsable: Boolean = true

    fun setContext(temp: AppCompatActivity, oldName: String, collapsable: Boolean = true){
        mContext = temp
        this.oldName = oldName
        this.newName = oldName
        this.collapsable = collapsable
    }

    fun saveName(oldName: String, newName: String){
        val dbCooper = FeedReaderDbHelper(mContext)
        dbCooper.updateWasteListName(oldName,newName)
    }

    fun resolve(){
        if(collapsable) {
            val back_intent = Intent()
            mContext.setResult(AppCompatActivity.RESULT_OK, back_intent)
            mContext.finish()
        }else{

        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater;

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(inflater.inflate(R.layout.wastestream_name_layout, null))
                // Add action buttons
                .setPositiveButton("Save",
                    DialogInterface.OnClickListener { dialog, id ->
                        val dialogObj = Dialog::class.java.cast(dialog)
                        val edit: EditText = dialogObj.findViewById<EditText>(R.id.wasteName)
                        newName = edit.text.toString()
                        this.saveName(oldName, newName)
                        this.resolve()
                        getDialog().cancel()
                    })
                .setNegativeButton("Cancel",
                    DialogInterface.OnClickListener { dialog, id ->
                        this.resolve()
                        getDialog().cancel()
                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

}

class StreamNameDialogFragment : DialogFragment() {

    private var mContext : WasteStreams = WasteStreams()
    private var oldName: String = String()
    var newName: String = String()

    fun setContext(temp: WasteStreams, oldName: String){
        mContext = temp
        this.oldName = oldName
        this.newName = oldName
    }

    private fun saveName(){
        val dbCooper = FeedReaderDbHelper(mContext)
        if(oldName.equals("Add A Waste Stream")){
            val intent = Intent(mContext, constituent_list::class.java).apply {
                putExtra("wasteName",newName)
            }
            mContext.startActivityForResult(intent,0)
        }else{
            dbCooper.updateWasteListName(oldName,newName)
        }

    }

    fun resolve(){
        mContext.render_wastes()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater;

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(inflater.inflate(R.layout.wastestream_name_layout, null))
                // Add action buttons
                .setPositiveButton("Save",
                    DialogInterface.OnClickListener { dialog, id ->
                        val dialogObj = Dialog::class.java.cast(dialog)
                        val edit: EditText = dialogObj.findViewById<EditText>(R.id.wasteName)
                        newName = edit.text.toString()
                        this.saveName()
                        this.resolve()
                        getDialog().cancel()
                    })
                .setNegativeButton("Cancel",
                    DialogInterface.OnClickListener { dialog, id ->
                        this.resolve()
                        getDialog().cancel()
                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

}