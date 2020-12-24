package com.github.rcd27

import androidx.viewpager2.widget.ViewPager2
import io.reactivex.subjects.BehaviorSubject

class PageCallback(
    private val fakeSize: Int,
    private val viewPager: ViewPager2
) : ViewPager2.OnPageChangeCallback() {

    var currentPosition: Int = 0

    val pageSelected: BehaviorSubject<Int> = BehaviorSubject.create<Int>()

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

    override fun onPageSelected(position: Int) {
        currentPosition = position
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        pageSelected.onNext(position)
    }
}