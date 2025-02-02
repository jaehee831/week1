package com.example.week1_0627.ui.home

import ContactsAdapter
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

import android.content.Context
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.week1_0627.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

import java.io.FileOutputStream
import java.io.InputStream

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView // 변수 초기화를 나중으로 미룸
    private lateinit var contactsAdapter: ContactsAdapter
    private lateinit var contacts: MutableList<Contact>
    private lateinit var favoriteContacts: MutableList<Contact>

    // Fragment의 UI 초기화
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? { // nullable 객체 선언
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // 폴더에서 contacts.json 파일을 내부 저장소로 복사
        context?.let { copyAssetsToInternalStorage(it, "contacts.json") } //context가 null이 아닐 때만 작업 수행

        contacts = loadContactsFromJson().toMutableList()
        favoriteContacts = loadFavoriteContacts()

        // contacts 데이터 recyclerview에 바인딩, 아이템 클릭 이벤트 처리
        contactsAdapter = ContactsAdapter(contacts, { position ->
            deleteContact(position)
        }, { contact ->
            toggleFavorite(contact)
        }) // lambda 함수, posiiton 및 contact 받아서 각 메서드 실행

        recyclerView.adapter = contactsAdapter

        val addButton: Button = view.findViewById(R.id.button_add_contact)
        addButton.setOnClickListener {
            showAddContactDialog()
        }

        return view
    }

    private fun loadContactsFromJson(): List<Contact> {
        val file = File(context?.filesDir, "contacts.json")
        if (!file.exists()) return emptyList()
        val reader = file.bufferedReader()
        val contactType = object : TypeToken<List<Contact>>() {}.type
        return Gson().fromJson(reader, contactType) // JSON 데이터를 자바 객체로 변환하거나 그 반대로 변환
    }

    private fun saveContactsToJson() {
        val jsonString = Gson().toJson(contacts)
        val file = File(context?.filesDir, "contacts.json")
        file.bufferedWriter().use { it.write(jsonString) }
    }

    private fun loadFavoriteContacts(): MutableList<Contact> {
        val sharedPreferences = context?.getSharedPreferences("favorites", Context.MODE_PRIVATE) // favorites 파일에 저장된 객체 가져옴
        val json = sharedPreferences?.getString("favorite_contacts", "[]")
        val contactType = object : TypeToken<MutableList<Contact>>() {}.type
        return Gson().fromJson(json, contactType)
    }

    private fun saveFavoriteContacts() {
        val sharedPreferences = context?.getSharedPreferences("favorites", Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        val json = Gson().toJson(favoriteContacts)
        editor?.putString("favorite_contacts", json)
        editor?.apply()
    }

    private fun toggleFavorite(contact: Contact) {
        if (favoriteContacts.contains(contact)) {
            favoriteContacts.remove(contact)
        } else {
            favoriteContacts.add(contact)
        }
        saveFavoriteContacts()
    }

    private fun showAddContactDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_contact, null)
        val nameEditText: EditText = dialogView.findViewById(R.id.edit_text_name)
        val phoneEditText: EditText = dialogView.findViewById(R.id.edit_text_phone)
        val exerciseEditText: EditText = dialogView.findViewById(R.id.edit_text_exercise)
        val saveButton: Button = dialogView.findViewById(R.id.button_save)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        saveButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val phone = phoneEditText.text.toString()
            val exercise = exerciseEditText.text.toString()

            if (name.isNotEmpty() && phone.isNotEmpty()) {
                val newContact = Contact(name, phone, exercise)
                contacts.add(newContact)
                contactsAdapter.notifyItemInserted(contacts.size - 1)
                saveContactsToJson()
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun copyAssetsToInternalStorage(context: Context, fileName: String) { //파일 복사 메서드 추가
        val file = File(context.filesDir, fileName)
        if (!file.exists()) {
            val assetManager = context.assets
            val inputStream: InputStream = assetManager.open(fileName)
            val outputStream: FileOutputStream = FileOutputStream(file)
            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }
            inputStream.close()
            outputStream.close()
        }
    }

    private fun deleteContact(position: Int) {
        contacts.removeAt(position)
        contactsAdapter.notifyItemRemoved(position)
        saveContactsToJson()
    }
}
