
package com.example.myapplication

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplication.databinding.ActivityViewPostBinding
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ViewPost : AppCompatActivity() {

    private lateinit var binding: ActivityViewPostBinding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var postAdapter: PostAdapter
    private val posts = mutableListOf<PostData>()
    private val allPosts = mutableListOf<PostData>()
    private val userPosts = mutableListOf<PostData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()

        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        postAdapter = PostAdapter(posts, this::onPostLongClick, this::onPostClick)
        binding.recyclerView.adapter = postAdapter

        binding.fab.setOnClickListener {
            val intent = Intent(this, UploadPost::class.java)
            startActivity(intent)
        }

        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("All Ads"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Your Ads"))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showAllPosts()
                    1 -> showUserPosts()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterPosts(newText)
                return true
            }
        })

        /*binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Handle home navigation
                    // E.g., start home activity
                    true
                }
                R.id.navigation_profile -> {
                    // Handle profile navigation
                    // E.g., start profile activity
                    true
                }
                R.id.navigation_back -> {
                    // Handle back navigation
                    true
                }
                R.id.navigation_logout -> {
                    // Handle logout navigation
                    firebaseAuth.signOut()
                    val intent = Intent(this, Login::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }*/

        loadPosts()
    }

    private fun loadPosts() {
        val currentUserUid = firebaseAuth.currentUser?.uid

        val usersRef = firebaseDatabase.reference.child("users")
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                allPosts.clear()
                userPosts.clear()
                for (userSnapshot in dataSnapshot.children) {
                    val userId = userSnapshot.key
                    userId?.let {
                        val postsRef = userSnapshot.child("posts")
                        for (postSnapshot in postsRef.children) {
                            val post = postSnapshot.getValue(PostData::class.java)
                            post?.let {
                                allPosts.add(it)
                                if (userId == currentUserUid) {
                                    userPosts.add(it)
                                }
                            }
                        }
                    }
                }
                showAllPosts()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun showAllPosts() {
        posts.clear()
        posts.addAll(allPosts)
        postAdapter.notifyDataSetChanged()

    }

    private fun showUserPosts(){
        posts.clear()
        posts.addAll(userPosts)
        postAdapter.notifyDataSetChanged()

    }

    private fun filterPosts(query: String?) {
        val filteredPosts = if (query.isNullOrEmpty()) {
            posts
        } else {
            val filteredQuery = query.lowercase()
            posts.filter {
                (it.price?.contains(filteredQuery, true) == true) ||
                        (it.description?.contains(filteredQuery, true) == true) ||
                        (it.place?.contains(filteredQuery, true) == true)
            }
        }
        postAdapter.updateList(filteredPosts)
    }

    private fun onPostClick(post: PostData) {
        val intent = Intent(this, Map::class.java).apply {
            putExtra("price", post.price)
            putExtra("description", post.description)
            putExtra("place", post.place)
            putExtra("contactNumber", post.contactNumber)
            putExtra("photoUrl", post.photoDownloadUrl)
        }
        startActivity(intent)
    }


    private fun onPostLongClick(post: PostData) {
        val currentUserUid = firebaseAuth.currentUser?.uid

        if (currentUserUid != null) {
            val userRef = firebaseDatabase.reference.child("users").child(currentUserUid).child("posts")

            AlertDialog.Builder(this)
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Yes") { dialogInterface: DialogInterface, i: Int ->
                    userRef.orderByChild("photoDownloadUrl").equalTo(post.photoDownloadUrl)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for (postSnapshot in snapshot.children) {
                                    postSnapshot.ref.removeValue()
                                        .addOnSuccessListener {
                                            Toast.makeText(this@ViewPost, "Post deleted successfully", Toast.LENGTH_SHORT).show()
                                            loadPosts()
                                        }
                                        .addOnFailureListener { _ ->
                                            Toast.makeText(this@ViewPost, "Failed to delete post", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(this@ViewPost, "Failed to delete post", Toast.LENGTH_SHORT).show()
                            }
                        })
                }
                .setNegativeButton("No", null)
                .show()
        }
    }
}
