package com.puc.projectgrafo

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