package com.example.implementationbiometricauthentication.room.database

import com.example.implementationbiometricauthentication.room.entitie.User

class UserRepository(private val userDao: UserDao) {
    suspend fun saveUser(user: User) {
        userDao.insert(user)
    }

    suspend fun removeAllUser() {
        userDao.deleteAll()
    }

    suspend fun read(): List<User> {
        return userDao.getAll()
    }
}