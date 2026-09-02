package exemploJava;

import java.util.ArrayList;

import core.Avaliador;


public class Principal {
	public static void tgesteArray() {
		  ArrayList<String> nomes = new ArrayList<>();
		  ArrayList<Avaliador> avaliadores = new ArrayList<>();

	        nomes.add("Ana");
	        nomes.add("Bruno");
	        nomes.add("Carlos");
            Avaliador a = new Avaliador();
	        avaliadores.add(a);
            for (Avaliador x: avaliadores){
            	
            }
	        ;
	}

	public static void main(String[] args) {
		int s, m;
		Calculadora calc;
		calc = new Calculadora();
		// TODO Auto-generated method stub
       System.out.print("ola");
       s= Calculadora.soma(3, 4);
       double x = calc.multi(5.3, 6.2);
       Avaliador a;
       a = new Avaliador();
       a.c = new Calculadora();
       
	}

}
