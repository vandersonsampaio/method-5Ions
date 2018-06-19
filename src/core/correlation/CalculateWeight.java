package core.correlation;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

public class CalculateWeight {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PearsonsCorrelation pearson = new PearsonsCorrelation();
		
		double[] virtualValues = {-0.1111111, -0.10454543, -0.09583331,  -0.10288062, -0.09594092, -0.088135555, -0.07807805, -0.07242337, -0.07182318, -0.071232855, -0.08894878, 
				-0.08847184, -0.087533146, -0.08780487, -0.08591885, -0.07982262}; //Fórmula 1
		double[] realValues = {9, 8, 9, 10, 10, 9, 9, 9, 9, 9, 9, 9, 10, 9, 10, 10}; //YouGov
		
		double corr = pearson.correlation(virtualValues, realValues);
		
		//System.out.println("F1:" + corr);
		
		//ShortTime
		double[] virtualPartPos = {1.7, 0.1, 0, 0.2, 0.1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.8};
		double[] virtualPartNeg = {-16.6, -1, -1.7, -0.3, -2.5, -0.5, -3.1, -1.3, 0, -0.1, -0.6, -0.2, -0.3, -1.1, -3, -0.5};
		
		double[] virtualCandPos = {0.2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.7, 0, 0, 0.2, 0, 0};
		double[] virtualCandNeg = {-3, -0.2, 0, 0, -1.2, -1, -2.2, -0.1, 0, 0, 0, 0, 0, -1.6, -0.4, -0.1};
		
		double[] virtualEnvPos = {0.3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.1, 0, 0};
		double[] virtualEnvNeg = {-1.5, 0, 0, 0, 0, 0, 0, 0, 0, -0.4, 0, -0.2, -0.1, -0.3, 0, 0};
		
		//Acum
		double[] virtualPartPosAC = {1.7, 1.8, 1.8, 2, 2.1, 2.1, 2.1, 2.1, 2.1, 2.1, 2.1, 2.1, 2.1, 2.1, 2.1, 2.9};
		double[] virtualPartNegAC = {-16.6, -17.6, -19.3, -19.6, -22.1, -22.6, -25.7, -27, -27, -27.1, -27.7, -27.9, -28.2, -29.3, -32.3, -32.8};
		
		double[] virtualCandPosAC = {0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.9, 0.9, 0.9, 1.1, 1.1, 1.1};
		double[] virtualCandNegAC = {-3, -3.2, -3.2, -3.2, -4.4, -5.4, -7.6, -7.7, -7.7, -7.7, -7.7, -7.7, -7.7, -9.3, -9.7, -9.8};
		
		double[] virtualEnvPosAC = {0.3, 0.3, 0.3, 0.3, 0.3, 0.3, 0.3, 0.3, 0.3, 0.3, 0.3, 0.3, 0.3, 0.4, 0.4, 0.4};
		double[] virtualEnvNegAC = {-1.5, -1.5, -1.5, -1.5, -1.5, -1.5, -1.5, -1.5, -1.5, -1.9, -1.9, -2.1, -2.2, -2.5, -2.5, -2.5};
		
		//double[] consolPE = new double[16];
		//double[] consolPC = new double[16];
		double[] consolP = new double[16];
		double[] consolC = new double[16];
		double[] consolE = new double[16];
		//double[] consolCE = new double[16];
		double[] consol = new double[16];
		
		double[] corre = new double[3];
		double[] coeficientes = new double[3];
		int iMaior;
		int iMeio;
		int iMenor;
		
		for(int i = 0; i < 3; i++)
			coeficientes[i] = 1;
		
		coeficientes[0] = 1;
		coeficientes[1] = 3;
		coeficientes[2] = -3.2;
		
		double[] virtualEnvAC = new double[virtualEnvNeg.length];
		double[] virtualCandAC = new double[virtualEnvNeg.length];
		double[] virtualPartAC = new double[virtualEnvNeg.length];
		
		for(int i = 0; i < virtualEnvPosAC.length; i++){
			virtualEnvAC[i] = virtualEnvPosAC[i] / (virtualEnvPosAC[i] + virtualEnvNegAC[i]);
			virtualCandAC[i] = virtualCandPosAC[i] / (virtualCandPosAC[i] + virtualCandNegAC[i]);
			virtualPartAC[i] = virtualPartPosAC[i] / (virtualPartPosAC[i] + virtualPartNegAC[i]);
			
			consol[i] = (coeficientes[0] * virtualPartAC[i] + coeficientes[1] * virtualCandAC[i] + coeficientes[2] * virtualEnvAC[i]);
		}
		
		corr =  pearson.correlation(consol, realValues);
		System.out.println("Corr: " + Math.abs(corr));
		
		
		
		double aux = calcule(virtualPartAC, virtualCandAC, virtualEnvAC, realValues, coeficientes);
		System.out.println("Corr: " + Math.abs(aux));
		
		System.exit(0);
		
		for(int i = 0; i < virtualPartPosAC.length; i++){
			consolP[i] = (virtualPartPosAC[i]) / (virtualPartNegAC[i]);
			consolC[i] = (virtualCandPosAC[i]) / (virtualCandNegAC[i]);
			consolE[i] = (virtualEnvPosAC[i]) / (virtualEnvNegAC[i]);
			//consolPE[i] = (virtualPartPosAC[i] + virtualEnvPosAC[i]) / (virtualPartNegAC[i] + virtualEnvNegAC[i]);
			//consolPC[i] = (virtualPartPosAC[i] + virtualCandPosAC[i]) / (virtualPartNegAC[i] + virtualCandNegAC[i]);
			//consolCE[i] = (virtualCandPosAC[i] + virtualEnvPosAC[i]) / (virtualCandNegAC[i] + virtualEnvNegAC[i]);
			consol[i] = (coeficientes[0] * virtualPartPosAC[i] + 
					coeficientes[1] * virtualCandPosAC[i] + 
					coeficientes[2] * virtualEnvPosAC[i]) / 
					(coeficientes[0] * virtualPartNegAC[i] + 
							coeficientes[1] * virtualCandNegAC[i] + 
							coeficientes[2] * virtualEnvNegAC[i]);
		}
		
		corre[0] = Math.abs(pearson.correlation(consolP, realValues));
		System.out.println("Partido: " + corre[0]);
		
		corre[1] = Math.abs(pearson.correlation(consolC, realValues));
		System.out.println("Candidato: " + corre[1]);
		
		corre[2] = Math.abs(pearson.correlation(consolE, realValues));
		System.out.println("Envolvido: " + corre[2]);
		
		if(corre[0] > corre[1] && corre[0] > corre[2]){
			iMaior = 0;
			if(corre[1] > corre[2]){
				iMeio = 1;
				iMenor = 2;
			} else {
				iMeio = 2;
				iMenor = 1;
			}
		} else if(corre[1] > corre[2]){
			iMaior = 1;
			if(corre[0] > corre[2]){
				iMeio = 0;
				iMenor = 2;
			} else {
				iMeio = 2;
				iMenor = 0;
			}
		} else {
			iMaior = 2;
			if(corre[0] > corre[1]){
				iMeio = 0;
				iMenor = 2;
			} else {
				iMeio = 2;
				iMenor = 0;
			}
		}
		
		System.out.println("Maior: " + iMaior);
		System.out.println("Meio: " + iMeio);
		System.out.println("Menor: " + iMenor);
		
		int iteracoes = 1;
		double ultCorr = corr - 0.1;
		double diff = 0, ultDiff;
		boolean plus = true;
		double cons = 0.3;
		while(corr < .7 && iteracoes < 100000){
			ultDiff = diff;
			
			if(ultCorr > corr) {
				diff = cons * Math.sqrt(ultCorr); //novo calculo
				
				if(iteracoes % 3 == 1){
					coeficientes[iMenor] += ultDiff;
				} else if(iteracoes % 3 == 2){	
					coeficientes[iMaior] -= ultDiff;
				} else {
					if(plus)
						coeficientes[iMeio] += ultDiff;
					else
						coeficientes[iMeio] -= ultDiff;
				}
			} else {
				diff = cons * (corr - ultCorr); //novo calculo
			}
			
			ultCorr = corr;
			
			if(iteracoes % 3 == 1){
				coeficientes[iMaior] += diff;
			} else if(iteracoes % 3 == 2){	
				if(plus)
					coeficientes[iMeio] += diff;
				else
					coeficientes[iMeio] += diff;
				
				plus = !plus;
			} else {
				coeficientes[iMenor] -= diff;
			}
			
			
			
			for(int i = 0; i < virtualPartPosAC.length; i++){
				consol[i] = (coeficientes[0] * virtualPartPosAC[i] + 
						coeficientes[1] * virtualCandPosAC[i] + 
						coeficientes[2] * virtualEnvPosAC[i]) / 
						(coeficientes[0] * virtualPartNegAC[i] + 
								coeficientes[1] * virtualCandNegAC[i] + 
								coeficientes[2] * virtualEnvNegAC[i]);
			}
		
			//corr = pearson.correlation(consolPE, realValues);
			//System.out.println("Par-Env: " + corr);
		
			//corr = pearson.correlation(consolPC, realValues);
			//System.out.println("Par-Cand: " + corr);
		
			//corr = pearson.correlation(consolCE, realValues);
			//System.out.println("Can-Env: " + corr);
		
			corr = Math.abs(pearson.correlation(consol, realValues));
			System.out.println("Coeficientes: " + coeficientes[0] + " ; " + coeficientes[1] + " , " + coeficientes[2]);
			System.out.println("Iteração: " + iteracoes++ + " Correlação: " + corr);
		}
	}
	
	private static double calcule(double[] a, double[] b, double[] c, double[] real, double[] weigth){
		double[] covA = covX(a);
		double[] covB = covX(b);
		double[] covC = covX(c);
		
		double[] covReal = covY(real);
		double varReal = var(covReal);
		
		double cof = 0;
		double aux = 0;
		
		for(int i = 0; i < covA.length; i++){
			aux += (weigth[0] * covA[i] + weigth[1] * covB[i] + weigth[2] * covC[i]) * covReal[i];
		}
		
		cof = aux / (varX(covA, covB, covC, weigth) * varReal);
		
		System.out.println("Numerador: " + (aux / 0.6));
		System.out.println("Denominador: " + varX(covA, covB, covC, weigth) * varReal);
		
		return cof;
	}
	
	private static double[] covX(double[] values){
		double[] ret = new double[values.length];
		double med = med(values);
		
		for(int i = 0; i < values.length; i++)
			ret[i] = values[i] - med;
		
		return ret;
	}
	
	private static double[] covY(double[] values){
		double auxMed = med(values);
		double[] ret = new double[values.length];
		
		for(int i = 0; i < values.length; i++)
			ret[i] = values[i] - auxMed;
		
		return ret ;
	}
	
	private static double varX(double[] covA, double[] covB, double[] covC, double[] weigth){
		double ret = 0;
		
		for(int i = 0; i < covA.length; i++){
			ret += Math.pow(weigth[0] * covA[i], 2) + 2 * weigth[0] * covA[i] * weigth[1] * covB[i] + 2 * weigth[0] * covA[i] * weigth[2] * covC[i] + Math.pow(weigth[1] * covB[i], 2) 
				+ 2 * weigth[1] * covB[i] * weigth[2] * covC[i] + Math.pow(weigth[2] * covC[i], 2);
		}
		
		return Math.sqrt(ret);
	}
	
	private static double var(double[] values){
		double ret = 0;
		
		for(int i = 0; i < values.length; i++)
			ret += values[i] * values[i];
		
		return Math.sqrt(ret);
	}
	
	private static double med(double[] values){
		double ret = 0;
		
		for(int i = 0; i < values.length; i++)
			ret += values[i];
		
		return ret / values.length; 
	}
}
