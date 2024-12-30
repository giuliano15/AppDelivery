package com.example.project1732.via_cep.data.local.db


import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.project1732.via_cep.data.local.dao.AddressDao
import com.example.project1732.via_cep.data.local.entity.AddressEntity


@Database(entities = [AddressEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun addressDao(): AddressDao

}