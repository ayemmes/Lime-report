package com.limereport.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.limereport.app.data.AppDatabase
import com.limereport.app.data.Rates
import com.limereport.app.data.RatesStore
import com.limereport.app.data.Shift
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShiftViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).shiftDao()
    private val ratesStore = RatesStore(app)

    val shifts: StateFlow<List<Shift>> =
        dao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _rates = MutableStateFlow(ratesStore.load())
    val rates: StateFlow<Rates> = _rates.asStateFlow()

    fun saveRates(rates: Rates) {
        ratesStore.save(rates)
        _rates.value = rates
    }

    fun save(shift: Shift) = viewModelScope.launch { dao.upsert(shift) }

    fun delete(shift: Shift) = viewModelScope.launch { dao.delete(shift) }
}
