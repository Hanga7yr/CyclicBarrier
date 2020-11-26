package ManuelM;

import java.util.ArrayList;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class EjemploFunc1 {
	static CyclicBarrier barrera1;
	static boolean[] terminado;
	
	public void ejemploEspera(int numHilos, int numHilosAEsperar) {
		barrera1 = new CyclicBarrier(numHilosAEsperar);
		ArrayList<Thread> threadHandle = new ArrayList<Thread>();
		
		//Array para guardar que hilos han acabado
		terminado = new boolean[numHilos];
		
		//Creamos los hilos
		for(int i = 0; i < numHilos; i++)
			threadHandle.add(new hilo(i));
		
		System.out.println();
		
		//Los lanzamos, sin embargo, debido a la barrera estos pararan hasta que lleguen todos al mismo punto.
		for(Thread i : threadHandle)
			i.start();
		
		//Si el programa no ha terminado a los 10 segundos, interumpimos uno de los hilos
		//Haciendo que se rompa la barrera, permitiendo que continuen los hilos.
		(new Thread() {
			public void run() {
				try {
					sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				//Busco un hilo que no haya acabado, y lo interumpo.
				for(int i = 0; i < numHilos; i++)
					if(terminado[i] == false)
						threadHandle.get(i).interrupt();
			}
		}).start();
		
		//Se espera un tiempo a que acaben los hilos para insertar un espaciado aestetico
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		System.out.println();
		
		//Esperamos a que todos terminen antes de cerrar el hilo principal
		//Lo que ocurre una vez se muestra el error de cyclicBarrier
		try {

			for(Thread i : threadHandle){
				i.join();
				System.out.println("Hilo: " + i.getName() + " ha terminado...");
			}
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
			

			//Guardo los hilos que ya han acabado, para saber cual esta todavia
			terminado[Integer.parseInt(this.getName())] = true;
		}
	}
}
