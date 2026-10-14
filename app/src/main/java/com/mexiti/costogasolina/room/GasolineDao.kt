package com.mexiti.costogasolina.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mexiti.costogasolina.model.GasolineTransaction
import kotlinx.coroutines.flow.Flow

// Indica que esta interfaz contiene las operaciones de la base de datos.
@Dao
interface GasolineDao {

    // Agrega un registro. Si ya existe el mismo ID, lo reemplaza.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: GasolineTransaction)

    // Consulta todos los registros y avisa cuando la información cambia.
    @Query("SELECT * FROM gasoline_transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<GasolineTransaction>>

    // Busca un registro por su ID. Devuelve null si no lo encuentra.
    @Query("SELECT * FROM gasoline_transactions WHERE id = :id")
    suspend fun getTransactionById(id: Int): GasolineTransaction?

    // Actualiza el registro que tenga el mismo ID que el objeto recibido.
    @Update
    suspend fun updateTransaction(transaction: GasolineTransaction)

    // Elimina el registro identificado por el ID del objeto recibido.
    @Delete
    suspend fun deleteTransaction(transaction: GasolineTransaction)
}