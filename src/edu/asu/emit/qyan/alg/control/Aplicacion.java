package edu.asu.emit.qyan.alg.control;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import edu.asu.emit.qyan.alg.model.Path;
import edu.asu.emit.qyan.alg.model.VariableGraph;

public class Aplicacion {

	public static ArrayList<FuentesComida> fuentes = new ArrayList<>();
	public static VariableGraph graph = new VariableGraph("data/test_25");
	public static ArrayList<Float> pi = new ArrayList<>();
	public static ArrayList<String[]> caminos = new ArrayList<>();
	public static int abejas = 5;


	public static void main(String[] args) throws InterruptedException, IOException {

		crearArchivoCaminos();
		for (int m = 0; m < 5; m++) {
//		long startTime = System.nanoTime();

			leerArchivoCaminos();

			crearFuenteDeComida(abejas);

			for (int l = 1; l <= 6; l++) {
//				System.out.println("se lee el " + l + " archivo");
				cargarSolicitudes(abejas, l);

				for (int i = 0; i < 300; i++) {

					primerPaso(abejas);
					segundoPaso(abejas);
					tercerPaso(abejas);
				}

				for (int p = 0; p < abejas; p++) {

					fuentes.get(p).grafo.restar();

				}
			}
			elegirConexion();
//		long endTime   = System.nanoTime();
//		long totalTime = (endTime - startTime)/1000000000;
//		System.out.println(totalTime);
			fuentes.clear();
			pi.clear();
			caminos.clear();
		}
	}

	/**
	 * funcion para leer el archivo y guardar en memoria
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

	private static void cargarSolicitudes(int cantFuente, int solicitud) throws IOException {

		String numero = "";
		numero = Integer.toString(solicitud);

//		System.out.println("se carga: " + "solicitudes" + numero);
		FileReader input = new FileReader("data/solicitudes" + numero);
		BufferedReader bufRead = new BufferedReader(input);

		String linea = bufRead.readLine();

		while (linea != null ) {

			if (linea.trim().equals("")) {
				linea = bufRead.readLine();
				continue;
			}
			String[] str_list = linea.trim().split("\\s*,\\s*");

			/**
			 * Calculo para la cantidad de fs
			 */
			int calAux = Integer.parseInt(str_list[2]);
			double doubleAux = Integer.parseInt(str_list[2]);
			doubleAux = Math.ceil(calAux/10);
			calAux = (int) Math.ceil(doubleAux / 12);
			/**
			 *
			 */

			int origen = Integer.parseInt(str_list[0]);
			int destino = Integer.parseInt(str_list[1]);
			int fs = calAux;
			int tiempo = Integer.parseInt(str_list[3]);
			int id = Integer.parseInt(str_list[4]);

			int inicio = origen;
			int fin = destino;
			String listaCaminos = "";

			for (int k = 0; k < caminos.size(); k++) {
				if (caminos.get(k)[0].equals(str_list[0]) && caminos.get(k)[1].equals(str_list[1])) {
					listaCaminos = caminos.get(k)[2];
					break;
				}
			}

			for (int j = 0; j < cantFuente; j++) {

				if (fuentes.get(j).ids.contains(id)) {

//					System.out.println("se carga la solicitud " + linea);
					Boolean reasignar = fuentes.get(j).grafo.verificar_conexion(origen,id,fs);

					if (!reasignar) {
//						System.out.println("SE VA A VOLVER A ASIGNAR");
						BuscarSlot r = new BuscarSlot(fuentes.get(j).grafo, listaCaminos);
						resultadoSlot res = r.concatenarCaminos(fs, 0, 0);
						if (res != null) {
							//guardar caminos utilizados y el numero de camino utilizado
//							fuentes.get(j).caminoUtilizado.add(res.caminoUtilizado);
//							fuentes.get(j).caminos.add(res.camino);
//							fuentes.get(j).ids.add(id);
//							fuentes.get(j).modificado.add(0);
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
//							System.out.println("Se elimino y se va a guardar de nuevo");
							Asignacion asignar = new Asignacion(fuentes.get(j).grafo, res);
							asignar.marcarSlotUtilizados(id);
						} else {
//							System.out.println("NO SE ENCONTRO LUGAR");
							/**
							 * Si es que se bloqueo y no encontro un camino se guardara los datos de la conexion y la palabra bloqueado
							 */
//							fuentes.get(j).caminoUtilizado.add(99);
//							fuentes.get(j).caminos.add("Bloqueado:" + str_list[0] + ":" + str_list[1] + ":" + calAux);
//							fuentes.get(j).ids.add(id);
//							fuentes.get(j).modificado.add(0);
							//System.out.println("No se encontró camino posible y se guarda la informacion de la conexion.");
						}
					}

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
						/**
						 * Si es que se bloqueo y no encontro un camino se guardara los datos de la conexion y la palabra bloqueado
						 */
						fuentes.get(j).caminoUtilizado.add(99);
						fuentes.get(j).caminos.add("Bloqueado:" + str_list[0] + ":" + str_list[1] + ":" + calAux);
						fuentes.get(j).ids.add(id);
						fuentes.get(j).modificado.add(0);
						//System.out.println("No se encontró camino posible y se guarda la informacion de la conexion.");
					}
				}
			}
			linea = bufRead.readLine();
		}
		bufRead.close();
	}

	/**
	 * Funcion para crear el archivo de los posibles caminos
	 * @throws IOException
	 */
	private static void crearArchivoCaminos() throws IOException {
		YenTopKShortestPathsAlg yenAlg = new YenTopKShortestPathsAlg(graph);
		PrintWriter writer = new PrintWriter("data/Kcaminos", "UTF-8");

		// en este for hay que poner la cantidad de vertices que tenemos
		for (int i = 0; i <= 39; i++) {
			for (int k = 0; k <= 39; k++) {
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
	 * Funcion para crear una fuente de comida
	 */
	public static void crearFuenteDeComida(int cantFuente) throws IOException {

		//crear matriz inicial para todas las fuentes de comida
		// Matriz que representa la red igual al archivo test_16 que se va a utilar al tener los caminos.
		for (int i = 0; i<cantFuente ; i++) {
			int[] vertices = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25,26,27,28,29,30,31,32,33,34,35,36,37,38,39};
			GrafoMatriz g = new GrafoMatriz(vertices);
			g.InicializarGrafo(g.grafo);

            g.agregarRuta(   0, 12,1, 3,  200);
            g.agregarRuta( 0, 13, 1, 3,  200);
            g.agregarRuta(0, 16, 1, 3,  200);
            g.agregarRuta(0, 19, 1, 3,  200);
            g.agregarRuta(0, 28, 1, 3,  200);
            g.agregarRuta(1, 3, 1, 3,  200);
            g.agregarRuta( 1, 4, 1, 3,  200);
            g.agregarRuta(1 ,31, 1, 3, 200);
            g.agregarRuta( 1 ,39, 1, 3, 200);
            g.agregarRuta( 2 ,17, 1, 3, 200);
            g.agregarRuta( 2  ,21, 1, 3,  200);
            g.agregarRuta( 2, 23 ,1, 3,  200);
            g.agregarRuta(2 ,25, 1, 3,  200);
            g.agregarRuta(3, 5 ,1, 3,  200);
            g.agregarRuta(3 ,10 ,1, 3,  200);
            g.agregarRuta(3 ,13 ,1, 3,  200);
            g.agregarRuta(3 ,36 ,1, 3,  200);
            g.agregarRuta(4 ,31 ,1, 3,  200);
            g.agregarRuta(4 ,33 ,1, 3,  200);
            g.agregarRuta(4 ,37 ,1, 3,  200);
            g.agregarRuta(5 ,10 ,1, 3,  200);
            g.agregarRuta(5 ,13 ,1, 3,  200);
            g.agregarRuta(5 ,29 ,1, 3,  200);
            g.agregarRuta(5 ,36, 1, 3,  200);
            g.agregarRuta(6 ,9 ,1, 3,  200);
            g.agregarRuta( 6 ,24 ,1, 3,  200);
            g.agregarRuta(6 ,29 ,1, 3,  200);
            g.agregarRuta(6 ,35, 1, 3,  200);
            g.agregarRuta(7 ,8 ,1, 3,  200);
            g.agregarRuta(7 ,14 ,1, 3,  200);
            g.agregarRuta(7 ,15 ,1, 3,  200);
            g.agregarRuta(7 ,32 ,1, 3,  200);
            g.agregarRuta(8 ,14 ,1, 3,  200);
            g.agregarRuta(8 ,15 ,1, 3,  200);
            g.agregarRuta(8 ,18 ,1, 3,  200);
            g.agregarRuta(8 ,32 ,1, 3,  200);
            g.agregarRuta(9 ,22 ,1, 3,  200);
            g.agregarRuta(9 ,29 ,1, 3,  200);
            g.agregarRuta(9 ,35 ,1, 3,  200);
            g.agregarRuta(9 ,39 ,1, 3,  200);
            g.agregarRuta( 10, 12 ,1, 3,  200);
            g.agregarRuta( 10 ,19 ,1, 3,  200);
            g.agregarRuta(11 ,14 ,1, 3,  200);
            g.agregarRuta(11 ,15 ,1, 3,  200);
            g.agregarRuta( 11 ,32 ,1, 3,  200);
            g.agregarRuta( 11 ,38 ,1, 3,  200);
            g.agregarRuta( 12 ,19 ,1, 3,  200);
            g.agregarRuta(   12 ,28 ,1, 3,  200);
            g.agregarRuta( 13 ,28 ,1, 3,  200);
            g.agregarRuta(13 ,36 ,1, 3,  200);
            g.agregarRuta(14 ,15 ,1, 3,  200);
            g.agregarRuta(15 ,27 ,1, 3,  200);
            g.agregarRuta(16 ,19 ,1, 3,  200);
            g.agregarRuta(16 ,28 ,1, 3,  200);
            g.agregarRuta(16 ,36 ,1, 3,  200);
            g.agregarRuta(17 ,21 ,1, 3,  200);
            g.agregarRuta(17 ,23 ,1, 3,  200);
            g.agregarRuta( 17 ,25 ,1, 3,  200);
            g.agregarRuta(18 ,25 ,1, 3,  200);
            g.agregarRuta(18 ,26 ,1, 3,  200);
            g.agregarRuta(18 ,27 ,1, 3,  200);
            g.agregarRuta(18 ,30 ,1, 3,  200);
            g.agregarRuta(20 ,27 ,1, 3,  200);
            g.agregarRuta( 20 ,33 ,1, 3,  200);
            g.agregarRuta( 20 ,34 ,1, 3,  200);
            g.agregarRuta( 20 ,38 ,1, 3,  200);
            g.agregarRuta( 21 ,22 ,1, 3,  200);
            g.agregarRuta( 21 ,23 ,1, 3,  200);
            g.agregarRuta( 22 ,23, 1, 3,  200);
            g.agregarRuta( 22 ,39 ,1, 3,  200);
            g.agregarRuta( 23 ,25 ,1, 3,  200);
            g.agregarRuta( 24 ,31 ,1, 3,  200);
            g.agregarRuta( 24 ,35 ,1, 3,  200);
            g.agregarRuta(24 ,36 ,1, 3,  200);
            g.agregarRuta( 24 ,37 ,1, 3,  200);
            g.agregarRuta( 25 ,26 ,1, 3,  200);
            g.agregarRuta( 26 ,30 ,1, 3,  200);
            g.agregarRuta( 26 ,34 ,1, 3,  200);
            g.agregarRuta( 26 ,39 ,1, 3,  200);
            g.agregarRuta( 27 ,38 ,1, 3,  200);
            g.agregarRuta( 28 ,37 ,1, 3,  200);
            g.agregarRuta( 29 ,35 ,1, 3,  200);
            g.agregarRuta( 29 ,38 ,1, 3,  200);
            g.agregarRuta( 30 ,34 ,1, 3,  200);
            g.agregarRuta( 30 ,35 ,1, 3,  200);
            g.agregarRuta( 31 ,33 ,1, 3,  200);
            g.agregarRuta( 32 ,34 ,1, 3,  200);
            g.agregarRuta( 33 ,37 ,1, 3,  200);
            g.agregarRuta( 33 ,38, 1, 3,  200);

			fuentes.add(new FuentesComida(g));
		}
	}

	/**
	 * funcion para calcular los FS de todas las fuentes de comida
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
					for (int p = 0; p < fuentes.get(i).grafo.grafo[k][j].listafs.length; p++){
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

		//System.out.println("asd");

	}

	/**
	 * funcion para calcular los FS para una fuente de comida
	 */

	public static int calcularFsUno(int nroGrafo) {

		int indiceMayor = 0;

		// for para recorrer las filas de un grafo
		for (int k = 0; k < fuentes.get(nroGrafo).grafo.grafo.length; k++) {
			// for para recorrer las columnas de un grafo
			for (int j = 0; j < fuentes.get(nroGrafo).grafo.grafo.length; j++) {
				// for para recorrer el array de listafs (cada enlace del grafo)
				for (int p = 0; p < fuentes.get(nroGrafo).grafo.grafo[k][j].listafs.length; p++){
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
	 En el primer paso vamos a utilizar a las abejas empleadas para cambiar soluciones de las fuentes de comida si es que tienen
	 mejor resultado
	 **/
	public static void primerPaso(int cantFuentes) {
		calcularFS(cantFuentes);

		//calcular Vij para cada fuente de comida
		for (int i = 0; i < cantFuentes; i++) {
			Random rand = new Random();
			double alpha = (double)(Math.random() * 2 - 1);
			int j = rand.nextInt(fuentes.get(i).caminos.size()-1);
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
	 * Primero vamos a eliminar la conexion actual para volver a buscar un lugar para la misma
	 * @param nroCamino
	 * @param nroGrafo
	 */

	public static void borrarConexion(int nroCamino, int nroGrafo, int nroCaminoAUsar) {

		String camino = String.valueOf(fuentes.get(nroGrafo).caminos.get(nroCamino));
		Boolean reasignarSioSi = false;

		String caminoFinal = camino;
		//System.out.println("el camino es: " + caminoFinal);

        int inicioSolicitud = 0;
        int finSolicitud = 0;
        int inicio = 0;
        int longitud = 0;

        if (!caminoFinal.contains("Bloqueado")) {

			String[] caminosLista;
			caminosLista = caminoFinal.split(",");

            inicioSolicitud = Integer.parseInt(caminosLista[0]);
            finSolicitud = Integer.parseInt(caminosLista[caminosLista.length-1]);


            Boolean bandera = true;

            for (int p = 0; p < caminosLista.length - 1; p++) {

                int primer = Integer.parseInt(caminosLista[p]);
                int segundo = Integer.parseInt(caminosLista[p+1]);

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
            String[] lista = caminoFinal.split(":");
            inicioSolicitud = Integer.parseInt(lista[1]);
            finSolicitud = Integer.parseInt(lista[2]);
            longitud = Integer.parseInt(lista[3]);
			reasignarSioSi = true;
        }


		Boolean reasignar = asginar(inicioSolicitud, finSolicitud, nroGrafo, longitud, fuentes.get(nroGrafo).ids.get(nroCamino), nroCaminoAUsar, reasignarSioSi);

		if (reasignar) {
			Integer val = fuentes.get(nroGrafo).modificado.get(nroCamino);
			fuentes.get(nroGrafo).modificado.set(nroCamino, val + 1);

			fuentes.get(nroGrafo).caminos.remove(fuentes.get(nroGrafo).caminos.size()-1);
			fuentes.get(nroGrafo).ids.remove(fuentes.get(nroGrafo).ids.size()-1);
			fuentes.get(nroGrafo).caminoUtilizado.remove(fuentes.get(nroGrafo).ids.size()-1);
			fuentes.get(nroGrafo).modificado.remove(fuentes.get(nroGrafo).ids.size()-1);
			// volver a como estaba
			String[] caminosLista;
			caminosLista = caminoFinal.split(",");
			for (int p = 0; p < caminosLista.length-1; p++) {

				int primer = Integer.parseInt(caminosLista[p]);
				int segundo = Integer.parseInt(caminosLista[p+1]);

				for (int k=0; k < fuentes.get(nroGrafo).grafo.grafo[0][0].listafs.length; k++) {
					if (k == inicio)
						for (int j=0; j < longitud; j++ ) {
							fuentes.get(nroGrafo).grafo.grafo[primer][segundo].listafs[k].id = fuentes.get(nroGrafo).ids.get(nroCamino);
							fuentes.get(nroGrafo).grafo.grafo[primer][segundo].listafs[k].libreOcupado = 1;
							fuentes.get(nroGrafo).grafo.grafo[segundo][primer].listafs[k].id = fuentes.get(nroGrafo).ids.get(nroCamino);
							fuentes.get(nroGrafo).grafo.grafo[segundo][primer].listafs[k].libreOcupado = 1;
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
	 * @param inicio
	 * @param fin
	 * @param nroGrafo
	 * @param cantFs
	 * @param id
	 * @return
	 */

	public static Boolean asginar(int inicio, int fin, int nroGrafo, int cantFs, int id, int caminoAUsar, Boolean reasignar) {

		String listaCaminos = "";

		String inicioSolicitud = String.valueOf(inicio);
		String finSolicitud = String.valueOf(fin);


		for (int k = 0; k < caminos.size(); k++) {
			if (caminos.get(k)[0].equals(inicioSolicitud) && caminos.get(k)[1].equals(finSolicitud)) {
				listaCaminos = caminos.get(k)[2];
				break;
			}
		}

		BuscarSlot r = new BuscarSlot(fuentes.get(nroGrafo).grafo, listaCaminos);
		resultadoSlot res = r.concatenarCaminos(cantFs,3, caminoAUsar);


		if (res !=null) {
			//guardar caminos utilizados
			fuentes.get(nroGrafo).caminoUtilizado.add(res.caminoUtilizado);
			fuentes.get(nroGrafo).caminos.add(res.camino);
			fuentes.get(nroGrafo).ids.add(id);
			fuentes.get(nroGrafo).modificado.add(0);
			Asignacion asignar = new Asignacion(fuentes.get(nroGrafo).grafo, res);
			asignar.marcarSlotUtilizados(id);
		}
		else {
			/**
			 * Si es que se bloqueo y no encontro un camino se guardara los datos de la conexion y la palabra bloqueado
			 */
			fuentes.get(nroGrafo).caminoUtilizado.add(99);
			fuentes.get(nroGrafo).caminos.add("Bloqueado:" + inicio + ":" + fin + ":" + cantFs);
			fuentes.get(nroGrafo).ids.add(id);
			fuentes.get(nroGrafo).modificado.add(0);
			//System.out.println("No se encontró camino posible.");
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
	En el segundo paso vamos a seleccionar una fuente de comida utilizando la ruleta para cambiar su solucion y verificar si es mejor
	 **/
	public static void segundoPaso(int cantFuentes) {

		Random rand = new Random();
		float sumatoria = 0;
		float prueba;
		float suma = 0;

		//primero se calcula todos los pi de todas las fuentes de comida
		for (int i = 0; i<cantFuentes; i++) {
			sumatoria = sumatoria + fuentes.get(i).fsUtilizados;
		}

		// se agregan los valores de pi
		for (int j = 0; j<cantFuentes; j++) {
			prueba = fuentes.get(j).fsUtilizados / sumatoria;
			pi.add(prueba);
		}

		// se va cambiar un resultado dependiendo de la ruleta
		for (int p = 0; p < cantFuentes; p++) {

			float nectar = rand.nextFloat();

			for (int i=0; i < cantFuentes; i++) {
				suma = suma + pi.get(i);

				if (suma >= nectar) {
                    double alpha = (double)(Math.random() * 2 - 1);
                    int j = rand.nextInt(fuentes.get(i).caminos.size()-1);
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

        for (int k = cantFuentes-1; k >= 0; k--) {
            pi.remove(k);
        }

	}

	/**
	 * En el tercer paso vamos a verificar si existen fuentes de comida abandonadas y vamos a guardar la mejor fuente de comida o solucion hasta el momento
	 */

	public static void tercerPaso(int cantFuentes) {

		for (int i=0; i < cantFuentes; i++) {
			for (int p = 0; p < fuentes.get(i).modificado.size(); p++) {
				if (fuentes.get(i).modificado.get(p) >= 2) {

					String[] camino;
					camino = fuentes.get(i).caminos.get(p).split(",");

					String origen = camino[camino.length-1];
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
					double alpha = (double)(Math.random() * 2 - 1);

					int j = 1;
					int u = lista.length-1;

					double nroCaminoAserUtilizado = alpha * (u-j);
					int caminoAUsar = (int) nroCaminoAserUtilizado;
					borrarConexion(j, i, caminoAUsar);


				}
			}
		}
	}

	public static void elegirConexion() {

		calcularFS(abejas);

		int cantBloqueados = 0;
		int cantBloqueadosNuevo = 0;

		int[] vertices = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25,26,27,28,29,30,31,32,33,34,35,36,37,38,39};
		GrafoMatriz g = new GrafoMatriz(vertices);
		g.InicializarGrafo(g.grafo);

		g.agregarRuta(   0, 12,1, 1,  200);
		g.agregarRuta( 0, 13, 1, 1,  200);
		g.agregarRuta(0, 16, 1, 1,  200);
		g.agregarRuta(0, 19, 1, 1,  200);
		g.agregarRuta(0, 28, 1, 1,  200);
		g.agregarRuta(1, 3, 1, 1,  200);
		g.agregarRuta( 1, 4, 1, 1,  200);
		g.agregarRuta(1 ,31, 1, 1, 200);
		g.agregarRuta( 1 ,39, 1, 1, 200);
		g.agregarRuta( 2 ,17, 1, 1, 200);
		g.agregarRuta( 2  ,21, 1, 1,  200);
		g.agregarRuta( 2, 23 ,1, 1,  200);
		g.agregarRuta(2 ,25, 1, 1,  200);
		g.agregarRuta(3, 5 ,1, 1,  200);
		g.agregarRuta(3 ,10 ,1, 1,  200);
		g.agregarRuta(3 ,13 ,1, 1,  200);
		g.agregarRuta(3 ,36 ,1, 1,  200);
		g.agregarRuta(4 ,31 ,1, 1,  200);
		g.agregarRuta(4 ,33 ,1, 1,  200);
		g.agregarRuta(4 ,37 ,1, 1,  200);
		g.agregarRuta(5 ,10 ,1, 1,  200);
		g.agregarRuta(5 ,13 ,1, 1,  200);
		g.agregarRuta(5 ,29 ,1, 1,  200);
		g.agregarRuta(5 ,36, 1, 1,  200);
		g.agregarRuta(6 ,9 ,1, 1,  200);
		g.agregarRuta( 6 ,24 ,1, 1,  200);
		g.agregarRuta(6 ,29 ,1, 1,  200);
		g.agregarRuta(6 ,35, 1, 1,  200);
		g.agregarRuta(7 ,8 ,1, 1,  200);
		g.agregarRuta(7 ,14 ,1, 1,  200);
		g.agregarRuta(7 ,15 ,1, 1,  200);
		g.agregarRuta(7 ,32 ,1, 1,  200);
		g.agregarRuta(8 ,14 ,1, 1,  200);
		g.agregarRuta(8 ,15 ,1, 1,  200);
		g.agregarRuta(8 ,18 ,1, 1,  200);
		g.agregarRuta(8 ,32 ,1, 1,  200);
		g.agregarRuta(9 ,22 ,1, 1,  200);
		g.agregarRuta(9 ,29 ,1, 1,  200);
		g.agregarRuta(9 ,35 ,1, 1,  200);
		g.agregarRuta(9 ,39 ,1, 1,  200);
		g.agregarRuta( 10, 12 ,1, 1,  200);
		g.agregarRuta( 10 ,19 ,1, 1,  200);
		g.agregarRuta(11 ,14 ,1, 1,  200);
		g.agregarRuta(11 ,15 ,1, 1,  200);
		g.agregarRuta( 11 ,32 ,1, 1,  200);
		g.agregarRuta( 11 ,38 ,1, 1,  200);
		g.agregarRuta( 12 ,19 ,1, 1,  200);
		g.agregarRuta(   12 ,28 ,1, 1,  200);
		g.agregarRuta( 13 ,28 ,1, 1,  200);
		g.agregarRuta(13 ,36 ,1, 1,  200);
		g.agregarRuta(14 ,15 ,1, 1,  200);
		g.agregarRuta(15 ,27 ,1, 1,  200);
		g.agregarRuta(16 ,19 ,1, 1,  200);
		g.agregarRuta(16 ,28 ,1, 1,  200);
		g.agregarRuta(16 ,36 ,1, 1,  200);
		g.agregarRuta(17 ,21 ,1, 1,  200);
		g.agregarRuta(17 ,23 ,1, 1,  200);
		g.agregarRuta( 17 ,25 ,1, 1,  200);
		g.agregarRuta(18 ,25 ,1, 1,  200);
		g.agregarRuta(18 ,26 ,1, 1,  200);
		g.agregarRuta(18 ,27 ,1, 1,  200);
		g.agregarRuta(18 ,30 ,1, 1,  200);
		g.agregarRuta(20 ,27 ,1, 1,  200);
		g.agregarRuta( 20 ,33 ,1, 1,  200);
		g.agregarRuta( 20 ,34 ,1, 1,  200);
		g.agregarRuta( 20 ,38 ,1, 1,  200);
		g.agregarRuta( 21 ,22 ,1, 1,  200);
		g.agregarRuta( 21 ,23 ,1, 1,  200);
		g.agregarRuta( 22 ,23, 1, 1,  200);
		g.agregarRuta( 22 ,39 ,1, 1,  200);
		g.agregarRuta( 23 ,25 ,1, 1,  200);
		g.agregarRuta( 24 ,31 ,1, 1,  200);
		g.agregarRuta( 24 ,35 ,1, 1,  200);
		g.agregarRuta(24 ,36 ,1, 1,  200);
		g.agregarRuta( 24 ,37 ,1, 1,  200);
		g.agregarRuta( 25 ,26 ,1, 1,  200);
		g.agregarRuta( 26 ,30 ,1, 1,  200);
		g.agregarRuta( 26 ,34 ,1, 1,  200);
		g.agregarRuta( 26 ,39 ,1, 1,  200);
		g.agregarRuta( 27 ,38 ,1, 1,  200);
		g.agregarRuta( 28 ,37 ,1, 1,  200);
		g.agregarRuta( 29 ,35 ,1, 1,  200);
		g.agregarRuta( 29 ,38 ,1, 1,  200);
		g.agregarRuta( 30 ,34 ,1, 1,  200);
		g.agregarRuta( 30 ,35 ,1, 1,  200);
		g.agregarRuta( 31 ,33 ,1, 1,  200);
		g.agregarRuta( 32 ,34 ,1, 1,  200);
		g.agregarRuta( 33 ,37 ,1, 1,  200);
		g.agregarRuta( 33 ,38, 1, 1,  200);
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

		int m,n,b = 0;
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

		float indice = (float)fuentes.get(nroGrafo).fsUtilizados/200;

		System.out.println(indice +" "+ cantBloqueados +" "+ ((contadorEntropia/178)));
	}

}
