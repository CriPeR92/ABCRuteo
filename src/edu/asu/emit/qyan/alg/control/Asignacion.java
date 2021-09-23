package edu.asu.emit.qyan.alg.control;

/**
 * En caso de querer utilizar MidFit comentar la segunda funcion "marcarSlotUtilizados, en caso de utilizar FirstFit comentar la primera
 */

public class Asignacion {

    GrafoMatriz g;
    resultadoSlot resultado;
    int p;
    int m;

    Asignacion(GrafoMatriz g, resultadoSlot resultado) {
        this.g = g;
        this.resultado = resultado;
    }

    /**
     * Funcion para asignar los FS utilizando MF
     *
     * @param id
     */
    public void marcarSlotUtilizados(Integer id) {

        String camino = resultado.camino.toString();
        String nuevo_camino;
        int p1;
        //vamos a guardar el camino formateado
        p1 = camino.indexOf("]");
        nuevo_camino = camino;
        nuevo_camino = nuevo_camino.replaceAll("\\s", "");

        int mitad = lugarInicialAsignacion(resultado);
        String[] caminosLista;
        caminosLista = resultado.camino.split(",");

        for (int i = 0; i < caminosLista.length - 1; i++) {

            int k = Integer.parseInt(caminosLista[i]);
            int l = Integer.parseInt(caminosLista[i + 1]);

            int n1 = g.posicionNodo(k);
            int n2 = g.posicionNodo(l);

            p = n1;
            m = n2;

            int mitadderecha = mitad;
            int mitadizquierda = mitad;
            int contador = 0;
            for (int x = 0; x < resultado.cantidadfs; x++) {

                if (x == 0) {
                    g.grafo[n1][n2].listafs[mitad].libreOcupado = 1;
                    g.grafo[n1][n2].listafs[mitad].tiempo = g.grafo[n1][n2].tiempo;
                    g.grafo[n2][n1].listafs[mitad].libreOcupado = 1;
                    g.grafo[n2][n1].listafs[mitad].tiempo = g.grafo[n2][n1].tiempo;
                    g.grafo[n1][n2].listafs[mitad].id = id;
                    g.grafo[n2][n1].listafs[mitad].id = id;

                    //concatenar y guardar el id de la conexion
                    g.grafo[n1][n2].enlace.add(id.toString() + "," + nuevo_camino);
                    g.grafo[n1][n2].ids.add(id);
                    g.grafo[n2][n1].enlace.add(id.toString() + "," + nuevo_camino);
                    g.grafo[n2][n1].ids.add(id);
                } else if (x != 0 && (x % 2) == 0) {
                    mitadizquierda--;
                    g.grafo[n1][n2].listafs[mitadizquierda].libreOcupado = 1;
                    g.grafo[n1][n2].listafs[mitadizquierda].tiempo = g.grafo[n1][n2].tiempo;
                    g.grafo[n2][n1].listafs[mitadizquierda].libreOcupado = 1;
                    g.grafo[n2][n1].listafs[mitadizquierda].tiempo = g.grafo[n2][n1].tiempo;
                    g.grafo[n1][n2].listafs[mitadizquierda].id = id;
                    g.grafo[n2][n1].listafs[mitadizquierda].id = id;
                } else if (x != 0 && (x % 2) != 0) {
                    mitadderecha++;
                    g.grafo[n1][n2].listafs[mitadderecha].libreOcupado = 1;
                    g.grafo[n1][n2].listafs[mitadderecha].tiempo = g.grafo[n1][n2].tiempo;
                    g.grafo[n2][n1].listafs[mitadderecha].libreOcupado = 1;
                    g.grafo[n2][n1].listafs[mitadderecha].tiempo = g.grafo[n2][n1].tiempo;
                    g.grafo[n1][n2].listafs[mitadderecha].id = id;
                    g.grafo[n2][n1].listafs[mitadderecha].id = id;
                }
            }
        }


    }

    /**
     * Funcion para asignar los FS utilizando FF
     *
     * @param id
     */
//    public void marcarSlotUtilizados(int id) {
//
//        int mitad = lugarInicialAsignacion(resultado);
//        String[] caminosLista;
//        caminosLista = resultado.camino.split(",");
//
//        for (int i = 0; i < caminosLista.length - 1; i++) {
//
//            int k = Integer.parseInt(caminosLista[i]);
//            int l = Integer.parseInt(caminosLista[i + 1]);
//
//            int n1 = g.posicionNodo(k);
//            int n2 = g.posicionNodo(l);
//
//            p = n1;
//            m = n2;
//
//            int mitadderecha = mitad;
//            int mitadizquierda = mitad;
//            for (int x = 0; x < resultado.cantidadfs; x++) {
//
//                if (x == 0) {
//                    g.grafo[n1][n2].listafs[mitad].libreOcupado = 1;
//                    g.grafo[n2][n1].listafs[mitad].libreOcupado = 1;
//                    g.grafo[n1][n2].listafs[mitad].id = id;
//                    g.grafo[n2][n1].listafs[mitad].id = id;
//                } else if (x != 0 && (x % 2) == 0) {
//                    mitadizquierda--;
//                    g.grafo[n1][n2].listafs[mitadizquierda].libreOcupado = 1;
//                    g.grafo[n2][n1].listafs[mitadizquierda].libreOcupado = 1;
//                    g.grafo[n1][n2].listafs[mitadizquierda].id = id;
//                    g.grafo[n2][n1].listafs[mitadizquierda].id = id;
//                } else if (x != 0 && (x % 2) != 0) {
//                    mitadderecha++;
//                    g.grafo[n1][n2].listafs[mitadderecha].libreOcupado = 1;
//                    g.grafo[n2][n1].listafs[mitadderecha].libreOcupado = 1;
//                    g.grafo[n1][n2].listafs[mitadderecha].id = id;
//                    g.grafo[n2][n1].listafs[mitadderecha].id = id;
//                }
//            }
//        }
//    }
    public int lugarInicialAsignacion(resultadoSlot resultado) {

        int indiceInicio = (resultado.indice - resultado.contador) + 1;
        return (indiceInicio + resultado.indice) / 2;
    }


}
