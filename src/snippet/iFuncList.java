package snippet;

public class iFuncList {
	public static class LogNormalFunc implements InfluenceFunc {

		double beta;
		double miu;
		double sigma;
		
		public LogNormalFunc(double beta, double miu, double sigma) {
			this.beta = beta;
			this.miu = miu;
			this.sigma = sigma;
			assert(beta > 0);
		}
		
		@Override
		public double Calculate(double dist) {
			double c = Math.exp(miu - sigma*sigma);
			double x = c - dist * beta;
//            TODO uncomment this assertion
//			assert(x > 0);
			return 1/(x * sigma * 2.506628) * Math.exp(-Math.pow((Math.log(x) - miu), 2)/2/sigma/sigma);
		}

	}
	
	public static class LinerFunc implements InfluenceFunc {
		double c;
		double beta;
		double k1;	//线性分段的两个斜率
		double k2;
		
		public LinerFunc(double c, double beta, double k1, double k2) {
			assert(c > 0 && beta > 0 && k1 > 0 && k2 > 0 && k1 > k2);
			this.c = c;
			this.beta = beta;
			this.k1 = k1;
			this.k2 = k2;
		}
		
		/*x不能小于0，所以需要调节k1和k2的值*/
		/*这里的c需要自行设定以确定左边直线的终止位置*/
		@Override
		public double Calculate(double dist) {
			double x = c - dist * beta;
			assert(x > 0);
			if (dist > 0) {
				return k1 * x;
			} else {
				return -k2 * (x - c) + k1 * c;
			}
		}
	}
	
	public static class LogFunc implements InfluenceFunc {
		double beta;
		double k1;
		double k2;
		public LogFunc(double beta, double k1, double k2) {
			assert(beta > 0 && k1 != 0 && k2 > 0);
			this.beta = beta;
			this.k1 = k1;
			this.k2 = k2;
		}
		
		@Override
		public double Calculate(double dist) {
			double x = 1/k1 - dist * beta;
			assert(x > 0);
			return 1/(Math.pow(Math.log(k1*x),2) + k2);
		}
	}
	
	public static class SineFunc implements InfluenceFunc {
		double beta;
		double k;
		double n;
		public SineFunc(double beta, double n, double k) {
			assert(beta > 0 && n > 0 && k > 0);
			this.beta = beta;
			this.k = k;
			this.n = n;
		}
		
		@Override
		public double Calculate(double dist) {
			double c = Math.pow(n, 1/k);
			double x = c - dist * beta;
			assert(x > 0);
			return Math.sin(Math.PI * Math.pow(x, k) / (n + Math.pow(x, k)));
		}
	}
	
}
