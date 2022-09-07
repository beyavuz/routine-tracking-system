package com.example.proje.fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.proje.R
import com.example.proje.model.DatabaseEntry
import com.example.proje.util.DataBaseHelper
import com.example.proje.util.DataModelSource
import kotlinx.android.synthetic.main.fragment_summary.*
import org.w3c.dom.Text
import java.util.*


class SummaryFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var dropMenuItemList : ArrayList<String>
    private lateinit var dataModelSource : DataModelSource
    private lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view : View =  inflater.inflate(R.layout.fragment_summary, container, false)
        val button = view.findViewById<Button>(R.id.date_picker_button)
        val secilenUygulama = view.findViewById<TextView>(R.id.secilenuygulama)
        val dropMenu = view.findViewById<TextView>(R.id.uygulama_sec)
        val aramaButton = view.findViewById<Button>(R.id.ara_button)
        val secilen_tarih = view.findViewById<TextView>(R.id.secilen_tarih)

        val card_view = view.findViewById<CardView>(R.id.cardView_summary)
        val image_view = view.findViewById<ImageView>(R.id.image_view_summary)
        val text_view_summary = view.findViewById<TextView>(R.id.text_view_summary)
        val text_view_sure_summary = view.findViewById<TextView>(R.id.text_view_sure_summary)

        dataModelSource = DataModelSource.getInstance()
        dropMenuItemList = dataModelSource.forSummary()


        aramaButton.setOnClickListener {
            val veriYokText = view.findViewById<TextView>(R.id.textView4_veriyok)
            veriYokText.visibility = View.INVISIBLE
            card_view.visibility = View.INVISIBLE

            println("SECİLEN_TARİH: ${secilen_tarih.text}  uzunluğu ${secilen_tarih.text.length}  SECİLEN_UYGULAMA: ${secilenUygulama.text}")

            if(secilen_tarih.text=="" || secilenUygulama.text==""){
                println("evet girdi.....")
                var dialog = Dialog(this.requireContext())
                dialog.setContentView(R.layout.summary_fragment_no_date_and_app_entered)
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                var button_alert_dialog = dialog.findViewById<Button>(R.id.button_summary_alert_dialog)
                button_alert_dialog.setOnClickListener {
                    dialog.dismiss()
                }
                var uyari_text = dialog.findViewById<TextView>(R.id.text_whether_is_to_be_entered)
                if(secilen_tarih.text=="" && secilenUygulama.text!=""){
                    uyari_text.text = "Tarih girilmedi"
                }else if(secilen_tarih.text!="" && secilenUygulama.text==""){
                    uyari_text.text = "Uygulama girilmedi"
                }
                dialog.show()
            }else {

                val dialogBuilder = AlertDialog.Builder(this.context)
                dialogBuilder.setView(inflater.inflate(R.layout.summary_loading_dialog, null))
                dialogBuilder.setCancelable(false)
                var dialog = dialogBuilder.create()
                dialog.show()
                val handler = Handler()
                val runnable = Runnable {
                    if (dialog.isShowing()) {
                        var gelen_uygulama = getAppUsageFromDb(secilenUygulama.text.toString(), secilen_tarih.text.toString())
                        if (gelen_uygulama == null) {
                            println("NULLLL GELDİ....")
                            //     val veriYokText = view.findViewById<TextView>(R.id.textView4_veriyok)
                            veriYokText.visibility = View.VISIBLE
                        } else {
                            println("NULLLL GELMEDİ.....")
                            card_view.visibility = View.VISIBLE
                            var package_name = gelen_uygulama.packageName
                            image_view.setImageDrawable(requireContext().packageManager.getApplicationIcon(package_name))
                            text_view_summary.text = gelen_uygulama.app_name
                            var saat = (gelen_uygulama.usage_app.div(1000).div(60).toInt()) / 60 //(apps.kullanimSuresi.div(1000).div(60).toInt())/60
                            var dakika = (gelen_uygulama.usage_app.div(1000).div(60).toInt()) % 60  //(apps.kullanimSuresi.div(1000).div(60).toInt())% 60
                            if (saat == 0 && dakika == 0) {
                                text_view_sure_summary.text = "Hiç kullanılmamış."
                            } else {
                                if (saat == 0) {
                                    text_view_sure_summary.text = "$dakika dakika"
                                } else if (dakika == 0) {
                                    text_view_sure_summary.text = "$saat saat"
                                } else {
                                    text_view_sure_summary.text = "$saat saat $dakika dakika"
                                }
                            }

                        }
                        dialog.dismiss()
                    }
                }

                dialog.setOnDismissListener(DialogInterface.OnDismissListener { handler.removeCallbacks(runnable) })
                handler.postDelayed(runnable, 3000)

            }


        }


        dropMenu.setOnClickListener {
            dialog = Dialog(context as Context)
            dialog.setContentView(R.layout.summary_drop_menu_dialog)
            dialog.window?.setLayout(800, 1000)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()



            var editText = dialog.findViewById<EditText>(R.id.edit_text_for_search_in_summary)
            var listView = dialog.findViewById<ListView>(R.id.list_view_for_summary)

            if(editText == null){
                println("********************************** edittext nullllllllll")
            }else if(listView == null){
                println("*******************************  listview nullllllllllll")
            }


            var diziAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, dropMenuItemList)
            listView.adapter = diziAdapter

            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    diziAdapter.filter.filter(s)
                }

                override fun afterTextChanged(s: Editable?) {
                }
            })

            listView.setOnItemClickListener(object : AdapterView.OnItemClickListener {
                override fun onItemClick(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                ) {
                    secilenUygulama.text = diziAdapter.getItem(position)
                    secilenUygulama.visibility = View.VISIBLE
                    println("SECİLEN UYGULAMA: ${diziAdapter.getItem(position)}")
                    dialog.dismiss()
                }

            })
        }



        var calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        button.setOnClickListener {
            var datePicker = DatePickerDialog(context as Context, DatePickerDialog.OnDateSetListener { view, year, month, day ->
                var ay = month + 1
                if(ay<10){

                }
                var tarih: String = "$day-$ay-$year"
                secilen_tarih.text = "$tarih"
                secilen_tarih.visibility = View.VISIBLE
                println("SECİLEN TARİH: ${tarih}")

            }, year, month, day)
            datePicker.show()
        }


        return view
    }

    override fun onResume() {
        super.onResume()

        if (arguments!=null){
            sharedPreferences =
                requireActivity().getSharedPreferences("AllInstalledApps", Context.MODE_PRIVATE)
            //    val editableFactory = Editable.Factory.getInstance()
            /*
            var liste = requireArguments().getStringArrayList("liste")
            liste!!.forEach {
                summaryApp.text = "${summaryApp.text}\n $it"
            }

             */

            var liste = sharedPreferences.all.forEach { t, any ->
           //     summaryApp.text = "${summaryApp.text}\n -- $t -- $any "
            }
        }
    }


    private fun initDatePicker(){
        var datesetlistener = DatePickerDialog.OnDateSetListener { view, year, month, day ->
            var ay = month + 1  //Ocak 0'dan başlıoy o yüzden 1 arttıyorum.


        }
    }

    private fun dateString(year: Int, month: Int, day: Int){

    }

    private fun getcalender(){
        var calendar : Calendar = Calendar.getInstance()

    }

    private fun getAppUsageFromDb(appName:String,appDate:String):DatabaseEntry?{
        var dbHelper = DataBaseHelper(this.requireContext(),"uygulamalar")
        return dbHelper.getApp(appName,appDate)

    }

}