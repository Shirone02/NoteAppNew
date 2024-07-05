package com.example.noteapp.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.noteapp.R
import com.example.noteapp.adapter.ListNoteAdapter
import com.example.noteapp.database.NoteDatabase
import com.example.noteapp.databinding.ActivityMainBinding
import com.example.noteapp.fragments.BackupFragment
import com.example.noteapp.fragments.EditCategoriesFragment
import com.example.noteapp.fragments.HelpFragment
import com.example.noteapp.fragments.NotesFragment
import com.example.noteapp.fragments.PrivacyPolicyFragment
import com.example.noteapp.fragments.RateFragment
import com.example.noteapp.fragments.SettingFragment
import com.example.noteapp.fragments.TrashFragment
import com.example.noteapp.fragments.UncategorizedFragment
import com.example.noteapp.listeners.OnItemClickListener
import com.example.noteapp.models.Note
import com.example.noteapp.repository.CategoryRepository
import com.example.noteapp.repository.NoteRepository
import com.example.noteapp.viewmodel.CategoryViewModel
import com.example.noteapp.viewmodel.CategoryViewModelFactory
import com.example.noteapp.viewmodel.NoteViewModel
import com.example.noteapp.viewmodel.NoteViewModelFactory
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import java.util.ArrayList
import java.util.Calendar


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    lateinit var noteViewModel: NoteViewModel
    lateinit var categoryViewModel: CategoryViewModel
    private lateinit var noteAdapter: ListNoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setUpViewModel()

        setSupportActionBar(binding.topAppBar)

        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.topAppBar,
            R.string.open_nav,
            R.string.close_nav
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, NotesFragment()).commit()
            binding.navView.setCheckedItem(R.id.nav_note)
        }
    }

    fun showDeleteIcon() {
        binding.topAppBar.menu.clear()
        binding.topAppBar.inflateMenu(R.menu.menu_selection)
        binding.topAppBar.setNavigationIcon(R.drawable.ic_back)
        binding.topAppBar.navigationIcon?.setTint(ContextCompat.getColor(this, R.color.white))
        binding.topAppBar.setNavigationOnClickListener {
            binding.topAppBar.menu.clear()
            binding.topAppBar.inflateMenu(R.menu.top_app_bar)
            binding.topAppBar.setNavigationIcon(R.drawable.ic_option)
            binding.topAppBar.navigationIcon?.setTint(ContextCompat.getColor(this, R.color.white))
            binding.topAppBar.setNavigationOnClickListener {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }
            binding.topAppBar.setTitle("Notepad Free")
        }
    }


    private fun setUpViewModel() {
        val noteRepository = NoteRepository(NoteDatabase(this))
        val categoryRepository = CategoryRepository(NoteDatabase(this))

        val categoryViewModelProviderFactory =
            CategoryViewModelFactory(application, categoryRepository)
        val viewModelProviderFactory = NoteViewModelFactory(application, noteRepository)

        categoryViewModel =
            ViewModelProvider(this, categoryViewModelProviderFactory)[CategoryViewModel::class.java]
        noteViewModel = ViewModelProvider(this, viewModelProviderFactory)[NoteViewModel::class.java]
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_note -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, NotesFragment()).commit()

            R.id.nav_edit_categories -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, EditCategoriesFragment()).commit()

            R.id.nav_backup -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, BackupFragment()).commit()

            R.id.nav_trash -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TrashFragment()).commit()

            R.id.nav_uncategorized -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, UncategorizedFragment()).commit()

            R.id.nav_setting -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SettingFragment()).commit()

            R.id.nav_rate -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RateFragment()).commit()

            R.id.nav_help -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HelpFragment()).commit()

            R.id.nav_policy -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PrivacyPolicyFragment()).commit()
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}