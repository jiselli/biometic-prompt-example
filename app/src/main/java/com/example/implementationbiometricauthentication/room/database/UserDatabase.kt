package com.example.implementationbiometricauthentication.room.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.implementationbiometricauthentication.room.entitie.User

@Database(entities = [User::class], version = 1)
abstract class UserDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var userDatabase: UserDatabase? = null

        fun getInstance(context: Context): UserDatabase {
            val database = userDatabase
            if (database != null) {
                return database
            }

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UserDatabase::class.java,
                    "database"
                ).build()
                userDatabase = instance
                return instance
            }
        }
    }
}