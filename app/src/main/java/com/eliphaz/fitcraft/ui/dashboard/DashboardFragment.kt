package com.eliphaz.fitcraft.ui.dashboard

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.eliphaz.fitcraft.databinding.FragmentDashboardBinding
import com.bumptech.glide.Glide
import com.eliphaz.fitcraft.baseclasses.exercicio
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.StorageReference
import com.eliphaz.fitcraft.R



class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    private lateinit var nomeExercicioEditText: EditText
    private lateinit var repeticoesEditText: EditText
    private lateinit var exerciocioImageView: ImageView
    private var imageUri: Uri? = null


    //TODO("Declare aqui as outras variaveis do tipo EditText que foram inseridas no layout")
    private lateinit var registerButton: Button
    private lateinit var selectImageButton: Button
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var auth: FirebaseAuth

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textDashboard
        dashboardViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)
        exerciocioImageView = view.findViewById(R.id.image_exercicio)
        registerButton = view.findViewById(R.id.salvarItemButton)
        selectImageButton = view.findViewById(R.id.button_select_image)
        nomeExercicioEditText = view.findViewById(R.id.nomeExercicioEditText)
        repeticoesEditText = view.findViewById(R.id.repeticoesEditText)
        //TODO("Capture aqui os outro campos que foram inseridos no layout. Por exemplo, ate
        // o momento so foi capturado o endereco (EditText)")

        auth = FirebaseAuth.getInstance()

        try {
            //val storage = FirebaseStorage.getInstance()
            //storageReference = FirebaseStorage.getInstance()
                //.getReferenceFromUrl("gs://apptemplate-35820.appspot.com")
                //.child("exercicios_images")
            //storageReference = FirebaseStorage.getInstance().getReference().child("exercicios_images")
        } catch (e: Exception) {
            Log.e("FirebaseStorage", "Erro ao obter referência para o Firebase Storage", e)
            // Trate o erro conforme necessario, por exemplo:
            Toast.makeText(context, "Erro ao acessar o Firebase Storage", Toast.LENGTH_SHORT).show()
        }

        selectImageButton.setOnClickListener {
            openFileChooser()
        }

        registerButton.setOnClickListener {
            salvarItem()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun salvarItem() {
        //TODO("Capture aqui o conteudo que esta nos outros editTexts que foram criados")
        val nomeexercicio = nomeExercicioEditText.text.toString().trim()
        val repeticoes = repeticoesEditText.text.toString().trim()

        if (nomeexercicio.isEmpty() || repeticoes.isEmpty() || imageUri == null) {
            Toast.makeText(context, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT)
                .show()
            return
        }
        uploadImageToFirestore()
    }


    private fun uploadImageToFirestore() {
        if (imageUri != null) {
            val inputStream = context?.contentResolver?.openInputStream(imageUri!!)
            val bytes = inputStream?.readBytes()
            inputStream?.close()

            if (bytes != null) {
                val base64Image = Base64.encodeToString(bytes, Base64.DEFAULT)
                val nomeexercicio = nomeExercicioEditText.text.toString().trim()
                val repeticoes = repeticoesEditText.text.toString().trim()
                val item = exercicio(nomeexercicio, repeticoes, base64Image)
                //TODO("Capture aqui o conteudo que esta nos outros editTexts que foram criados")



                saveItemIntoDatabase(item)
            }
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK
            && data != null && data.data != null
        ) {
            imageUri = data.data
            Glide.with(this).load(imageUri).into(exerciocioImageView)
        }
    }

    private fun saveItemIntoDatabase(exercicio: exercicio ) {
        //TODO("Altere a raiz que sera criada no seu banco de dados do realtime database.
        // Renomeie a raiz itens")
        databaseReference = FirebaseDatabase.getInstance().getReference("exercicio")

        // Cria uma chave unica para o novo item
        val exercicioID = databaseReference.push().key
        if (exercicioID != null) {
            databaseReference.child(auth.uid.toString()).child(exercicioID).setValue(exercicio)
                .addOnSuccessListener {
                    Toast.makeText(context, "Exercício cadastrado com sucesso!", Toast.LENGTH_SHORT)
                        .show()
                    requireActivity().supportFragmentManager.popBackStack()
                }.addOnFailureListener {
                    Toast.makeText(context, "Falha ao cadastrar o exercício", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "Erro ao gerar o ID do exercício", Toast.LENGTH_SHORT).show()
        }
    }
}