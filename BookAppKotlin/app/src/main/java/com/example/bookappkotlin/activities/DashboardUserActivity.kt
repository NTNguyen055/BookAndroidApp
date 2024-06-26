package com.example.bookappkotlin.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.bookappkotlin.BooksUserFragment
import com.example.bookappkotlin.databinding.ActivityDashboardUserBinding
import com.example.bookappkotlin.models.ModelCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardUserBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    private lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        setupWithViewPagerAdapter(binding.viewPager)
        binding.tabLayout.setupWithViewPager(binding.viewPager)

        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            startActivity( Intent(this, MainActivity::class.java))
            finish()
        }

        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null)
        {
            binding.backBtn.setOnClickListener {
                onBackPressed()
            }
        }
        else {
            binding.backBtn.visibility = View.GONE
        }

        //click vao nut mo profile
        binding.profileBtn.setOnClickListener {
            startActivity( Intent(this, ProfileActivity::class.java))
        }
    }

    private fun setupWithViewPagerAdapter( viewPager: ViewPager) {
        viewPagerAdapter = ViewPagerAdapter (
            supportFragmentManager,
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
            this
        )
        //init list
        categoryArrayList = ArrayList()

        //Load sach tu db thong qua bang Categories
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list
                categoryArrayList.clear()

                //Lay cac du lieu tu cac cot
                val modelAll = ModelCategory("01", "All", 1, "")
                val modelMostViewed = ModelCategory("01", "Most Viewed", 1, "")
                val modelMostDownloaded = ModelCategory("01", "Most Downloaded", 1, "")

                //Them vao danh sach
                categoryArrayList.add(modelAll)
                categoryArrayList.add(modelMostViewed)
                categoryArrayList.add(modelMostDownloaded)

                //Them vao recylerview
                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(
                        "${modelAll.id}",
                        "${modelAll.category}",
                        "${modelAll.uid}"
                    ), modelAll.category
                )

                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(
                        "${modelMostViewed.id}",
                        "${modelMostViewed.category}",
                        "${modelMostViewed.uid}"
                    ), modelMostViewed.category
                )

                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(
                        "${modelMostDownloaded.id}",
                        "${modelMostDownloaded.category}",
                        "${modelMostDownloaded.uid}"
                    ), modelMostDownloaded.category
                )
                //Tai lai danh sach refresh
                viewPagerAdapter.notifyDataSetChanged()

                //NOW load du lieu firebase tu db
                for (ds in snapshot.children)
                {
                    //Lay du lieu tu cot
                    val model = ds.getValue(ModelCategory::class.java)

                    categoryArrayList.add(model!!)

                    viewPagerAdapter.addFragment(
                        BooksUserFragment.newInstance(
                            "${model.id}",
                            "${model.category}",
                            "${model.uid}"
                        ), model.category
                    )
                    viewPagerAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
        //Dat lai con tro cho viewpager
        viewPager.adapter = viewPagerAdapter
    }

    private fun checkUser() {
        // Lấy giá trị hiện tại
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null)
        {
            // Không đăng nhập
            binding.subTitleTv.text = "Khách (không đăng nhập)"

            // Dang xuat
            binding.profileBtn.visibility = View.GONE
            binding.logoutBtn.visibility = View.GONE
        }
        else {
            // Đã đăng nhập
            val email = firebaseUser.email

            binding.subTitleTv.text = email

            // Da dang nhap
            binding.profileBtn.visibility = View.VISIBLE
            binding.logoutBtn.visibility = View.VISIBLE

        }
    }
    class ViewPagerAdapter (fm: FragmentManager, behavior: Int, private val context: Context) : FragmentPagerAdapter (fm, behavior) {

        private val fragmentsList: ArrayList<BooksUserFragment> = ArrayList()

        private val fragmentTitleList:  ArrayList<String> = ArrayList()

        override fun getCount(): Int {
            return fragmentsList.size
        }

        override fun getItem(position: Int): Fragment {
            return fragmentsList[position]
        }

        override fun getPageTitle(position: Int): CharSequence {
            return fragmentTitleList[position]
        }

        public fun addFragment (fragment: BooksUserFragment, title: String) {
            fragmentsList.add(fragment)

            fragmentTitleList.add(title)
        }

    }
}
