package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [MenuItemEntity::class, InvoiceEntity::class, AppConfigEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun posDao(): PosDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "quickbill_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.posDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: PosDao) {
            dao.saveConfig(AppConfigEntity(id = 1, adminPin = "1234", storeName = "Quick Café & Bistro"))
            dao.insertMenuItem(MenuItemEntity(name = "Club Sandwich", category = "Sandwich", price = 8.50, imageUrl = "https://images.unsplash.com/photo-1528735602780-2552fd46c7af?w=500", description = "Triple Decker toasted club sandwich with chicken and fresh veggies."))
            dao.insertMenuItem(MenuItemEntity(name = "Avocado Toast", category = "Toast", price = 6.75, imageUrl = "https://images.unsplash.com/photo-1588137378633-dea1336ce1e2?w=500", description = "Sourdough toast topped with smashed avocado, poached egg, and chilli flakes."))
            dao.insertMenuItem(MenuItemEntity(name = "Grilled Cheese Toast", category = "Toast", price = 5.25, imageUrl = "https://images.unsplash.com/photo-1525351484163-7529414344d8?w=500", description = "Classic buttery crispy grilled cheese sandwich."))
            dao.insertMenuItem(MenuItemEntity(name = "Iced Caramel Latte", category = "Beverage", price = 4.50, imageUrl = "https://images.unsplash.com/photo-1461023058943-07fcbe16d735?w=500", description = "Fresh espresso with milk and caramel syrup over ice."))
            dao.insertMenuItem(MenuItemEntity(name = "Classic Croissant", category = "Snack", price = 3.50, imageUrl = "https://images.unsplash.com/photo-1555507036-ab1f4038808a?w=500", description = "Flaky buttery French croissant baked fresh daily."))
        }
    }
}
