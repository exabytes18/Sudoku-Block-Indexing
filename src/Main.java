/**
 * Here's a fun example for screwing around with java performance. It was born
 * out of curiousity of testing the performance of various approaches to
 * calculating the index of a particular cell in a sudoku grid. I have included
 * 4 different approaches that use various forms of integer division, floating
 * point operations, and lookup tables to calculate the result. As with many
 * java program benchmarks, this is flawed.
 * <p/>
 * d6ec5990d5f8bbd2f970e8887afdcdf4ede36ec1ba8498e5d6d8d78556c3fcfe
 */
public class Main {
	private static final BlockIndexer intDivHeavy = new BlockIndexer("Integer Division (Heavy)") {
		@Override
		int blockIndex(int i) {
			return (i - 27 * (i / 27) - 9 * ((i - 27 * (i / 27)) / 9)) / 3 + 3 * (i / 27);
		}
	};
	private static final BlockIndexer intDivLight = new BlockIndexer("Integer Division (Light)") {
		@Override
		int blockIndex(int i) {
			return (i - 9 * (i / 9)) / 3 + 3 * (i / 27);
		}
	};
	private static final BlockIndexer lookup = new BlockIndexer("Lookup") {
		private char[] lookup = {0, 0, 0, 1, 1, 1, 2, 2, 2, 0, 0, 0, 1, 1, 1, 2, 2, 2, 0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 4, 5, 5, 5, 3, 3, 3, 4, 4, 4, 5, 5, 5, 3, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6, 6, 7, 7, 7, 8, 8, 8, 6, 6, 6, 7, 7, 7, 8, 8, 8, 6, 6, 6, 7, 7, 7, 8, 8, 8};

		@Override
		int blockIndex(int i) {
			return lookup[i];
		}
	};
	private static final BlockIndexer floor = new BlockIndexer("Floor") {
		@Override
		int blockIndex(int i) {
			return (int) Math.floor((i % 9) / 3) + 3 * (int) Math.floor(i / (9 * 3));
		}
	};

	public static void main(String[] args) {
		intDivHeavy.printBenchmark();
		System.out.println();
		intDivLight.printBenchmark();
		System.out.println();
		lookup.printBenchmark();
		System.out.println();
		floor.printBenchmark();
	}

	private static abstract class BlockIndexer {
		private final String name;

		protected BlockIndexer(String name) {
			this.name = name;
		}

		public void printBenchmark() {
			System.out.println(name);
			for (int i = 0; i < 9; i++) {
				for (int j = 0; j < 9; j++) {
					System.out.print(blockIndex(9 * i + j));
					System.out.print(" ");
				}
				System.out.println();
			}

			int numIterations = 10000000;

			// Warmup
			int y = 0;
			for (int k = 0; k < 20; k++) {
				y += loop(this, numIterations);
			}
			System.out.println(y);

			long start = System.nanoTime();
			int x = loop(this, numIterations);
			long end = System.nanoTime();

			System.out.println("Summation:\t\t\t\t" + x);
			System.out.printf("%d iterations:\t%.3fs\n", numIterations, (end - start) / 1e9);
		}

		private static int loop(final BlockIndexer indexer, final int numIterations) {
			int x = 0;

			/*
			 * This is a dangerous test procedure as a sufficiently smart
			 * compiler/jvm may realize that these loops can be safely
			 * interchanged enabling the calculation to be lifted out of the
			 * inner loop leaving essentially no work to be done at runtime.
			 *
			 * Of course, this can only be done after inlining of the
			 * blockIndex method (and loop invariants hold). However, this
			 * should still be applicable to any of the BlockIndexer's
			 * provided here.
			 */
			for (int i = 0; i < numIterations; i++) {
				for (int j = 0; j < 81; j++) {
					x += indexer.blockIndex(j);
				}
			}
			return x;
		}

		abstract int blockIndex(int cellIndex);
	}
}
