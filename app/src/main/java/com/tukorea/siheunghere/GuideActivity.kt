package com.tukorea.siheunghere

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.guide_activity.*
import kotlinx.android.synthetic.main.guide_page.view.*


class GuideActivity: AppCompatActivity() {

    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.guide_activity)

        viewPager = Viewpager

        var layouts = arrayOf(R.layout.guide_page)

        viewPager.adapter = ViewPagerAdapter(layouts)   // 어댑터 생성
        viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL   // 가로방향

        // 스킵버튼
        btn_skip.setOnClickListener {
            var dlg = AlertDialog.Builder(this)
            dlg.setMessage("이용 가이드를 건너뛰시겠습니까?\n이용 가이드는 옵션을 통해 다시 볼 수 있습니다.")
            dlg.setNegativeButton("취소", null)
            dlg.setPositiveButton("건너뛰기") { dialog, which ->
                finish()
            }
            dlg.show()
        }

        // 다음버튼
        btn_next.setOnClickListener {
            if (viewPager.currentItem != 3) {
                viewPager.currentItem = viewPager.currentItem + 1
                if (viewPager.currentItem == 3){
                    btn_next.text="나가기"
                }
            } else {
                finish()
            }
        }

        // 현재 위치 표시
        viewPager.apply {
            registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    when(position){
                        0->{
                            //현재위치 표시
                            btn_next.text="다음"
                            dot_page1.setImageResource(R.drawable.dot_active)
                            dot_page2.setImageResource(R.drawable.dot_inactive)
                            dot_page3.setImageResource(R.drawable.dot_inactive)
                            dot_page4.setImageResource(R.drawable.dot_inactive)
                        }
                        1->{
                            //현재위치 표시
                            btn_next.text="다음"
                            dot_page1.setImageResource(R.drawable.dot_inactive)
                            dot_page2.setImageResource(R.drawable.dot_active)
                            dot_page3.setImageResource(R.drawable.dot_inactive)
                            dot_page4.setImageResource(R.drawable.dot_inactive)
                        }
                        2->{
                            //현재위치 표시
                            btn_next.text="다음"
                            dot_page1.setImageResource(R.drawable.dot_inactive)
                            dot_page2.setImageResource(R.drawable.dot_inactive)
                            dot_page3.setImageResource(R.drawable.dot_active)
                            dot_page4.setImageResource(R.drawable.dot_inactive)
                        }
                        3->{
                            //현재위치 표시
                            btn_next.text="나가기"
                            dot_page1.setImageResource(R.drawable.dot_inactive)
                            dot_page2.setImageResource(R.drawable.dot_inactive)
                            dot_page3.setImageResource(R.drawable.dot_inactive)
                            dot_page4.setImageResource(R.drawable.dot_active)
                        }
                    }
                }
            })
        }


    }
    // 뒤로가기 누르면 이전 페이지로 이동
    override fun onBackPressed() {
        if (viewPager.currentItem == 0) {
            // 사용자가 첫 번째 페이지에서 뒤로가기 버튼을 누르면 finish 하도록 하고
            super.onBackPressed()
        } else {
            // 그렇지 않으면 이전 페이지로 이동하게 한다.
            viewPager.currentItem = viewPager.currentItem - 1
        }
    }

    //뷰페이저 어댑터
    inner class ViewPagerAdapter(pageList: Array<Int>) : RecyclerView.Adapter<ViewPagerAdapter.PagerViewHolder>() {
        var item = pageList

        var guide_img_src = arrayOf(R.drawable.guide_img1, R.drawable.guide_img2, R.drawable.guide_img3, R.drawable.guide_img4)
        var guide_title = arrayOf("주변 공유자원 검색", "공유자원 목록 보기", "건의 글 게시판", "여기있시흥")
        var guide_content = arrayOf(
            "검색버튼을 사용하여\n주변 자원을 검색할 수 있습니다.\n자원을 스크롤하여 원하는 자원만 볼 수 있습니다.",
            "화살표를 위로 슬라이딩하면\n주변 자원을 목록으로 볼 수 있습니다.\n현재 위치에서 가까운 순서로 정렬되어 있습니다.",
            "건의 글 게시판을 사용하려면\n카카오 로그인이 필요합니다.\n다른 사람이 작성한 건의 글에 동의할 수 있고\n새롭게 작성할 수도 있습니다.",
            "주변 공유자원을 찾아보고 이용해보세요!\n필요한 공유자원을 건의해 보세요!\n이용 가이드는 옵션을 통해 다시 볼 수 있습니다."
        )
        var guide_mascot_src = arrayOf(R.drawable.guide_mascot1, R.drawable.guide_mascot2, R.drawable.guide_mascot3, R.drawable.guide_mascot4)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerViewHolder {
            var p = PagerViewHolder(parent, viewType)
            return p
        }

        override fun getItemCount(): Int = 4

        override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
            //gif 재생
            Glide.with(holder.v).load(guide_img_src[position]).into(holder.v.guide_img)
            //내용 설정
            holder.v.guide_title.text = guide_title[position]
            holder.v.guide_content.text = guide_content[position]
            when (position){
                0 -> {
                    //마스코트 왼쪽
                    holder.v.guide_mascot_left.setImageResource(guide_mascot_src[position])
                    holder.v.guide_mascot_right.setImageResource(0)
                }
                1 -> {
                    //마스코트 오른쪽
                    holder.v.guide_mascot_left.setImageResource(0)
                    holder.v.guide_mascot_right.setImageResource(guide_mascot_src[position])
                }
                2 ->{
                    //마스코트 오른쪽
                    holder.v.guide_mascot_left.setImageResource(0)
                    holder.v.guide_mascot_right.setImageResource(guide_mascot_src[position])
                }
                3 ->{
                    //마스코트 왼쪽
                    holder.v.guide_mascot_left.setImageResource(guide_mascot_src[position])
                    holder.v.guide_mascot_right.setImageResource(0)
                }
            }

        }

        inner class PagerViewHolder(parent: ViewGroup, viewType: Int) : RecyclerView.ViewHolder
            (LayoutInflater.from(parent.context).inflate(R.layout.guide_page, parent, false)){
            val v = itemView
        }
    }


}