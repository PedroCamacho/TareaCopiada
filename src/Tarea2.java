

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class Tarea2 {
	public static void main(String[] args) {
		Barrera barrier = new Barrera(5, 2);
		Vehiculo[] cola = new Vehiculo[5];
		for (int i = 1; i < 6; i++) {
			Vehiculo coche = new Vehiculo(i, barrier);
			cola[i - 1] = coche;
		}
		for (Vehiculo coche : cola) {
			coche.start();
		}
		barrier.nextCoche();
	}
}

class Vehiculo extends Thread {

	private int plaza;
	private int identificador;
	private Barrera barrera;
	private int entrada;
	private int salida;

	public Vehiculo(int id, Barrera barrier) {
		this.identificador = id;
		this.barrera = barrier;
		Random randomGenerator = new Random();
		entrada = randomGenerator.nextInt(3000) + 1;
		salida = randomGenerator.nextInt(3000) + 1;
	}

	@Override
	public void run() {
		barrera.pedir(this);
		barrera.nextCoche();
	}

	public void aparcar(int plaza) {
		this.plaza = plaza;
	}

	public int salir() {
		return this.plaza;
	}

	public int getIdentificador() {
		return this.identificador;
	}

	public int getEntrada() {
		return entrada;
	}

	public void setEntrada(int entrada) {
		this.entrada = entrada;
	}

	public int getSalida() {
		return salida;
	}

	public void setSalida(int salida) {
		this.salida = salida;
	}
}

class Barrera {

	private Semaphore semaforo;
	private Vehiculo[] aparcados;
	private Queue<Integer> pendientes;
	private Queue<Integer> plazasLibres;
	private HashMap<Integer, Vehiculo> lista = new HashMap<Integer, Vehiculo>();
	private int total;

	public Barrera(int vehiculos, int plazas) {
		semaforo = new Semaphore(plazas);
		pendientes = new LinkedList<Integer>();
		plazasLibres = new LinkedList<Integer>();
		total = vehiculos;
		aparcados = new Vehiculo[plazas];
		for (int i = 0; i < plazas; i++) {
			plazasLibres.add(i);
		}
	}

	public boolean isLlena() {
		return pendientes.size() == this.total;
	}

	@SuppressWarnings("static-access")
	public void entrar(Integer coche) {
		try {
			semaforo.acquire();
			Vehiculo vehiculo = lista.get(coche);
			int plaza = plazasLibres.poll();

			vehiculo.aparcar(plaza);
			aparcados[plaza] = vehiculo;
			System.out.println("Coche " + vehiculo.getIdentificador() + " aparcando en la plaza " + plaza);
			estado();

			plazasLibres.add(plaza);
			vehiculo.sleep(vehiculo.getSalida());
			System.out.println("Coche " + vehiculo.getIdentificador() + " saliendo de la plaza " + vehiculo.salir());
			aparcados[vehiculo.salir()] = null;
			estado();

			semaforo.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("static-access")
	public void pedir(Vehiculo coche) {
		try {
			coche.sleep(coche.getEntrada());
			semaforo.acquire();
			this.pendientes.add(coche.getIdentificador());
			this.lista.put(coche.getIdentificador(), coche);
			semaforo.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public boolean isPendientes() {
		return !pendientes.isEmpty();
	}

	public void estado() {
		String state = " ";
		for (int i = 0; i < aparcados.length; i++) {
			if (aparcados[i] != null) {
				state += "[" + aparcados[i].getIdentificador() + "] ";
			} else {
				state += "[] ";
			}
		}
		System.out.println(state);
	}

	public void nextCoche() {
		while (isPendientes()) {
			Integer coche = pendientes.poll();
			entrar(coche);
		}
	}
}