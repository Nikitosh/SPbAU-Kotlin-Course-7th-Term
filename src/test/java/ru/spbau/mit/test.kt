package ru.spbau.mit
import org.junit.Test
import java.io.FileInputStream
import kotlin.test.assertEquals

class TestGraph {
    @Test
    fun testRead() {
        testEquals(createSampleGraph1(), Graph.read(FileInputStream("src/test/resources/sample1.txt")))
        testEquals(createSampleGraph2(), Graph.read(FileInputStream("src/test/resources/sample2.txt")))
    }

    @Test
    fun testFindCycle() {
        assertEquals(4, createSampleGraph1().findCycle().size)
        assertEquals(3, createSampleGraph2().findCycle().size)
    }

    @Test
    fun testSolve() {
        assertEquals(listOf(0, 0, 0, 0), solve(createSampleGraph1()))
        assertEquals(listOf(0, 0, 0, 1, 1, 2), solve(createSampleGraph2()))
    }

    private fun createSampleGraph1(): Graph {
        val vertices = List(4, { _ -> Vertex()})
        val graph = Graph(vertices)
        graph.addUndirectedEdge(vertices[0], vertices[2])
        graph.addUndirectedEdge(vertices[3], vertices[2])
        graph.addUndirectedEdge(vertices[3], vertices[1])
        graph.addUndirectedEdge(vertices[0], vertices[1])
        return graph
    }

    private fun createSampleGraph2(): Graph {
        val vertices = List(6, { _ -> Vertex()})
        val graph = Graph(vertices)
        graph.addUndirectedEdge(vertices[0], vertices[1])
        graph.addUndirectedEdge(vertices[2], vertices[3])
        graph.addUndirectedEdge(vertices[5], vertices[3])
        graph.addUndirectedEdge(vertices[1], vertices[2])
        graph.addUndirectedEdge(vertices[0], vertices[2])
        graph.addUndirectedEdge(vertices[2], vertices[4])
        return graph
    }

    private fun testEquals(graph1: Graph, graph2: Graph) {
        assertEquals(graph1.vertices.size, graph2.vertices.size)
        for (i in 0 until graph1.vertices.size) {
            val neighbours1 = graph1.vertices[i].neighbours
            val neighbours2 = graph2.vertices[i].neighbours
            assertEquals(neighbours1.size, neighbours2.size)
            for (j in 0 until neighbours1.size) {
                assertEquals(graph1.vertices.indexOf(neighbours1[j]), graph2.vertices.indexOf(neighbours2[j]))
            }
        }
    }
}
