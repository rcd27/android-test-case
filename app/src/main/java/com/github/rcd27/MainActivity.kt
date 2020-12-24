package com.github.rcd27

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.main_activity.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        val adapter = ViewPagerAdapter()
        viewPager.adapter = adapter

        val trueList = listOf(
            "https://lovetest.me/a_test_1/test1.png",
            "https://lovetest.me/a_test_1/test2.png",
            "https://lovetest.me/a_test_1/test3.png"
        )

        // FIXME: поломается, если trueListSize == 0
        val modifiedList = trueList.takeLast(2) + trueList

        adapter.submitList(modifiedList)

        viewPager.setCurrentItem(2, false)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            var currentPosition: Int = 0
            val fakeSize = modifiedList.size

            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    if (currentPosition == 0) {
                        viewPager.setCurrentItem(fakeSize - 2, false);
                    } else if (currentPosition == fakeSize - 1) {
                        viewPager.setCurrentItem(1, false);
                    }
                } else if (state == ViewPager2.SCROLL_STATE_DRAGGING && currentPosition == fakeSize) {
                    //we scroll too fast and miss the state SCROLL_STATE_IDLE for the previous item
                    viewPager.setCurrentItem(2, false);
                }
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                // TODO add observable.debounce
                Log.d("X", "Page scrolled: $position")
            }

            override fun onPageSelected(position: Int) {
                currentPosition = position
            }
        })
    }
}

class ViewPagerAdapter :
    ListAdapter<String, ViewPagerHolder>(object : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_view_pager, parent, false)
        return ViewPagerHolder(view)
    }

    override fun onBindViewHolder(holder: ViewPagerHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class ViewPagerHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val imageView = view.findViewById<ImageView>(R.id.imageView)

    fun bind(item: String) {
        Picasso.get()
            .load(item)
            .error(R.drawable.ic_android_black_24dp)
            .into(imageView)
    }
}
