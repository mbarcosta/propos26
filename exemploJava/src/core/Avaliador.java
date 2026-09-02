package core;

import exemploJava.Calculadora;

public class Avaliador {
	public Calculadora c;
	
	public void teste() {
		GetSet x, y, z;
		x = new GetSet();
		y = new GetSet();
		x.teste();
		y.teste();
	}

	public Calculadora getC() {
		return c;
	}

	public void setC(Calculadora c) {
		this.c = c;
	}

}
