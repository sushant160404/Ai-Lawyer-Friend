package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LawyerDao {
    @Query("SELECT * FROM lawyers")
    fun getAllLawyers(): Flow<List<LawyerEntity>>

    @Query("SELECT * FROM lawyers WHERE isSaved = 1")
    fun getSavedLawyers(): Flow<List<LawyerEntity>>

    @Query("SELECT * FROM lawyers WHERE id = :id")
    suspend fun getLawyerById(id: String): LawyerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLawyers(lawyers: List<LawyerEntity>)

    @Update
    suspend fun updateLawyer(lawyer: LawyerEntity)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages WHERE threadId = :threadId ORDER BY timestamp ASC")
    fun getChatHistory(threadId: String): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllChatMessages(): Flow<List<ChatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatEntity): Long

    @Query("DELETE FROM chat_messages WHERE threadId = :threadId")
    suspend fun deleteThread(threadId: String)

    @Query("DELETE FROM chat_messages")
    suspend fun clearHistory()
}

@Dao
interface ConsultationDao {
    @Query("SELECT * FROM consultations ORDER BY createdAt DESC")
    fun getAllConsultations(): Flow<List<ConsultationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConsultation(consultation: ConsultationEntity): Long

    @Query("UPDATE consultations SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)
}

@Dao
interface LegalNoteDao {
    @Query("SELECT * FROM legal_notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<LegalNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: LegalNoteEntity): Long

    @Query("DELETE FROM legal_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)

    @Update
    suspend fun updateNote(note: LegalNoteEntity)
}
