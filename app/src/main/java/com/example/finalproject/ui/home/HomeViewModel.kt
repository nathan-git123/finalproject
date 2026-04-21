package com.example.finalproject.ui.home

import androidx.lifecycle.ViewModel
import com.example.finalproject.data.SP500Validator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    validator: SP500Validator
) : ViewModel() {
    val allTickers: List<String> = validator.tickers
}