package edu.asu.emit.qyan.alg.control;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.asu.emit.qyan.alg.model.Path;
import edu.asu.emit.qyan.alg.model.VariableGraph;

public class Aplicacion {

    // Fuentes de comida
    public static ArrayList<FuentesComida> fuentes = new ArrayList<>();
    // Archivo que representa al grafo
    public static VariableGraph graph = new VariableGraph("data/test_25");
    // PI - Lista que contiene el nectar de cada solucion
    public static ArrayList<Float> pi = new ArrayList<>();
    // Lista de caminos pre-calculados
    public static ArrayList<String[]> caminos = new ArrayList<>();
    // Numero de abejas o soluciones candidatas
    public static int abejas = 5;
    // ID para las solicitudes entrantes
    public static int id = 1;


    public static void main(String[] args) throws InterruptedException, IOException {


        // Se crean las solicitudes de manera aleatoria
        for (int h = 1; h <= 100; h++) {
            crearArchivosSolicitudes(h);
        }


        // Se crea el archivo con todos los caminos Pre-calculados
        // Una vez creado se puede comentar la siguiente linea
        crearArchivoCaminos();

        // Se guardan todos los caminos en memoria
        leerArchivoCaminos();

        // Se crean las soluciones candidatas
        crearFuenteDeComida(abejas);

        /**
         * Ciclo que representa la cantidad de archivos que seran el conjunto de
         * solicitudes entrantes (ahora mismo tenemos 100 arhcivos de 50 solicitudes
         * cada uno)
         */
        for (int l = 1; l <= 100; l++) {

            // Se cargan y se asignan las solicitudes utilizando FAR y (FF ó MF)
            cargarSolicitudes(abejas, l);

            /**
             * Ciclo que representa al Algoritmo ABC con sus 3 pasos
             * El numero de iteraciones se representa en el numero del for
             */
            for (int i = 0; i < 50; i++) {
                primerPaso(abejas);
                segundoPaso(abejas);
                tercerPaso(abejas);
            }

            // Funcion para restar el tiempo de vida de las solicitudes
            // En caso de ser cero, se borra la solicitud del grafo
            for (int p = 0; p < abejas; p++) {
                fuentes.get(p).grafo.restar();
            }

            // Se elige la mejor solucion luego de cada ciclo completo
            elegirConexion();
        }
    }

    /**
     * Funcion para leer el archivo y guardar en memoria
     *
     * @throws IOException
     */
    private static void leerArchivoCaminos() throws IOException {
        FileReader input = new FileReader("data/Kcaminos");
        BufferedReader bufRead = new BufferedReader(input);
        String linea = bufRead.readLine();

        while (linea != null) {
            String[] variables = linea.split("-");
            variables[2] = variables[2].replace(", [", ";[");
            variables[2] = variables[2].replace("[", "");
            variables[2] = variables[2].replace("]", "");
            variables[2] = variables[2].replace(", ", ",");
            caminos.add(variables);
            linea = bufRead.readLine();
        }
    }

    /**
     * Funcion para cargar las solicitudes al grafo
     *
     * @param cantFuente Numero de soluciones candidatas
     * @param solicitud  Numero del archivo a leer
     * @throws IOException
     */
    private static void cargarSolicitudes(int cantFuente, int solicitud) throws IOException {

        String numero = "";
        numero = Integer.toString(solicitud);
        FileReader input = new FileReader("data/solicitudes" + numero);
        BufferedReader bufRead = new BufferedReader(input);
        String linea = bufRead.readLine();

        // Se lee cada linea del archivo que sera una solicitud por linea
        while (linea != null) {

            if (linea.trim().equals("")) {
                linea = bufRead.readLine();
                continue;
            }
            String[] str_list = linea.trim().split("\\s*,\\s*");

            // Sacamos los datos de la linea, origen-destino-CantidadDeFs-tiempo-id
            int origen = Integer.parseInt(str_list[0]);
            int destino = Integer.parseInt(str_list[1]);
            int fs = Integer.parseInt(str_list[2]);
            int tiempo = Integer.parseInt(str_list[3]);
            int id = Integer.parseInt(str_list[4]);

            String listaCaminos = "";

            // Se carga en memoria todos los caminos entre los nodos origen y destino
            for (String[] camino : caminos) {
                if (camino[0].equals(str_list[0]) && camino[1].equals(str_list[1])) {
                    listaCaminos = camino[2];
                    break;
                }
            }

            for (int j = 0; j < cantFuente; j++) {

                /**
                 * (IF) donde se verifica si la solicitud entrante ya existe en la red
                 * En caso de que exista se realiza el cambio ya sea reducir FS o aumentar
                 * En el caso de que sea aumentar pero no se tienen los espacios necesarios se intenta hacer un reasignamiento
                 * Y si aun asi no se encuentra lugar se cuenta como un semi-bloqueo
                 * En caso de que la reasignacion sea correcta, se borra la conexion en su lugar anterior
                 */
                if (fuentes.get(j).ids.contains(id)) {

                    // Se intenta aumentar o disminuir los FS de la solicitud
                    boolean reasignar = fuentes.get(j).grafo.verificar_conexion(origen, id, fs);

                    // En caso de no poder aumentar los FS, se busca el espacio necesario en el grafo
                    if (!reasignar) {
                        BuscarSlot r = new BuscarSlot(fuentes.get(j).grafo, listaCaminos);
                        resultadoSlot res = r.concatenarCaminos(fs, 0, 0);
                        // Si se encuentra el espacio, se elimina del lugar anterior de la solicitud
                        if (res != null) {
                            int i, k, f;
                            for (i = 0; i < fuentes.get(j).grafo.grafo.length; i++) {
                                for (f = 0; f < fuentes.get(j).grafo.grafo.length; f++) {
                                    for (k = 0; k < fuentes.get(j).grafo.grafo[i][j].listafs.length; k++) {
                                        if (fuentes.get(j).grafo.grafo[i][f].listafs[k].id == id) {
                                            fuentes.get(j).grafo.grafo[i][f].listafs[k].id = 0;
                                            fuentes.get(j).grafo.grafo[i][f].listafs[k].tiempo = 0;
                                            fuentes.get(j).grafo.grafo[i][f].listafs[k].libreOcupado = 0;
                                        }
                                    }
                                }
                            }
                            Asignacion asignar = new Asignacion(fuentes.get(j).grafo, res);
                            asignar.marcarSlotUtilizados(id);
                        } else {
                            // Si es que se bloqueo y no encontro un camino se guardara los datos de la conexion y la palabra bloqueado
                            fuentes.get(j).semiBloqueados = fuentes.get(j).semiBloqueados + 1;
                        }
                    }
                    // Aqui es cuando entra una solicitud nueva en la red
                } else {
                    BuscarSlot r = new BuscarSlot(fuentes.get(j).grafo, listaCaminos);
                    resultadoSlot res = r.concatenarCaminos(fs, 0, 0);
                    if (res != null) {
                        //guardar caminos utilizados y el numero de camino utilizado
                        fuentes.get(j).caminoUtilizado.add(res.caminoUtilizado);
                        fuentes.get(j).caminos.add(res.camino);
                        fuentes.get(j).ids.add(id);
                        fuentes.get(j).modificado.add(0);
                        Asignacion asignar = new Asignacion(fuentes.get(j).grafo, res);
                        asignar.marcarSlotUtilizados(id);
                    } else {
                        //Si es que se bloqueo y no encontro un camino se guardara los datos de la conexion y la palabra bloqueado
                        fuentes.get(j).caminoUtilizado.add(99);
                        fuentes.get(j).caminos.add("Bloqueado:" + str_list[0] + ":" + str_list[1] + ":" + str_list[2]);
                        fuentes.get(j).ids.add(id);
                        fuentes.get(j).modificado.add(0);
                    }
                }
            }
            linea = bufRead.readLine();
        }
        bufRead.close();
    }

    /**
     * Funcion para crear el archivo de los posibles caminos
     *
     * @throws IOException
     */
    private static void crearArchivoCaminos() throws IOException {
        YenTopKShortestPathsAlg yenAlg = new YenTopKShortestPathsAlg(graph);
        PrintWriter writer = new PrintWriter("data/Kcaminos", "UTF-8");

        // en este for hay que poner la cantidad de vertices que tenemos
        for (int i = 0; i <= 15; i++) {
            for (int k = 0; k <= 15; k++) {
                if (i != k) {
                    List<Path> shortest_paths_list = yenAlg.get_shortest_paths(graph.get_vertex(i), graph.get_vertex(k), 5);
                    List<Path> shortest_paths_list2 = yenAlg.get_shortest_paths(graph.get_vertex(k), graph.get_vertex(i), 5);
                    writer.println(i + "-" + k + "-" + shortest_paths_list.toString());
                    writer.println(k + "-" + i + "-" + shortest_paths_list2.toString());
                }
            }
        }
        writer.close();
    }

    /**
     * Funcion para crear una fuente de comida o solucion candidata
     */
    public static void crearFuenteDeComida(int cantFuente) throws IOException {

        // Crear matriz inicial para todas las fuentes de comida
        // Matriz que representa la red igual al archivo test_16 que se va a utilar al tener los caminos.
        for (int i = 0; i < cantFuente; i++) {

            //Formato para crear grafos con sus vertices
            int[] vertices = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
            GrafoMatriz g = new GrafoMatriz(vertices);
            g.InicializarGrafo(g.grafo);

            g.agregarRuta(1, 6, 1, 3, 200);
            g.agregarRuta(1, 7, 1, 3, 200);
            g.agregarRuta(1, 8, 1, 3, 200);
            g.agregarRuta(2, 3, 1, 3, 200);
            g.agregarRuta(2, 5, 1, 3, 200);
            g.agregarRuta(2, 6, 1, 3, 200);
            g.agregarRuta(3, 5, 1, 3, 200);
            g.agregarRuta(3, 8, 1, 3, 200);
            g.agregarRuta(4, 5, 1, 3, 200);
            g.agregarRuta(4, 6, 1, 3, 200);
            g.agregarRuta(6, 13, 1, 3, 200);
            g.agregarRuta(7, 10, 1, 3, 200);
            g.agregarRuta(7, 14, 1, 3, 200);
            g.agregarRuta(8, 9, 1, 3, 200);
            g.agregarRuta(8, 15, 1, 3, 200);
            g.agregarRuta(9, 10, 1, 3, 200);
            g.agregarRuta(9, 12, 1, 3, 200);
            g.agregarRuta(9, 15, 1, 3, 200);
            g.agregarRuta(10, 12, 1, 3, 200);
            g.agregarRuta(11, 13, 1, 3, 200);
            g.agregarRuta(11, 14, 1, 3, 200);
            g.agregarRuta(13, 14, 1, 3, 200);

            fuentes.add(new FuentesComida(g));
        }
    }

    /**
     * Funcion para calcular los FS de todas las fuentes de comida (Indice mayor)
     *
     * @param fuentesComida cantidad de fuentes de comida o soluciones candidatas
     */
    public static void calcularFS(int fuentesComida) {

        int indiceMayor = 0;

        // for para recorrer todas las fuentes de comida
        for (int i = 0; i < fuentesComida; i++) {

            // for para recorrer las filas de un grafo
            for (int k = 0; k < fuentes.get(i).grafo.grafo.length; k++) {
                // for para recorrer las columnas de un grafo
                for (int j = 0; j < fuentes.get(i).grafo.grafo.length; j++) {
                    // for para recorrer el array de listafs (cada enlace del grafo)
                    for (int p = 0; p < fuentes.get(i).grafo.grafo[k][j].listafs.length; p++) {
                        if (fuentes.get(i).grafo.grafo[k][j].listafs[p].libreOcupado == 1) {
                            if (indiceMayor < p) {
                                indiceMayor = p;
                            }
                        }
                    }
                }
            }
            fuentes.get(i).fsUtilizados = indiceMayor;
            indiceMayor = 0;
        }
    }

    /**
     * Funcion para calcular los FS para una sola fuente de comida
     *
     * @param nroGrafo numero de la fuente de comida que sera evaluada
     */
    public static int calcularFsUno(int nroGrafo) {
        int indiceMayor = 0;
        // for para recorrer las filas de un grafo
        for (int k = 0; k < fuentes.get(nroGrafo).grafo.grafo.length; k++) {
            // for para recorrer las columnas de un grafo
            for (int j = 0; j < fuentes.get(nroGrafo).grafo.grafo.length; j++) {
                // for para recorrer el array de listafs (cada enlace del grafo)
                for (int p = 0; p < fuentes.get(nroGrafo).grafo.grafo[k][j].listafs.length; p++) {
                    if (fuentes.get(nroGrafo).grafo.grafo[k][j].listafs[p].libreOcupado == 1) {
                        if (indiceMayor < p) {
                            indiceMayor = p;
                        }
                    }
                }
            }
        }
        return indiceMayor;
    }

    /**
     * En el primer paso vamos a utilizar a las abejas empleadas para cambiar soluciones de las fuentes de comida si es que tienen
     * mejor resultado
     **/
    public static void primerPaso(int cantFuentes) {
        calcularFS(cantFuentes);

        //calcular Vij para cada fuente de comida
        for (int i = 0; i < cantFuentes; i++) {
            Random rand = new Random();
            double alpha = (double) (Math.random() * 2 - 1);
            int j = rand.nextInt(fuentes.get(i).caminos.size() - 1);
            int k = rand.nextInt(cantFuentes - 1);

            while (j == k) {
                k = rand.nextInt(cantFuentes - 1);
            }

            double nroCaminoAserUtilizado = (fuentes.get(i).caminoUtilizado.get(j)) + alpha * ((fuentes.get(i).caminoUtilizado.get(j)) - (fuentes.get(k).caminoUtilizado.get(j)));
            int caminoAUsar = (int) nroCaminoAserUtilizado;
            borrarConexion(j, i, caminoAUsar);
        }

    }


    /**
     * Funcion para eliminar una conexion actual para volver a buscar un lugar para la misma
     *
     * @param nroCamino      numero de camino utilizado
     * @param nroGrafo       numero de fuente de comida a modificar
     * @param nroCaminoAUsar numero de camino a probar y utilizar si es que es mejor o no a (nroCamino)
     */
    public static void borrarConexion(int nroCamino, int nroGrafo, int nroCaminoAUsar) {

        String camino = String.valueOf(fuentes.get(nroGrafo).caminos.get(nroCamino));
        boolean reasignarSioSi = false;

        int inicioSolicitud = 0;
        int finSolicitud = 0;
        int inicio = 0;
        int longitud = 0;

        if (!camino.contains("Bloqueado")) {

            String[] caminosLista;
            caminosLista = camino.split(",");

            inicioSolicitud = Integer.parseInt(caminosLista[0]);
            finSolicitud = Integer.parseInt(caminosLista[caminosLista.length - 1]);


            boolean bandera = true;

            for (int p = 0; p < caminosLista.length - 1; p++) {

                int primer = Integer.parseInt(caminosLista[p]);
                int segundo = Integer.parseInt(caminosLista[p + 1]);

                for (int k = 0; k < fuentes.get(nroGrafo).grafo.grafo[0][0].listafs.length; k++) {
                    if (fuentes.get(nroGrafo).grafo.grafo[primer][segundo].listafs[k].id == fuentes.get(nroGrafo).ids.get(nroCamino)) {
                        if (p == 0) {
                            if (bandera) {
                                inicio = k;
                                bandera = false;
                            }
                            longitud = longitud + 1;
                        }
                        fuentes.get(nroGrafo).grafo.grafo[primer][segundo].listafs[k].id = 0;
                        fuentes.get(nroGrafo).grafo.grafo[primer][segundo].listafs[k].libreOcupado = 0;
                        fuentes.get(nroGrafo).grafo.grafo[segundo][primer].listafs[k].id = 0;
                        fuentes.get(nroGrafo).grafo.grafo[segundo][primer].listafs[k].libreOcupado = 0;
                    }
                }
            }
        } else {
            String[] lista = camino.split(":");
            inicioSolicitud = Integer.parseInt(lista[1]);
            finSolicitud = Integer.parseInt(lista[2]);
            longitud = Integer.parseInt(lista[3]);
            reasignarSioSi = true;
        }


        Boolean reasignar = asginar(inicioSolicitud, finSolicitud, nroGrafo, longitud, fuentes.get(nroGrafo).ids.get(nroCamino), nroCaminoAUsar, reasignarSioSi);

        if (reasignar) {
            Integer val = fuentes.get(nroGrafo).modificado.get(nroCamino);
            fuentes.get(nroGrafo).modificado.set(nroCamino, val + 1);
            fuentes.get(nroGrafo).caminos.remove(fuentes.get(nroGrafo).caminos.size() - 1);
            fuentes.get(nroGrafo).ids.remove(fuentes.get(nroGrafo).ids.size() - 1);
            fuentes.get(nroGrafo).caminoUtilizado.remove(fuentes.get(nroGrafo).ids.size() - 1);
            fuentes.get(nroGrafo).modificado.remove(fuentes.get(nroGrafo).ids.size() - 1);
            // volver a como estaba
            String[] caminosLista;
            caminosLista = camino.split(",");
            for (int p = 0; p < caminosLista.length - 1; p++) {

                int primer = Integer.parseInt(caminosLista[p]);
                int segundo = Integer.parseInt(caminosLista[p + 1]);

                for (int k = 0; k < fuentes.get(nroGrafo).grafo.grafo[0][0].listafs.length; k++) {
                    if (k == inicio) {
                        for (int j = 0; j < longitud; j++) {
                            fuentes.get(nroGrafo).grafo.grafo[primer][segundo].listafs[k].id = fuentes.get(nroGrafo).ids.get(nroCamino);
                            fuentes.get(nroGrafo).grafo.grafo[primer][segundo].listafs[k].libreOcupado = 1;
                            fuentes.get(nroGrafo).grafo.grafo[segundo][primer].listafs[k].id = fuentes.get(nroGrafo).ids.get(nroCamino);
                            fuentes.get(nroGrafo).grafo.grafo[segundo][primer].listafs[k].libreOcupado = 1;
                        }
                    }
                }
            }
        } else {
            fuentes.get(nroGrafo).caminos.remove(nroCamino);
            fuentes.get(nroGrafo).ids.remove(nroCamino);
            fuentes.get(nroGrafo).fsUtilizados = calcularFsUno(nroGrafo);
            fuentes.get(nroGrafo).modificado.remove(nroCamino);
            fuentes.get(nroGrafo).caminoUtilizado.remove(nroCamino);
        }
    }

    /**
     * Funcion para asignar una conexion nueva
     *
     * @param inicio   nodo inicio
     * @param fin      nodo destino
     * @param nroGrafo numero de solucion candidata a modificar
     * @param cantFs   cantidad de fs que solicita la conexion
     * @param id       id de la solicitud entrante
     */
    public static Boolean asginar(int inicio, int fin, int nroGrafo, int cantFs, int id, int caminoAUsar, Boolean reasignar) {

        String listaCaminos = "";
        String inicioSolicitud = String.valueOf(inicio);
        String finSolicitud = String.valueOf(fin);

        for (String[] camino : caminos) {
            if (camino[0].equals(inicioSolicitud) && camino[1].equals(finSolicitud)) {
                listaCaminos = camino[2];
                break;
            }
        }

        BuscarSlot r = new BuscarSlot(fuentes.get(nroGrafo).grafo, listaCaminos);
        resultadoSlot res = r.concatenarCaminos(cantFs, 3, caminoAUsar);

        if (res != null) {
            //Guardar caminos utilizados
            fuentes.get(nroGrafo).caminoUtilizado.add(res.caminoUtilizado);
            fuentes.get(nroGrafo).caminos.add(res.camino);
            fuentes.get(nroGrafo).ids.add(id);
            fuentes.get(nroGrafo).modificado.add(0);
            Asignacion asignar = new Asignacion(fuentes.get(nroGrafo).grafo, res);
            asignar.marcarSlotUtilizados(id);
        } else {
            //Si es que se bloqueo y no encontro un camino se guardara los datos de la conexion y la palabra bloqueado
            fuentes.get(nroGrafo).caminoUtilizado.add(99);
            fuentes.get(nroGrafo).caminos.add("Bloqueado:" + inicio + ":" + fin + ":" + cantFs);
            fuentes.get(nroGrafo).ids.add(id);
            fuentes.get(nroGrafo).modificado.add(0);
        }

        int fsNuevo = calcularFsUno(nroGrafo);
        if (reasignar) {
            return false;
        }
        if (fsNuevo > fuentes.get(nroGrafo).fsUtilizados) {
            return true;
        }
        return false;
    }

    /**
     * En el segundo paso vamos a seleccionar una fuente de comida utilizando la ruleta para cambiar su solucion y verificar si es mejor
     **/
    public static void segundoPaso(int cantFuentes) {

        Random rand = new Random();
        float sumatoria = 0;
        float prueba;
        float suma = 0;

        //primero se calcula todos los pi de todas las fuentes de comida
        for (int i = 0; i < cantFuentes; i++) {
            sumatoria = sumatoria + fuentes.get(i).fsUtilizados;
        }

        // se agregan los valores de pi
        for (int j = 0; j < cantFuentes; j++) {
            prueba = fuentes.get(j).fsUtilizados / sumatoria;
            pi.add(prueba);
        }

        // se va cambiar un resultado dependiendo de la ruleta
        for (int p = 0; p < cantFuentes; p++) {

            float nectar = rand.nextFloat();

            for (int i = 0; i < cantFuentes; i++) {
                suma = suma + pi.get(i);

                if (suma >= nectar) {
                    double alpha = (double) (Math.random() * 2 - 1);
                    int j = rand.nextInt(fuentes.get(i).caminos.size() - 1);
                    int k = rand.nextInt(cantFuentes - 1);

                    while (j == k) {
                        k = rand.nextInt(cantFuentes - 1);
                    }

                    double nroCaminoAserUtilizado = (fuentes.get(i).caminoUtilizado.get(j)) + alpha * ((fuentes.get(i).caminoUtilizado.get(j)) - (fuentes.get(k).caminoUtilizado.get(j)));
                    int caminoAUsar = (int) nroCaminoAserUtilizado;
                    borrarConexion(j, i, caminoAUsar);
                    suma = 0;
                    i = cantFuentes;
                }
            }

        }

        for (int k = cantFuentes - 1; k >= 0; k--) {
            pi.remove(k);
        }

    }

    /**
     * En el tercer paso vamos a verificar si existen fuentes de comida abandonadas y vamos a guardar la mejor fuente de comida o solucion hasta el momento
     */

    public static void tercerPaso(int cantFuentes) {

        for (int i = 0; i < cantFuentes; i++) {
            for (int p = 0; p < fuentes.get(i).modificado.size(); p++) {
                if (fuentes.get(i).modificado.get(p) >= 2) {

                    String[] camino;
                    camino = fuentes.get(i).caminos.get(p).split(",");

                    String origen = camino[camino.length - 1];
                    String destino = camino[0];


                    String listaCaminos = "";
                    for (int l = 0; l < caminos.size(); l++) {
                        if (caminos.get(l)[0].equals(origen) && caminos.get(l)[1].equals(destino)) {
                            listaCaminos = caminos.get(l)[2];
                            break;
                        }
                    }

                    String[] lista = listaCaminos.split(";");


                    Random rand = new Random();
                    double alpha = (double) (Math.random() * 2 - 1);

                    int j = 1;
                    int u = lista.length - 1;

                    double nroCaminoAserUtilizado = alpha * (u - j);
                    int caminoAUsar = (int) nroCaminoAserUtilizado;
                    borrarConexion(j, i, caminoAUsar);


                }
            }
        }
    }

    /**
     * Funcion para elegir la mejor solucion de todas
     */
    public static void elegirConexion() {

        calcularFS(abejas);

        int cantBloqueados = 0;
        int cantBloqueadosNuevo = 0;

        int[] vertices = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
        GrafoMatriz g = new GrafoMatriz(vertices);
        g.InicializarGrafo(g.grafo);

        g.agregarRuta(1, 6, 1, 3, 200);
        g.agregarRuta(1, 7, 1, 3, 200);
        g.agregarRuta(1, 8, 1, 3, 200);
        g.agregarRuta(2, 3, 1, 3, 200);
        g.agregarRuta(2, 5, 1, 3, 200);
        g.agregarRuta(2, 6, 1, 3, 200);
        g.agregarRuta(3, 5, 1, 3, 200);
        g.agregarRuta(3, 8, 1, 3, 200);
        g.agregarRuta(4, 5, 1, 3, 200);
        g.agregarRuta(4, 6, 1, 3, 200);
        g.agregarRuta(6, 13, 1, 3, 200);
        g.agregarRuta(7, 10, 1, 3, 200);
        g.agregarRuta(7, 14, 1, 3, 200);
        g.agregarRuta(8, 9, 1, 3, 200);
        g.agregarRuta(8, 15, 1, 3, 200);
        g.agregarRuta(9, 10, 1, 3, 200);
        g.agregarRuta(9, 12, 1, 3, 200);
        g.agregarRuta(9, 15, 1, 3, 200);
        g.agregarRuta(10, 12, 1, 3, 200);
        g.agregarRuta(11, 13, 1, 3, 200);
        g.agregarRuta(11, 14, 1, 3, 200);
        g.agregarRuta(13, 14, 1, 3, 200);

        FuentesComida resultadoFinal = new FuentesComida(g);

        int nroGrafo = 0;

        for (int i = 0; i < fuentes.size(); i++) {
            cantBloqueados = 0;
            cantBloqueadosNuevo = 0;

            if (i == 0) {
                resultadoFinal = fuentes.get(i);
                int sumatoria;

            } else {
                for (int j = 0; j < resultadoFinal.caminoUtilizado.size(); j++) {
                    if (resultadoFinal.caminoUtilizado.get(j) == 99) {
                        cantBloqueados++;
                    }
                }
                for (int k = 0; k < fuentes.get(i).caminoUtilizado.size(); k++) {
                    if (fuentes.get(i).caminoUtilizado.get(k) == 99) {
                        cantBloqueadosNuevo++;
                    }
                }
                if (cantBloqueadosNuevo < cantBloqueados) {
                    resultadoFinal = fuentes.get(i);
                    nroGrafo = i;
                } else if (cantBloqueados == cantBloqueadosNuevo && resultadoFinal.fsUtilizados > fuentes.get(i).fsUtilizados) {
                    resultadoFinal = fuentes.get(i);
                    nroGrafo = i;
                }
            }

        }
        cantBloqueados = 0;


        for (int l = 0; l < resultadoFinal.caminoUtilizado.size(); l++) {
            if (resultadoFinal.caminoUtilizado.get(l) == 99) {
                cantBloqueados++;
            }
        }

        int m, n, b = 0;
        float contadorEntropia = 0;
        int empezoEn = 0;

        for (m = 0; m < fuentes.get(nroGrafo).grafo.grafo.length; m++) {
            for (n = 0; n < fuentes.get(nroGrafo).grafo.grafo.length; n++) {
                if (fuentes.get(nroGrafo).grafo.grafo[m][n].distancia != 0) {
                    empezoEn = fuentes.get(nroGrafo).grafo.grafo[m][n].listafs[0].libreOcupado;
                    for (b = 0; b < fuentes.get(nroGrafo).grafo.grafo[m][n].listafs.length; b++) {
                        if (empezoEn != fuentes.get(nroGrafo).grafo.grafo[m][n].listafs[b].libreOcupado) {
                            empezoEn = fuentes.get(nroGrafo).grafo.grafo[m][n].listafs[b].libreOcupado;
                            contadorEntropia++;
                        }
                    }
                }
            }
        }

        float indice = (float) fuentes.get(nroGrafo).fsUtilizados / 200;

        /**
         * Las metricas son:
         * - cantBloqueados
         * - indice
         * - contadorEntropia
         * - semiBloqueados
         */
//		System.out.println(indice +" "+ cantBloqueados +" "+ ((contadorEntropia/45)/2));
        System.out.println(((contadorEntropia / 45) / 2));
    }

    /**
     * Funcion para crear las solicitudes de manera aleatoria
     *
     * @param l
     * @throws IOException
     */
    public static void crearArchivosSolicitudes(int l) throws IOException {
        PrintWriter writer = null;
        int contador = 0;
        try {
            writer = new PrintWriter("data/solicitudes" + l, "UTF-8");
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        for (int i = 0; i <= 50; i++) {
            int origen = (int) (Math.random() * (15) + 1);
            int destino = (int) (Math.random() * (15) + 1);
            int fs = 1 + (int) (Math.random() * (10 - 1) + 1);
            int tiempo = 1 + (int) (Math.random() * (10) + 1);
            if (origen == destino) {
                while (origen == destino) {
                    destino = (int) (Math.random() * (15) + 1);
                }
            }

            writer.println(origen + "," + destino + "," + fs + "," + tiempo + "," + id);
            id++;
        }

        if (l > 1) {

            FileReader input = new FileReader("data/solicitudes" + (l - 1));
            BufferedReader bufRead = new BufferedReader(input);

            String linea = bufRead.readLine();

            while (linea != null && contador < 10) {

                if (linea.trim().equals("")) {
                    linea = bufRead.readLine();
                    continue;
                }
                String[] str_list = linea.trim().split("\\s*,\\s*");
                int origen = Integer.parseInt(str_list[0]);
                int destino = Integer.parseInt(str_list[1]);
                int fsActual = Integer.parseInt(str_list[2]);
                int tiempo = Integer.parseInt(str_list[3]);
                int id1 = Integer.parseInt(str_list[4]);
                int fsNuevo = 1 + (int) (Math.random() * (10 - 1) + 1);

                if (fsActual == fsNuevo) {
                    while (fsActual == fsNuevo) {
                        fsNuevo = 1 + (int) (Math.random() * (10 - 1) + 1);
                    }
                }

                writer.println(origen + "," + destino + "," + fsNuevo + "," + tiempo + "," + id1);
                contador++;
                linea = bufRead.readLine();

            }

            writer.close();
        } else {
            writer.close();
        }
    }
}
