package com.mmutert.freshfreezer.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.mmutert.freshfreezer.MainCoroutineRule
import com.mmutert.freshfreezer.util.TimeHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class StoredItemDaoTest {

    private lateinit var database: ItemDatabase

    @Before
    fun initDb() {
        // using an in-memory database because the information stored here disappears when the
        // process is killed
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ItemDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDb() = database.close()

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Test
    fun insertItemAndGetById() = runBlockingTest {
        val currentDate = TimeHelper.currentDateLocalized
        val currentDateTime = TimeHelper.currentDateTimeLocalized
        val item = StorageItem(
            0,
            "ItemName",
            condition = Condition.CHILLED,
            bestBeforeDate = currentDate,
            itemCreationDate = currentDateTime,
            lastChangedAtDate = currentDateTime
        )

        val id = database.itemDao().insertItem(item)

        val retrievedItem = database.itemDao().getStoredItem(id)

        assertThat("Could not retrieve item", retrievedItem as StorageItem, CoreMatchers.notNullValue())
        Assert.assertTrue(retrievedItem.id > 0)
        Assert.assertTrue(retrievedItem.name == "ItemName")
        Assert.assertTrue(retrievedItem.bestBeforeDate == currentDate)
        Assert.assertTrue(retrievedItem.lastChangedAtDate == currentDateTime)
        Assert.assertTrue(retrievedItem.itemCreationDate == currentDateTime)
    }

    @Test
    fun doubleInsertReplacesOldItem() = runBlockingTest {
        val currentDate = TimeHelper.currentDateLocalized
        val currentDateTime = TimeHelper.currentDateTimeLocalized
        val item = StorageItem(
            0,
            "ItemName",
            condition = Condition.CHILLED,
            bestBeforeDate = currentDate,
            itemCreationDate = currentDateTime,
            lastChangedAtDate = currentDateTime
        )

        database.itemDao().insertItem(item)

        val currentDateTimePlusOne = currentDateTime.plusDays(1)
        val newBestBeforeDate = currentDate.plusDays(1)
        val item2 = StorageItem(
            1,
            "ItemName2",
            condition = Condition.ROOM_TEMP,
            bestBeforeDate = newBestBeforeDate,
            itemCreationDate = currentDateTimePlusOne,
            lastChangedAtDate = currentDateTimePlusOne
        )

        val id = database.itemDao().insertItem(item2)

        val retrievedItem = database.itemDao().getStoredItem(id)

        assertThat("Could not retrieve item", retrievedItem as StorageItem, CoreMatchers.notNullValue())
        assertThat(retrievedItem.id, `is`(1L))
        assertThat(retrievedItem.name, `is`("ItemName2"))
        assertThat(retrievedItem.condition, `is`(Condition.ROOM_TEMP))
        assertThat(retrievedItem.bestBeforeDate, `is`(newBestBeforeDate))
        assertThat(retrievedItem.lastChangedAtDate, `is`(currentDateTimePlusOne))
        assertThat(retrievedItem.itemCreationDate, `is`(currentDateTimePlusOne))
    }
}