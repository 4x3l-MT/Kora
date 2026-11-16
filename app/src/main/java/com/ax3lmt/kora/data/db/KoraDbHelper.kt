package com.ax3lmt.kora.data.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.ax3lmt.kora.model.DailyLog
import com.ax3lmt.kora.model.FlowLevel
import com.ax3lmt.kora.model.Symptom
import java.time.LocalDate

class KoraDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "kora_local_period.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_DAILY_LOGS = "daily_logs"
        private const val COLUMN_DATE = "date_str"
        private const val COLUMN_IS_PERIOD = "is_period_day"
        private const val COLUMN_FLOW = "flow_level"
        private const val COLUMN_SYMPTOMS = "symptoms"
        private const val COLUMN_PAIN = "pain_intensity"
        private const val COLUMN_NOTES = "notes"

        private const val TABLE_SETTINGS = "user_settings"
        private const val COLUMN_KEY = "setting_key"
        private const val COLUMN_VALUE = "setting_value"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createLogsTable = """
            CREATE TABLE $TABLE_DAILY_LOGS (
                $COLUMN_DATE TEXT PRIMARY KEY,
                $COLUMN_IS_PERIOD INTEGER NOT NULL,
                $COLUMN_FLOW TEXT NOT NULL,
                $COLUMN_SYMPTOMS TEXT NOT NULL,
                $COLUMN_PAIN INTEGER NOT NULL,
                $COLUMN_NOTES TEXT NOT NULL
            )
        """.trimIndent()

        val createSettingsTable = """
            CREATE TABLE $TABLE_SETTINGS (
                $COLUMN_KEY TEXT PRIMARY KEY,
                $COLUMN_VALUE TEXT NOT NULL
            )
        """.trimIndent()

        db.execSQL(createLogsTable)
        db.execSQL(createSettingsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_DAILY_LOGS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SETTINGS")
        onCreate(db)
    }

    fun saveDailyLog(log: DailyLog) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_DATE, log.date.toString())
            put(COLUMN_IS_PERIOD, if (log.isPeriodDay) 1 else 0)
            put(COLUMN_FLOW, log.flowLevel.name)
            put(COLUMN_SYMPTOMS, log.symptoms.joinToString(",") { it.id })
            put(COLUMN_PAIN, log.painIntensity)
            put(COLUMN_NOTES, log.notes)
        }
        db.insertWithOnConflict(
            TABLE_DAILY_LOGS,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE,
        )
    }

    fun deleteDailyLog(date: LocalDate) {
        val db = writableDatabase
        db.delete(TABLE_DAILY_LOGS, "$COLUMN_DATE = ?", arrayOf(date.toString()))
    }

    fun getDailyLog(date: LocalDate): DailyLog? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_DAILY_LOGS,
            null,
            "$COLUMN_DATE = ?",
            arrayOf(date.toString()),
            null,
            null,
            null,
        )
        cursor.use {
            if (it.moveToFirst()) {
                val isPeriod = it.getInt(it.getColumnIndexOrThrow(COLUMN_IS_PERIOD)) == 1
                val flowStr = it.getString(it.getColumnIndexOrThrow(COLUMN_FLOW))
                val flow = try { FlowLevel.valueOf(flowStr) } catch (_: Exception) { FlowLevel.NONE }
                val symptomsStr = it.getString(it.getColumnIndexOrThrow(COLUMN_SYMPTOMS))
                val symptoms = symptomsStr.split(",")
                    .filter { s -> s.isNotBlank() }
                    .mapNotNull { id -> Symptom.fromId(id.trim()) }
                    .toSet()
                val pain = it.getInt(it.getColumnIndexOrThrow(COLUMN_PAIN))
                val notes = it.getString(it.getColumnIndexOrThrow(COLUMN_NOTES))

                return DailyLog(
                    date = date,
                    isPeriodDay = isPeriod,
                    flowLevel = flow,
                    symptoms = symptoms,
                    painIntensity = pain,
                    notes = notes,
                )
            }
        }
        return null
    }

    fun getAllLogs(): List<DailyLog> {
        val db = readableDatabase
        val logs = mutableListOf<DailyLog>()
        val cursor = db.query(
            TABLE_DAILY_LOGS,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_DATE ASC",
        )
        cursor.use {
            while (it.moveToNext()) {
                val dateStr = it.getString(it.getColumnIndexOrThrow(COLUMN_DATE))
                val date = LocalDate.parse(dateStr)
                val isPeriod = it.getInt(it.getColumnIndexOrThrow(COLUMN_IS_PERIOD)) == 1
                val flowStr = it.getString(it.getColumnIndexOrThrow(COLUMN_FLOW))
                val flow = try { FlowLevel.valueOf(flowStr) } catch (_: Exception) { FlowLevel.NONE }
                val symptomsStr = it.getString(it.getColumnIndexOrThrow(COLUMN_SYMPTOMS))
                val symptoms = symptomsStr.split(",")
                    .filter { s -> s.isNotBlank() }
                    .mapNotNull { id -> Symptom.fromId(id.trim()) }
                    .toSet()
                val pain = it.getInt(it.getColumnIndexOrThrow(COLUMN_PAIN))
                val notes = it.getString(it.getColumnIndexOrThrow(COLUMN_NOTES))

                logs.add(
                    DailyLog(
                        date = date,
                        isPeriodDay = isPeriod,
                        flowLevel = flow,
                        symptoms = symptoms,
                        painIntensity = pain,
                        notes = notes,
                    ),
                )
            }
        }
        return logs
    }

    fun getLogsForRange(startDate: LocalDate, endDate: LocalDate): List<DailyLog> {
        val db = readableDatabase
        val logs = mutableListOf<DailyLog>()
        val cursor = db.query(
            TABLE_DAILY_LOGS,
            null,
            "$COLUMN_DATE BETWEEN ? AND ?",
            arrayOf(startDate.toString(), endDate.toString()),
            null,
            null,
            "$COLUMN_DATE ASC",
        )
        cursor.use {
            while (it.moveToNext()) {
                val dateStr = it.getString(it.getColumnIndexOrThrow(COLUMN_DATE))
                val date = LocalDate.parse(dateStr)
                val isPeriod = it.getInt(it.getColumnIndexOrThrow(COLUMN_IS_PERIOD)) == 1
                val flowStr = it.getString(it.getColumnIndexOrThrow(COLUMN_FLOW))
                val flow = try { FlowLevel.valueOf(flowStr) } catch (_: Exception) { FlowLevel.NONE }
                val symptomsStr = it.getString(it.getColumnIndexOrThrow(COLUMN_SYMPTOMS))
                val symptoms = symptomsStr.split(",")
                    .filter { s -> s.isNotBlank() }
                    .mapNotNull { id -> Symptom.fromId(id.trim()) }
                    .toSet()
                val pain = it.getInt(it.getColumnIndexOrThrow(COLUMN_PAIN))
                val notes = it.getString(it.getColumnIndexOrThrow(COLUMN_NOTES))

                logs.add(
                    DailyLog(
                        date = date,
                        isPeriodDay = isPeriod,
                        flowLevel = flow,
                        symptoms = symptoms,
                        painIntensity = pain,
                        notes = notes,
                    ),
                )
            }
        }
        return logs
    }

    fun saveSetting(key: String, value: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_KEY, key)
            put(COLUMN_VALUE, value)
        }
        db.insertWithOnConflict(TABLE_SETTINGS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getSetting(key: String, defaultValue: String): String {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_SETTINGS,
            arrayOf(COLUMN_VALUE),
            "$COLUMN_KEY = ?",
            arrayOf(key),
            null,
            null,
            null,
        )
        cursor.use {
            if (it.moveToFirst()) {
                return it.getString(it.getColumnIndexOrThrow(COLUMN_VALUE))
            }
        }
        return defaultValue
    }

    fun clearAllData() {
        val db = writableDatabase
        db.delete(TABLE_DAILY_LOGS, null, null)
        db.delete(TABLE_SETTINGS, null, null)
    }
}
