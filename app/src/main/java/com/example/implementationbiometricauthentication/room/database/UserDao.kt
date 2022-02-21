package com.example.implementationbiometricauthentication.room.database

import androidx.room.*
import com.example.implementationbiometricauthentication.room.entitie.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User)

    @Update
    fun update(user: User)

    @Delete
    fun delete(user: User)

    @Query("delete from user")
    fun deleteAll()

    @Query("select * from user order by email desc")
    fun getAll(): List<User>
}