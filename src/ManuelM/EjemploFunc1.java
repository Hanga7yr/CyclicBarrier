package ManuelM;

import java.util.ArrayList;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class EjemploFunc1 {
	static CyclicBarrier barrera1;
	
	public void ejemploEspera(int numHilos, int numHilosAEsperar) {
		barrera1 = new CyclicBarrier(numHilosAEsperar);
		ArrayList<Thread> threadHandle = new ArrayList<Thread>();

		
		//Creamos los hilos
		for(int i = 0; i < numHilos; i++)
			threadHandle.add(new hilo(i+1));
		
		System.out.println();
		
		
		//Los lanzamos, sin embargo, debido a la barrera estos pararan hasta que lleguen todos al mismo punto.
		for(Thread i : threadHandle)
			i.start();
		
		
		//Si el programa no ha terminado a los 10 segundos, interumpimos uno de los hilos
		//Haciendo que se rompa la barrera, permitiendo que continuen los hilos.
		Thread t = new Thread() {
			public void run() {
				try {
					sleep(10000);
					System.out.println("\nTiempo de Espera agotado");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				//Reseteo la barrera, haciendo que los hilos esperando salten la barrera con la excepción BrokenBarrier
				barrera1.reset();
			}
		};
		t.start();
		
		
		
		//Esperamos a que todos terminen antes de cerrar el hilo principal
		//Lo que ocurre una vez se muestra el error de cyclicBarrier
		try {
			
			for(Thread i : threadHandle){
				i.join();
				System.out.println("Hilo: " + i.getName() + " ha terminado...");
			}

			t.join();
		} catch (InterruptedException e) {
			System.out.println(e.getMessage());
		}
		
		System.out.println("Principal Termina");
	}
	
	public class hilo extends Thread{
		public hilo(int i ) {
			this.setName(i + "");

			System.out.println("Hilo: " + this.getName() + " creado...");
		}
		
		public void run() {
			
			try {
				System.out.println("Hilo: " + this.getName() + " esperando a todos los hilos...");
				barrera1.await();
				System.out.println("Hilo: " + this.getName() + " vuelvo a funcionar...");
			} catch (InterruptedException e) {
				System.out.println("Hilo: " + this.getName() + " ha sido interumpido");
			} catch(BrokenBarrierException e) {
				System.out.println("Hilo: " + this.getName() + ". Algunos de los hilos ha sido interumpido. Rompiendo la barrera y haciendo que continuen.");
			}
		}
	}
	
	public class hilo_carrera extends Thread{
		public hilo_carrera(int i) {
			this.setName(i + "");
		}
		
		@Override
		public void run() {
			System.out.println("Hilo " + this.getName() + ": Ha entrado a la carrera");
			
			try {
				//Espero a que todos los hilos esten en un punto comun
				inicio.await();
				
				//Espero una cantidad de tiempo random
				//Al llegar a una etapa, se muestra su mensaje
				//Si se llega a la etapa final el primero, finalizo la carrera.
				for(int i = 0; i < etapas.length; i++) {
					Thread.sleep((long) (Math.random()*1000));
					int posicion = etapas[i].await();
				}
				
				if(fin.await() == 0)
					System.out.println("Hilo " + this.getName() + ": He ganado la carrera!!");
			} catch (InterruptedException | BrokenBarrierException e) {
				System.out.println("Hilo " + this.getName() + ": Alguien ha llegado al final. Me rindo");
			}
		}
	}
	
	public class mensaje_etapa implements Runnable{
		int etapa = 0;
		int nHilos = 0;
		int llegados = 0;
		
		public mensaje_etapa(int i, int numHilos) {
			etapa = i;
			nHilos = numHilos;
		}
		
		public void run() {
			//Compruebo si se ha llegado en una posición determinada y muestro un mensaje apropiado
			if(llegados++ == 0)
				System.out.println("Hilo " + Thread.currentThread().getName() + ": He llegado primero a la etapa " + etapa);
			else if(llegados == nHilos)
				System.out.println("Hilo " + Thread.currentThread().getName() + ": He llegado último a la etapa " + etapa);
		}
		
	}
	
	static CyclicBarrier[] etapas = null;
	static CyclicBarrier inicio = null;
	static CyclicBarrier fin = null;
	public void carreaHilos(int numHilos, int nEtapas){
		ArrayList<Thread> threadHandle = new ArrayList<Thread>();

		//Creo las etapas y le añado un Runnable a realizar cada vez que la barrera se rompa.
		etapas = new CyclicBarrier[nEtapas];
		for(int i = 0; i < etapas.length; i++)
			etapas[i] = new CyclicBarrier(1, new mensaje_etapa(i, numHilos));
		
		//Instacio los hilos
		for(int i = 0; i < numHilos; i++)
			threadHandle.add(new hilo_carrera(i+1));
		
		//Creo un cyclicBarrier para esperar a que todos los hilos llegen a un punto comun
		//Y asi empiece en condiciones iguales
		inicio = new CyclicBarrier(numHilos, new Runnable() {
			public void run() {
				System.out.println("\nTodos los hilos han llegado a la linea de inicio. Iniciando carrera");
			}
		});
		
		//Barrera final que mostrará el final de la carrera
		//Tambien hará que el resto de los hilos terminen a la fuerza para terminar la carrera
		fin = new CyclicBarrier(1, new Runnable() {
			public void run() {
				System.out.println("\nAlguien ha llegado a la meta. Finalizando carrera");
				
				//Finalizo a la fuerza el resto de hilos. Ya que ha acabado la carrera
				for(Thread i : threadHandle)
					i.interrupt();
			}
		});

		//Inicio los hilos, al iniciarlos secuencialmente, siempre habra uno que empiece antes que otro.
		for(Thread i : threadHandle)
			i.start();
		
		//Esperamos a que todos acaben para que el principal acaba siempre despues que los hilos hijo
		for(Thread i : threadHandle)
			try {
				i.join();
			} catch (InterruptedException e) {}
	}
}
