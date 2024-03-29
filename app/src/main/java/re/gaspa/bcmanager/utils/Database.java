package re.gaspa.bcmanager.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import re.gaspa.bcmanager.ui.models.BusinessCard;

public class Database {

    private static SQLiteDatabase db = null;
    private static final String dbName = "BCManager.db";
    private static final String tableName = "BusinessCard";
    private static ArrayList<BusinessCard> bclist = null;

    public static SQLiteDatabase getDatabase(boolean clear) {
        if (clear) {
            File dbFile = new File(Environment.getExternalStorageDirectory(), dbName);
            dbFile.delete();
            Database.db = null;
        }
        return getDatabase();
    }

    public static SQLiteDatabase getDatabase() {
        if (Database.db == null) {
            File dbFile = new File(Environment.getExternalStorageDirectory(), dbName);
            Database.db = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
        }
        return Database.db;
    }

    public static void createTable(Context context) {
        SQLiteDatabase db = Database.getDatabase();
        StringBuilder query = new StringBuilder();
        query.append("CREATE TABLE IF NOT EXISTS " + tableName + " (");

        query.append("ID INTEGER PRIMARY KEY AUTOINCREMENT, ");
        query.append("PREF INTEGER, ");

        query.append("NOME TEXT, ");
        query.append("TELEFONO TEXT, ");
        query.append("EMAIL TEXT, ");
        query.append("SITO TEXT, ");
        query.append("TELEGRAM TEXT, ");
        query.append("COLOR TEXT, ");

        query.append("CASACITTA TEXT, ");
        query.append("CASASTRADA TEXT, ");
        query.append("CASALAT REAL, ");
        query.append("CASALNG REAL, ");

        query.append("LAVORORUOLO TEXT, ");
        query.append("LAVOROLUOGO TEXT, ");
        query.append("LAVOROLAT REAL, ");
        query.append("LAVOROLNG REAL, ");

        query.append("PROFILO BLOB, ");
        query.append("SFONDO BLOB ");

        query.append(");");

        db.execSQL(query.toString());

        BusinessCard fake[] = Utils.getFakeBusinessCard(context);

        for (BusinessCard aFake : fake) Database.addBusinessCard(aFake);
    }

    public static ArrayList<BusinessCard> getBusinessCards() {
        SQLiteDatabase db = Database.getDatabase();
      //  if (bclist == null) {
            bclist = new ArrayList<>();
            Cursor dbCursor = db.query(tableName, null, null, null, null, null, null);
            if (dbCursor != null) {
                if (dbCursor.moveToFirst()) {
                    do {
                        BusinessCard businessCard = BusinessCard.loadFromCursor(dbCursor);
                        bclist.add(businessCard);
                    }
                    while (dbCursor.moveToNext());
                }
                dbCursor.close();
            }
       // }
        return bclist;
    }

    public static void addBusinessCard(BusinessCard businessCard) {
        SQLiteDatabase db = Database.getDatabase();
        businessCard.setId(Database.getNextId());
        db.insert(tableName, null, businessCard.getContentValues());
        getBusinessCards().add(businessCard);
    }

    private static Integer getNextId() {
        Integer ret = -1;
        ArrayList<BusinessCard> list = Database.getBusinessCards();
        for (BusinessCard bc : list) {
            if (bc.getId() > ret)
                ret = bc.getId();
        }
        ret++;
        return ret;
    }

    public static void deleteBusinessCard(BusinessCard businessCard) {
        Integer id = businessCard.getId();
        SQLiteDatabase db = Database.getDatabase();
        ArrayList<BusinessCard> list = getBusinessCards();
        for (int i = 0; i < list.size(); i++) {
            if (Objects.equals(list.get(i).getId(), id))
                list.remove(i);
        }
        db.delete(tableName, "ID = ?", new String[]{String.valueOf(id)});
    }

    public static ArrayList<BusinessCard> getBusinessCards(boolean preferite, String name) {
        ArrayList<BusinessCard> ret = new ArrayList<>();
        ArrayList<BusinessCard> all = Database.getBusinessCards();
        name = name.toLowerCase();
        for (BusinessCard bc : all) {
            if ((!preferite || bc.getPreferito()) && bc.getNome().toLowerCase().contains(name))
                ret.add(bc);
        }
        return ret;
    }

    public static void setPreferite(BusinessCard businessCard, boolean preferite) {
        SQLiteDatabase db = Database.getDatabase();
        businessCard.setPreferito(preferite);
        db.update(tableName, businessCard.getContentValues(), "ID = ?", new String[]{businessCard.getId().toString()});
    }

    static void clearTable() {
        SQLiteDatabase db = Database.getDatabase();
        db.delete(tableName, null, null);
        bclist = new ArrayList<>();
    }

    static void resetPreferite() {
        SQLiteDatabase db = Database.getDatabase();
        ContentValues cv = new ContentValues();
        cv.put("PREF", 0);
        db.update(tableName, cv, null, null);
    }
}
