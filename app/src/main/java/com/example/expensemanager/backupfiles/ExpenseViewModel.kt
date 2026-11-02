package com.example.expensemanager.backupfiles
//
//import android.util.Log
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import com.github.mikephil.charting.data.PieEntry
//import com.google.firebase.Firebase
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.firestore.Query
//import com.google.firebase.firestore.firestore
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.asStateFlow
//
//class ExpenseViewModel:ViewModel() {
//    private val _transactions = MutableStateFlow<List<Map<String, Any>>>(emptyList())
//    val transactions = _transactions.asStateFlow()
//
//    fun fetchTransactions(userId: String) {
//        Firebase.firestore.collection("users")
//            .document(userId)
//            .collection("transactions")
//            .orderBy("date", Query.Direction.DESCENDING)
//            .get()
//            .addOnSuccessListener { result ->
//                _transactions.value = result.map { it.data }
//
//            }
//            .addOnFailureListener { exception ->
//                Log.w("ExpenseViewModel", "Error getting documents: ", exception)
//            }
//    }
//
//}