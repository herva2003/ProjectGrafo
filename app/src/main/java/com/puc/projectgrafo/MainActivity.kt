package com.puc.projectgrafo

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.puc.projectgrafo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var resultTextView: TextView
    private lateinit var binding: ActivityMainBinding

    private lateinit var editText: EditText
    private lateinit var editText2: EditText

    private var origem: Int = 0
    private var destino: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        resultTextView = binding.resultTextView
        editText = binding.impOrigem
        editText2 = binding.impDestino

        val db = FirebaseFirestore.getInstance()
        val prediosCollectionRef = db.collection("predios")

        prediosCollectionRef.get()
            .addOnSuccessListener { querySnapshot ->
                var quantidade: Int? = null

                for (documentSnapshot in querySnapshot) {
                    quantidade = documentSnapshot.getLong("quantidade")?.toInt()
                }

            binding.calculateButton.setOnClickListener {
                val origemText = editText.text.toString()
                val destinoText = editText2.text.toString()

                if (origemText.isNotEmpty() && destinoText.isNotEmpty()) {
                    val origem = origemText.toIntOrNull()
                    val destino = destinoText.toIntOrNull()

                    if (origem != null && destino != null) {
                        if (origem < quantidade!! && destino < quantidade) {
                            calcularMenorCaminho()
                        } else {
                            resultTextView.text = "O predio digitado não existe"
                        }
                    } else {
                        resultTextView.text = "Insira apenas valores numéricos para origem e destino."
                    }
                } else {
                    resultTextView.text = "Por favor, preencha os campos de origem e destino."
                }
            }
        }
    }

    private fun calcularMenorCaminho() {
        val origemText = editText.text.toString()
        val destinoText = editText2.text.toString()

        if (origemText.isNotEmpty() && destinoText.isNotEmpty()) {
            origem = origemText.toInt()
            destino = destinoText.toInt()

            val db = FirebaseFirestore.getInstance()
            val graphCollectionRef = db.collection("graphs")
            val prediosCollectionRef = db.collection("predios")

            prediosCollectionRef.get()
                .addOnSuccessListener { querySnapshot ->
                    var quantidade: Int? = null

                    for (documentSnapshot in querySnapshot) {
                        quantidade = documentSnapshot.getLong("quantidade")?.toInt()
                        if (quantidade != null) {
                            Log.d("MainActivity", "Quantidade de prédios: $quantidade")
                            break
                        }
                    }

                    if (quantidade != null) {
                        val graph = Graph(quantidade)

                        graphCollectionRef.get()
                            .addOnSuccessListener { querySnapshot2 ->
                                for (documentSnapshot in querySnapshot2) {
                                    val origem = documentSnapshot.getLong("origem")?.toInt()
                                    val destino = documentSnapshot.getLong("destino")?.toInt()
                                    val tempo = documentSnapshot.getLong("tempo")?.toInt()

                                    if (origem != null && destino != null && tempo != null) {
                                        if (origem < quantidade && destino < quantidade) {
                                            graph.addEdge(origem, destino, tempo)
                                            Log.d(
                                                "MainActivity",
                                                "Valores adicionados: source=$origem, destination=$destino, weight=$tempo"
                                            )
                                        } else {
                                            Log.d(
                                                "MainActivity",
                                                "Valores inválidos: source=$origem, destination=$destino, weight=$tempo"
                                            )
                                        }
                                    } else {
                                        Log.d(
                                            "MainActivity",
                                            "Valores inválidos: source=$origem, destination=$destino, weight=$tempo"
                                        )
                                    }
                                }

                                val result = graph.dijkstra(origem, destino)

                                resultTextView.text = ""
                                if (result.distance != Int.MAX_VALUE) {
                                    val pathString = result.path.joinToString(" -> ")
                                    val distanceString = result.distance.toString()
                                    val resultText =
                                        "Menor caminho: $pathString\nTempo: $distanceString minutos"
                                    resultTextView.text = resultText
                                } else {
                                    resultTextView.text = "Não foi possível encontrar um caminho."
                                }
                            }
                    }
                }
        }
    }
}