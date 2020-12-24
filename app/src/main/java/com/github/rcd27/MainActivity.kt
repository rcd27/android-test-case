package com.github.rcd27

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.main_activity.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private val cd = CompositeDisposable()

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

        val pageCallback = PageCallback(modifiedList.size, viewPager)
        viewPager.registerOnPageChangeCallback(pageCallback)

        pageCallback.pageSelected
            .subscribeOn(Schedulers.computation())
            .debounce(100, TimeUnit.MILLISECONDS)
            .distinctUntilChanged()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { position ->
                // FIXME: position не соответствует правде, потому что там modifiedList
                header.text = position.toString()
            }
            .let { cd.add(it) }
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
