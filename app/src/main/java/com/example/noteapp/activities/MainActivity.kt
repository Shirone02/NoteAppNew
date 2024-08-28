package com.example.noteapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.noteapp.R
import com.example.noteapp.database.NoteDatabase
import com.example.noteapp.databinding.ActivityMainBinding
import com.example.noteapp.fragments.EditCategoriesFragment
import com.example.noteapp.fragments.NoteWithCategoryFragment
import com.example.noteapp.fragments.NotesFragment
import com.example.noteapp.fragments.TrashFragment
import com.example.noteapp.fragments.UncategorizedFragment
import com.example.noteapp.models.Category
import com.example.noteapp.repository.CategoryRepository
import com.example.noteapp.repository.NoteCategoryRepository
import com.example.noteapp.repository.NoteRepository
import com.example.noteapp.viewmodel.CategoryViewModel
import com.example.noteapp.viewmodel.CategoryViewModelFactory
import com.example.noteapp.viewmodel.NoteCategoryViewModel
import com.example.noteapp.viewmodel.NoteCategoryViewModelFactory
import com.example.noteapp.viewmodel.NoteViewModel
import com.example.noteapp.viewmodel.NoteViewModelFactory
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    lateinit var noteViewModel: NoteViewModel
    lateinit var categoryViewModel: CategoryViewModel
    lateinit var noteCategoryViewModel: NoteCategoryViewModel
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        setUpViewModel()

        updateCategoryList()

        setSupportActionBar(binding.topAppBar)

        // firebase authentication
//        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestIdToken(getString(R.string.default_web_client_id))
//            .requestEmail()
//            .build()
//
//
//        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
//        val mAuth = Firebase.auth
//        val user = mAuth.currentUser
//        if (user != null) {
//            val userName = user.displayName
//            Toast.makeText(this, "Welcome $userName", Toast.LENGTH_SHORT).show()
//        } else {
//            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
//        }

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

    private fun signOutAndStartSignInActivity() {
//        mAuth.signOut()
//
//        mGoogleSignInClient.signOut().addOnCompleteListener(this) {
//            val intent = Intent(this@MainActivity, SignInActivity::class.java)
//            startActivity(intent)
//            finish()
//        }

        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this@MainActivity, SignInActivity::class.java))
        finish()
    }

    private fun updateCategoryList() {
        categoryViewModel.getAllCategory().observe(this) { categories ->
            addCategoriesToDrawer(categories)
        }
    }

    private fun addCategoriesToDrawer(categories: List<Category>) {
        val menuCategory = binding.navView.menu.findItem(R.id.categories)?.subMenu ?: return

        // Xóa các mục cũ trước khi thêm mới
        menuCategory.clear()
        for (category in categories) {
            val menuItem =
                menuCategory.add(Menu.NONE, category.id, Menu.NONE, category.categoryName)
            menuItem.setIcon(R.drawable.ic_categorized)
        }

        if (categories.isNotEmpty()) {
            val menuItem =
                menuCategory.add(Menu.NONE, R.id.fragment_uncategorized, Menu.NONE, "Uncategorized")
            menuItem.setIcon(R.drawable.ic_uncategorized)
        }

        menuCategory.add(Menu.NONE, R.id.fragment_edit_categories, Menu.NONE, "Edit categories")
            .setIcon(R.drawable.ic_edit_categories)
    }

    private fun setUpViewModel() {
        val noteRepository = NoteRepository(NoteDatabase(this))
        val categoryRepository = CategoryRepository(NoteDatabase(this))
        val noteCategoryRepository = NoteCategoryRepository(NoteDatabase(this))

        val categoryViewModelProviderFactory =
            CategoryViewModelFactory(application, categoryRepository)
        val viewModelProviderFactory = NoteViewModelFactory(application, noteRepository)
        val noteCategoryViewModelFactory =
            NoteCategoryViewModelFactory(application, noteCategoryRepository)

        categoryViewModel =
            ViewModelProvider(this, categoryViewModelProviderFactory)[CategoryViewModel::class.java]

        noteViewModel = ViewModelProvider(this, viewModelProviderFactory)[NoteViewModel::class.java]
        noteCategoryViewModel =
            ViewModelProvider(this, noteCategoryViewModelFactory)[NoteCategoryViewModel::class.java]
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Xử lý sự kiện khi một mục trong Navigation Drawer được chọn
        val categoryId = item.itemId
        // Tạo Fragment và truyền categoryId bằng arguments
        val fragment = NoteWithCategoryFragment().apply {
            arguments = Bundle().apply {
                putInt("categoryId", categoryId)
            }
        }
        // Thay thế nội dung bằng fragment mới
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()

        when (item.itemId) {
            R.id.nav_note -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, NotesFragment()).commit()

            R.id.nav_edit_categories -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, EditCategoriesFragment()).commit()

            R.id.nav_backup -> startActivity(Intent(this, BackupActivity::class.java))

            R.id.nav_trash -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TrashFragment()).commit()

            R.id.nav_setting -> startActivity(Intent(this, SettingsActivity::class.java))

            R.id.nav_rate -> false

            R.id.nav_help -> startActivity(Intent(this, HelpActivity::class.java))

            R.id.nav_policy -> startActivity(Intent(this, PrivacyPolicyActivity::class.java))

            R.id.fragment_uncategorized -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, UncategorizedFragment()).commit()

            R.id.fragment_edit_categories -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, EditCategoriesFragment()).commit()

            R.id.nav_logout -> signOutAndStartSignInActivity()
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