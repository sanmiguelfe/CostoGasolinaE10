package com.mexiti.costogasolina.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mexiti.costogasolina.model.GasolineTransaction
import com.mexiti.costogasolina.room.GasolineDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GasolineViewModel(
    private val gasolineDao: GasolineDao
) : ViewModel() {

    // Lista de registros que observa la pantalla del historial.
    // Se actualiza cuando cambia la información en Room.
    val transaction: StateFlow<List<GasolineTransaction>> =
        gasolineDao.getAllTransactions()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    // Crea un registro con los datos ingresados y lo guarda.
    fun addTransaction(
        price: Double,
        liters: Double,
        tip: Double,
        date: String,
        kilometers: Double
    ) {
        viewModelScope.launch {

            val newTransaction = GasolineTransaction(
                pricePerLiter = price,
                liters = liters,
                tip = tip,
                date = date,
                kilometers = kilometers
            )

            gasolineDao.insertTransaction(newTransaction)
        }
    }

    // Consulta un registro por su ID para cargar sus datos en pantalla.
    suspend fun getTransactionById(id: Int): GasolineTransaction? {
        return gasolineDao.getTransactionById(id)
    }

    // Guarda los cambios realizados en un registro existente.
    fun updateTransaction(transaction: GasolineTransaction) {
        viewModelScope.launch {
            gasolineDao.updateTransaction(transaction)
        }
    }

    // Elimina de la base de datos el registro seleccionado.
    fun deleteTransaction(transaction: GasolineTransaction) {
        viewModelScope.launch {
            gasolineDao.deleteTransaction(transaction)
        }
    }
}