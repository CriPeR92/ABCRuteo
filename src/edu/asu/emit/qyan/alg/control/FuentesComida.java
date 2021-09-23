package edu.asu.emit.qyan.alg.control;

import java.util.ArrayList;


/**
 * Clase para representar a la solucion, las variables se explican en el mismo orden
 * -Grafo
 * -Caminos utilizados
 * -id's de las solicitudes atendidas
 * -Numero de camino utilizados para cada solicitud
 * -Indice mayor de fs utilizado
 * -Si una solucion se ha modificado o no en una iteracion del algoritmo
 * -Contador de SemiBloqueos
 */
public class FuentesComida {

    public GrafoMatriz grafo;
    public ArrayList<String> caminos;
    public ArrayList<Integer> ids;
    public ArrayList<Integer> caminoUtilizado;
    public int fsUtilizados;
    public ArrayList<Integer> modificado;
    public int semiBloqueados;

    public FuentesComida(GrafoMatriz g) {
        this.grafo = g;
        this.caminos = new ArrayList<>();
        this.ids = new ArrayList<>();
        this.caminoUtilizado = new ArrayList<>();
        this.fsUtilizados = 0;
        this.modificado = new ArrayList<>();
        this.semiBloqueados = 0;
    }

}
