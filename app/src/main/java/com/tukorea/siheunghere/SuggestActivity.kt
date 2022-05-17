package com.tukorea.siheunghere

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.lakue.pagingbutton.OnPageSelectListener
import kotlinx.android.synthetic.main.main_title.*
import kotlinx.android.synthetic.main.suggest_activity.*


class SuggestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.suggest_activity)

        var context : Context = this.applicationContext
        var db : FirebaseFirestore = FirebaseFirestore.getInstance()

        //다시 건의글 버튼 누르면 홈화면으로 돌아감
        title_suggestBtn.setOnClickListener() {
            finish()
        }



        var collection : CollectionReference = db.collection("suggests")
        var suggests = ArrayList<QueryDocumentSnapshot>()
        collection.get().addOnCompleteListener(OnCompleteListener<QuerySnapshot>{
            fun onComplete(task : Task<QuerySnapshot>) {
                if(task.isSuccessful) {
                    for(document : QueryDocumentSnapshot in task.result) {
                        suggests.add(document)
                    }
                }
            }
        })

        var suggestListAdapter = SuggestListViewAdapter(this, suggests)
        suggestListView.adapter = suggestListAdapter


        //임시로 지정해둔 최대 페이지 번호
        var max_page = 10

        //한 번에 표시되는 버튼 수 (기본값 : 5)
        paging_btnList.setPageItemCount(4)


        //총 페이지 버튼 수와 현재 페이지 설정
        paging_btnList.addBottomPageButton(max_page, 1)

        //페이지 리스너를 클릭했을 때의 이벤트
        paging_btnList.setOnPageSelectListener(object : OnPageSelectListener {
            //PrevButton Click
            override fun onPageBefore(now_page: Int) {
                //prev 버튼클릭 - 버튼 재설정되고 그려짐
                paging_btnList.addBottomPageButton(max_page, now_page)
                //해당 페이지에 대한 소스 코드 작성
                //...
            }

            override fun onPageCenter(now_page: Int) {
                //...
            }

            //NextButton Click
            override fun onPageNext(now_page: Int) {
                //next 버튼클릭 - 버튼 재설정되고 그려짐
                paging_btnList.addBottomPageButton(max_page, now_page)
                //해당 페이지에 대한 소스 코드 작성
                //...
            }
        })
    }
}