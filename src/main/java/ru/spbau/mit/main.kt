package ru.spbau.mit

import java.io.InputStream
import java.util.*

class Vertex {
    val neighbours: MutableList<Vertex> = mutableListOf()

    fun addNeighbour(vertex: Vertex) {
        neighbours.add(vertex)
    }
}

class Graph(internal val vertices: List<Vertex>) {
    fun solve(): List<Int> {
        val cycleVertices = findCycle()
        return vertices.map({ vertex -> getDistanceToCycle(vertex, vertex, cycleVertices)!! }).toList()
    }

    companion object {
        fun read(inputStream: InputStream): Graph {
            val input = Scanner(inputStream)
            val vertexNumber = input.nextInt()
            val vertices = List(vertexNumber, { _ -> Vertex() })
            val graph = Graph(vertices)
            for (i in 1..vertexNumber) {
                val vertexV = input.nextInt()
                val vertexU = input.nextInt()
                graph.addUndirectedEdge(vertices[vertexV - 1], vertices[vertexU - 1])
            }
            return graph
        }
    }

    internal fun addUndirectedEdge(v: Vertex, u: Vertex) {
        v.addNeighbour(u)
        u.addNeighbour(v)
    }

    internal fun findCycle(): Set<Vertex> {
        return findCycle(vertices[0], vertices[0], HashSet(), mutableListOf())!!
    }

    private fun findCycle(vertex: Vertex, previousVertex: Vertex, visitedVertices: MutableSet<Vertex>,
                          path: MutableList<Vertex>): Set<Vertex>? {
        visitedVertices.add(vertex)
        path.add(vertex)
        vertex.neighbours.forEach({ neighbour ->
            if (visitedVertices.contains(neighbour) && neighbour != previousVertex) {
                return path.drop(path.indexOf(neighbour)).toSet()
            }
            if (!visitedVertices.contains(neighbour)) {
                val cycle = findCycle(neighbour, vertex, visitedVertices, path)
                if (cycle != null) {
                    return cycle
                }
            }
        })
        path.removeAt(path.size - 1)
        return null
    }

    private fun getDistanceToCycle(vertex: Vertex, previousVertex: Vertex, cycleVertices: Set<Vertex>): Int? {
        if (cycleVertices.contains(vertex)) {
            return 0
        }
        vertex.neighbours.forEach({ neighbour ->
            if (neighbour != previousVertex) {
                val distance = getDistanceToCycle(neighbour, vertex, cycleVertices)
                if (distance != null) {
                    return distance + 1
                }
            }
        })
        return null
    }
}

fun main(args: Array<String>) {
    val graph = Graph.read(System.`in`)
    println(graph.solve().joinToString(separator = " "))
}
