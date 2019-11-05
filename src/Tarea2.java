import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class Tarea2 {
	public static void main(String[] args) {
		Barrera barrera = new Barrera(4, 3);
		Coche[] lista = new Coche[4];
		for (int i = 1; i <= 4; i++) {
			Coche coche = new Coche(i, barrera);
			lista[i - 1] = coche;
		}
		for (Coche vehiculo : lista) {
			vehiculo.start();
		}
	}
}

class Coche extends Thread {
	private int plaza;
	private int id;
	private Barrera barrera;
	private int entrada;
	private int salida;

	public Coche(int id, Barrera barrera) {
		this.id = id;
		this.barrera = barrera;
		Random random = new Random();
		entrada = random.nextInt(2000) + 1;
		salida = random.nextInt(2000) + 1;
	}

	public void entra(int plaza) {
		this.plaza = plaza;
	}

	public int sale() {
		return this.plaza;
	}

	public int getID() {
		return this.id;
	}

	@Override
	public void run() {
		barrera.llega(this);
		barrera.siguiente();
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
	private Coche[] aparcados;
	private LinkedList<Integer> esperando;
	private LinkedList<Integer> plazasLibres;
	private HashMap<Integer, Coche> listaCoches = new HashMap<Integer, Coche>();
	private int total;

	public Barrera(int coches, int plazas) {
		semaforo = new Semaphore(plazas);
		esperando = new LinkedList<Integer>();
		plazasLibres = new LinkedList<Integer>();
		total = coches;
		aparcados = new Coche[plazas];
		for (int i = 0; i < plazas; i++) {
			plazasLibres.add(i);
		}
	}

	public void entra(Integer num) {
		try {
			semaforo.acquire();
			Coche coche = listaCoches.get(num);
			int plaza = plazasLibres.poll();

			coche.entra(plaza);
			aparcados[plaza] = coche;
			System.out.println("el coche " + coche.getID() + " ha aparca en la plaza " + plaza);
			aparcado();
			plazasLibres.add(plaza);
			Coche.sleep(coche.getSalida());
			System.out.println("el coche " + coche.getID() + " ha salido de la plaza " + coche.sale());
			aparcados[coche.sale()] = null;
			aparcado();
			semaforo.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public boolean isLlena() {
		return esperando.size() == this.total;
	}

	public void llega(Coche coche) {
		try {
			Coche.sleep(coche.getEntrada());
			semaforo.acquire();
			this.esperando.add(coche.getID());
			this.listaCoches.put(coche.getID(), coche);
			semaforo.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public boolean isPendiente() {
		return !esperando.isEmpty();
	}

	public void aparcado() {
		String aparcado = "";
		for (int i = 0; i < aparcados.length; i++) {
			if (aparcados[i] != null) {
				aparcado += "[" + aparcados[i].getID() + "] ";
			} else {
				aparcado += "[] ";
			}
		}
		System.out.println(aparcado);
	}

	public void siguiente() {
		while (isPendiente()) {
			Integer num = esperando.poll();
			entra(num);
		}
	}
}
