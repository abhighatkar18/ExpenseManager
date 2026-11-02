package com.example.expensemanager.backupfiles
//
//import android.os.Bundle
//import com.google.android.material.bottomnavigation.BottomNavigationView
//import androidx.appcompat.app.AppCompatActivity
//import androidx.navigation.fragment.NavHostFragment
//import com.example.expensemanager.databinding.ActivityHomePageBinding
//import com.example.expensemanager.fragments.Budget
//import com.example.expensemanager.fragments.Chatbot
//import com.example.expensemanager.fragments.Home
//import com.example.expensemanager.fragments.Profile
//
//class homePage : AppCompatActivity() {
//
//    private lateinit var binding: ActivityHomePageBinding
//    private lateinit var navView: BottomNavigationView
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityHomePageBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//        navView = binding.navView
//        navView.setOnItemSelectedListener { item ->
//            when (item.itemId) {
//                R.id.home -> {
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.nav_host_fragment_activity_home_page, Home())
//                        .commit()
//                    true
//                }
//                R.id.chatbot-> {
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.nav_host_fragment_activity_home_page, Chatbot())
//                        .commit()
//                    true
//                }
//                R.id.profile-> {
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.nav_host_fragment_activity_home_page, Profile())
//                        .commit()
//                    true
//                }
//                R.id.budget-> {
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.nav_host_fragment_activity_home_page, Budget())
//                        .commit()
//                    true
//                }
//                else -> false
//            }
//        }
//
//        navView.selectedItemId = R.id.home
//    }
//}