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

        binding.calculateButton.setOnClickListener {
            calculateShortestPaths()
        }
    }

    private fun calculateShortestPaths() {
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
                                }

                                val result = graph.dijkstra(origem, destino)

                                resultTextView.text = ""
                                if (result.path.isNotEmpty()) {
                                    val pathString = result.path.joinToString(" -> ")
                                    val distanceString = result.distance.toString()
                                    val resultText =
                                        "Menor caminho: $pathString\nTempo: $distanceString minutos"
                                    resultTextView.text = resultText
                                } else {
                                    resultTextView.text = "Não foi possível encontrar um caminho válido."
                                }
                            }
                    }
                }
        }
    }

    class Graph(private val numVertices: Int) {
        private val matrizAdjacente: Array<IntArray> = Array(numVertices) { IntArray(numVertices) }

        fun addEdge(origem: Int, destino: Int, tempo: Int) {
            matrizAdjacente[origem][destino] = tempo
            matrizAdjacente[destino][origem] = tempo
        }

        fun dijkstra(origem: Int, destino: Int): DijkstraResult {
            val tempo = IntArray(numVertices) { Int.MAX_VALUE }
            val visitado = BooleanArray(numVertices)
            val predecessor = IntArray(numVertices) { -1 }

            tempo[origem] = 0

            for (count in 0 until numVertices - 1) {
                val currentVertex = getMinimumDistanceVertex(tempo, visitado)
                visitado[currentVertex] = true

                for (vertex in 0 until numVertices) {
                    if (!visitado[vertex] && matrizAdjacente[currentVertex][vertex] != 0 && tempo[currentVertex] != Int.MAX_VALUE) {
                        val novaDistancia = tempo[currentVertex] + matrizAdjacente[currentVertex][vertex]
                        if (novaDistancia < tempo[vertex]) {
                            tempo[vertex] = novaDistancia
                            predecessor[vertex] = currentVertex
                        }
                    }
                }
            }

            val path = getPath(destino, predecessor)

            return DijkstraResult(tempo[destino], path)
        }

        private fun getMinimumDistanceVertex(distancia: IntArray, visitado: BooleanArray): Int {
            var minDistancia = Int.MAX_VALUE
            var minVertice = -1

            for (vertice in 0 until numVertices) {
                if (!visitado[vertice] && distancia[vertice] <= minDistancia) {
                    minDistancia = distancia[vertice]
                    minVertice = vertice
                }
            }
            return minVertice
        }

        private fun getPath(destino: Int, predecessor: IntArray): List<Int> {
            val path = mutableListOf<Int>()
            var currentVertex = destino

            while (currentVertex != -1) {
                path.add(0, currentVertex)
                currentVertex = predecessor[currentVertex]
            }

            return path
        }
    }

    data class DijkstraResult(val distance: Int, val path: List<Int>)
}
