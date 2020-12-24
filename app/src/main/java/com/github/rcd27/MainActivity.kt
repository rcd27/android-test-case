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
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.main_activity.*
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

data class JsonDto(
    val title: String,
    val url: String
)

interface Api {
    @GET("/a_test_1/test_app.json")
    fun getJson(): Single<List<JsonDto>>
}

class MainActivity : AppCompatActivity() {

    private val cd = CompositeDisposable()

    //region Can be moved to standalone layer, but I was lazy
    private val okHttpClient = OkHttpClient
        .Builder()
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://lovetest.me/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
        .build()

    private val api = retrofit.create(Api::class.java)
    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        val adapter = ViewPagerAdapter()
        viewPager.adapter = adapter

        api.getJson()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { list ->
                // FIXME: поломается, если trueListSize == 0
                val modifiedList = list.takeLast(2) + list

                adapter.submitList(modifiedList)

                viewPager.setCurrentItem(2, false)
                val pageCallback = PageCallback(modifiedList.size, viewPager)
                viewPager.registerOnPageChangeCallback(pageCallback)

                pageCallback.pageSelected
                    .subscribeOn(Schedulers.computation())
                    .debounce(50, TimeUnit.MILLISECONDS)
                    .distinctUntilChanged()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { position ->
                        header.text = modifiedList[position].title
                    }
                    ?.let { cd.add(it) }
            }
            .let { cd.add(it) }
    }
}

class ViewPagerAdapter :
    ListAdapter<JsonDto, ViewPagerHolder>(object : DiffUtil.ItemCallback<JsonDto>() {
        override fun areItemsTheSame(oldItem: JsonDto, newItem: JsonDto): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: JsonDto, newItem: JsonDto): Boolean {
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

    fun bind(item: JsonDto) {
        Picasso.get()
            .load(item.url)
            .error(R.drawable.ic_android_black_24dp)
            .into(imageView)
    }
}
