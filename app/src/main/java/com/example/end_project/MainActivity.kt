package com.example.end_project

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.end_project.databinding.ActivityMainBinding
import com.example.end_project.fragment.ListFragment
import com.example.end_project.fragment.MapFragment

class MainActivity : AppCompatActivity() { // 혹시 이 중괄호 '{' 가 지워졌었는지 확인해 보세요!

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            replaceFragment(ListFragment())
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_list -> {
                    replaceFragment(ListFragment())
                    true
                }
                R.id.menu_map -> {
                    replaceFragment(MapFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}