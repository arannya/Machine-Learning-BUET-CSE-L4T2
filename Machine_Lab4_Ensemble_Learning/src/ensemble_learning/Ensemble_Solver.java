package ensemble_learning;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

public class Ensemble_Solver {

	public static void main(String args[]) {

		int k = 25;
		int dim = 683;
		int width = 10;
		int[][] data = new int[dim][10];
		int[] index = new int[dim];
		int[] usedRows = new int[dim];
		int[] usedColumns = new int[width];

		Scanner scanner;

		try {
			scanner = new Scanner(new File("data.csv"));

			for (int i = 0; i < dim; i++) {
				index[i] = i;
				String[] line = scanner.nextLine().split(",");
				for (int j = 0; j < width; j++) {
					data[i][j] = Integer.parseInt(line[j]);

				}

			}

		} catch (FileNotFoundException e) {

			e.printStackTrace();
		}

		int trainLength = dim * 80 / 100;
		// System.out.println(trainLength);
		int[][] trainData = new int[trainLength][width];
		int[][] testData = new int[dim - trainLength][width];

		double tp = 0, fp = 0, tn = 0, fn = 0;
		double recall, precision, gMean, accuracy, fMeasure;

		// ========================ensemble
		// learning========================================
		System.out.println("Training individual learning stamps by ID3 algorithm in training data: \n");
		int iteration = 1;
		RandomizeArray(index);
		ArrayList<treeNode> All_hypothesis_Trees = new ArrayList<treeNode>();
		for (int x = 0; x < iteration; x++) {

			for (int i = 0; i < trainLength; i++) {

				// usedRows[i]=0;
				if (i < width)
					usedColumns[i] = 0;

				for (int j = 0; j < width; j++) {
					trainData[i][j] = data[index[i]][j];

				}

			}
			int count = 0;
			for (int i = trainLength; i < dim; i++) {

				usedRows[i] = 0;
				if (i < width)
					usedColumns[i] = 0;

				for (int j = 0; j < width; j++) {
					testData[count][j] = data[index[i]][j];

				}
				count++;
			}

			/////////////////////////////// ========================== sampling
			/////////////////////////////// data and assigning
			/////////////////////////////// weights===================
			// int selectionLength = trainData.length/k;
			double[] prob = new double[trainData.length];
			double[] w = new double[trainData.length];
			boolean[] corrected = new boolean[trainData.length];
			int[][] selectedData = new int[trainData.length][width];
			w = setInitialProbability(prob);

			for (int tree = 0; tree < k; tree++) {
				tp = 0;
				fp = 0;
				tn = 0;
				fn = 0;
				prob = setProb(w);
				selectedData = selectData(trainData, prob);
				for (int s = 0; s < width; s++) {
					usedColumns[s] = 0;
				}
				treeNode root = ID3(selectedData, usedColumns, width, trainLength, 2);

				for (int i = 0; i < trainLength; i++) {
					int decision = 0;
					boolean decisionTaken = false;
					treeNode temp = root;
					int treeDepth = 2;
					while (decisionTaken == false) {
						if (treeDepth == 0) {

							decisionTaken = true;
							// decision=temp.decisionValue;
						}
						if (temp.children == null) {
							decision = temp.decisionValue;
							decisionTaken = true;

						}

						else {
							int nodeVal = trainData[i][temp.attrNum] - 1;

							temp = temp.children.get(nodeVal);

						}
						if (decisionTaken == true) {
							// System.out.print(decision+" ");
							if (decision == 0 && trainData[i][width - 1] == 0) {
								tp++;
								corrected[i] = true;
							} else if (decision == 0 && trainData[i][width - 1] == 1) {
								fp++;
								corrected[i] = false;
							} else if (decision == 1 && trainData[i][width - 1] == 1) {
								tn++;
								corrected[i] = true;
							} else {
								fn++;
								corrected[i] = false;
							}

							break;
						}
						treeDepth--;
					}
				}

				double error = countTotalError(prob, corrected);
				double beta = error * 1.0 / (1 - error);
				w = updateW(w, beta, corrected);
				root.weight = Math.log(1 / beta);
				System.out.println("\ntp = " + tp + ", tn = " + tn + ", fp = " + fp + ", fn =  " + fn);
				if (error <= .5) {

					All_hypothesis_Trees.add(root);
				} else
					break;
			}

		}

		/////////////////////////////////////////// checking on test
		/////////////////////////////////////////// data==============
		System.out.println("\nResults in test data: ");
		tp = 0;
		fp = 0;
		tn = 0;
		fn = 0;
		for (int i = 0; i < testData.length; i++) {

			double class_zero_value = 0;
			double class_one_value = 0;
			for (int j = 0; j < All_hypothesis_Trees.size(); j++) {
				treeNode root = All_hypothesis_Trees.get(j);

				int decision = 0;
				boolean decisionTaken = false;
				treeNode temp = root;
				int treeDepth = 2;
				while (decisionTaken == false) {
					if (treeDepth == 0) {

						decisionTaken = true;
						// decision=temp.decisionValue;
					}
					if (temp.children == null) {
						decision = temp.decisionValue;
						decisionTaken = true;

					}

					else {
						int nodeVal = testData[i][temp.attrNum] - 1;

						temp = temp.children.get(nodeVal);

					}
					if (decisionTaken == true) {

						if (decision == 0)
							class_zero_value += root.weight;
						else
							class_one_value += root.weight;
						break;
					}
					treeDepth--;
				}
			}
			int decision = 1;
			if (class_zero_value >= class_one_value) {
				decision = 0;
			}
			// System.out.print(decision+" ");
			if (decision == 0 && testData[i][width - 1] == 0) {
				tp++;
			} else if (decision == 0 && testData[i][width - 1] == 1) {
				fp++;
			} else if (decision == 1 && testData[i][width - 1] == 1) {
				tn++;
			} else {
				fn++;
			}

			// }

		}

		tp = tp * 1.00 / iteration;
		fp = fp * 1.00 / iteration;
		tn = tn * 1.00 / iteration;
		fn = fn * 1.00 / iteration;
		System.out.println("\ntp = " + tp + ", tn = " + tn + ", fp = " + fp + ", fn =  " + fn + "\n");
		accuracy = (tp + tn) * 100.00 / (tp + fp + tn + fn);
		recall = tp * 100.00 / (tp + fn);
		precision = tp * 100.00 / (tp + fp);
		gMean = Math.sqrt(precision * recall);
		fMeasure = 2.00 * precision * recall / (precision + recall);
		// System.out.println("Number of iterations : " + iteration);
		System.out.println(
				"Accuracy percentage = " + accuracy + "\nPrecision percentage = " + precision + "\nRecall percentage = "
						+ recall + "\nFMeasure percentage = " + fMeasure + "\nGMean percentage = " + gMean);

	}

	static double[] updateW(double[] w, double beta, boolean[] corrected) {
		for (int i = 0; i < w.length; i++) {

			if (corrected[i] == true)
				w[i] = (w[i]) * (beta);
			else
				w[i] = w[i];

		}

		return w;
	}

	static double[] setProb(double[] w) {
		double sum = 0;
		double[] pr = new double[w.length];
		for (int i = 0; i < w.length; i++)
			sum = sum + w[i];
		for (int i = 0; i < w.length; i++)
			pr[i] = w[i] * 1.0 / sum;
		return pr;
	}

	static double countTotalError(double[] prob, boolean[] corrected) {

		double val = 0;

		for (int i = 0; i < prob.length; i++)
			if (corrected[i] == false)
				val = val + prob[i];
		return val;

	}

	static double[] setInitialProbability(double[] prob) {
		for (int i = 0; i < prob.length; i++) {
			double val = 1.00 / prob.length;
			prob[i] = val;
		}

		return prob;
	}

	static int[][] selectData(int[][] trainData, double[] prob) {
		int width = trainData[0].length;
		int[][] newArray = new int[trainData.length][width];
		int selected = 0;
		Random rand = new Random();
		for (int i = 0; i < trainData.length; i++) {
			double sum = 0;
			double r = rand.nextDouble();
			for (int j = 0; j < trainData.length; j++) {
				sum = sum + prob[j];
				if (r < sum) {

					newArray[selected] = trainData[j];
					// System.out.print(j+" ");
					selected++;
					break;
				}

			}

		}
		// System.out.println("\n"+selected);
		// System.out.println();
		return newArray;
	}

	public static int[] RandomizeArray(int[] array) {
		Random rgen = new Random(); // Random number generator

		for (int i = 0; i < array.length; i++) {
			int randomPosition = rgen.nextInt(array.length);
			int temp = array[i];
			array[i] = array[randomPosition];
			array[randomPosition] = temp;
		}

		return array;
	}

	static treeNode ID3(int[][] data, int[] usedColumns, int width, int dim, int treeDepth) {
		//
		treeNode root = new treeNode(null, null, -1, -1, -1);
		int numClass0 = 0, numClass1 = 0;
		boolean allTrue = false, allFalse = true;
		for (int i = 0; i < dim; i++) {

			if (data[i][width - 1] == 1) {
				allTrue = true;
				numClass1++;

			} else if (data[i][width - 1] == 0) {
				allFalse = false;
				numClass0++;
			}
		}

		if (allTrue == false) {

			root.decisionValue = 0;
			return root;
		} else if (allFalse == true) {

			root.decisionValue = 1;
			return root;

		}
		int count = 0;
		for (int i = 0; i < usedColumns.length; i++) {
			if (usedColumns[i] == 1)
				count++;

		}
		if (count == usedColumns.length - 1) {

			if (numClass0 >= numClass1) {
				root.decisionValue = 0;
				return root;
			} else {
				root.decisionValue = 1;
				return root;

			}
		}
		int mostCommon = 0;
		if (numClass1 > numClass0)
			mostCommon = 1;
		double p = 0;
		p = numClass1 * 1.00 / (numClass1 + numClass0);

		double parentEntropy = -1.00 * p * (Math.log(p) / Math.log(2))
				- (1.00 - p) * (Math.log(1.00 - p) / Math.log(2));

		double prevGain = 0;
		int attr = 0;
		for (int i = 0; i < width - 1; i++) {
			if (usedColumns[i] == 0) {
				double gain = parentEntropy - Entropy(data, dim, usedColumns, width, i);
				if (gain >= prevGain) {
					prevGain = gain;
					attr = i;

				}
			}

		}
		root.attrNum = attr;

		usedColumns[attr] = 1;
		ArrayList<treeNode> c = new ArrayList<treeNode>();
		root.children = c;
		for (int i = 0; i < width; i++) {
			int newCount = 0;
			int[] newSet = new int[dim];

			for (int j = 0; j < dim; j++) {

				if (data[j][attr] == i + 1) {
					newSet[newCount] = j;
					newCount++;

				}
			}
			if (newCount == 0) {
				// if(treeDepth>=0)
				treeNode a = new treeNode(root, null, attr, i, mostCommon);

				a.parent = root;
				a.decisionValue = mostCommon;

				c.add(a);

			} else {
				int[][] trainData = new int[newCount][width];
				for (int x = 0; x < newCount; x++) {

					for (int y = 0; y < width; y++) {
						trainData[x][y] = data[newSet[x]][y];
					}
				}
				if (treeDepth >= 0) {
					treeNode b = ID3(trainData, usedColumns, width, newCount, treeDepth - 1);

					b.parent = root;

					c.add(b);

				}

			}

		}

		return root;

	}

	static double Entropy(int[][] data, int dim, int[] usedColumns, int width, int attrIndex) {

		double entropy = 0;
		double count = 0;
		double[][] val = new double[10][2];
		for (int i = 0; i < dim; i++) {
			{
				count++;
				if (data[i][attrIndex] == 1) {
					if (data[i][width - 1] == 0)
						val[0][0]++;
					else if (data[i][width - 1] == 1)
						val[0][1]++;
				}

				else if (data[i][attrIndex] == 2) {
					if (data[i][width - 1] == 0)
						val[1][0]++;
					else if (data[i][width - 1] == 1)
						val[1][1]++;
				} else if (data[i][attrIndex] == 3) {
					if (data[i][width - 1] == 0)
						val[2][0]++;
					else if (data[i][width - 1] == 1)
						val[2][1]++;
				} else if (data[i][attrIndex] == 4) {
					if (data[i][width - 1] == 0)
						val[3][0]++;
					else if (data[i][width - 1] == 1)
						val[3][1]++;
				} else if (data[i][attrIndex] == 5) {
					if (data[i][width - 1] == 0)
						val[4][0]++;
					else if (data[i][width - 1] == 1)
						val[4][1]++;
				} else if (data[i][attrIndex] == 6) {
					if (data[i][width - 1] == 0)
						val[5][0]++;
					else if (data[i][width - 1] == 1)
						val[5][1]++;
				} else if (data[i][attrIndex] == 7) {
					if (data[i][width - 1] == 0)
						val[6][0]++;
					else if (data[i][width - 1] == 1)
						val[6][1]++;
				} else if (data[i][attrIndex] == 8) {
					if (data[i][width - 1] == 0)
						val[7][0]++;
					else if (data[i][width - 1] == 1)
						val[7][1]++;
				} else if (data[i][attrIndex] == 9) {
					if (data[i][width - 1] == 0)
						val[8][0]++;
					else if (data[i][width - 1] == 1)
						val[8][1]++;
				} else if (data[i][attrIndex] == 10) {
					if (data[i][width - 1] == 0)
						val[9][0]++;
					else if (data[i][width - 1] == 1)
						val[9][1]++;
				}
			}

		}

		for (int i = 0; i < 10; i++) {
			if (val[i][0] > 0 && val[i][1] > 0) {
				double p = val[i][0] * 1.00 / (val[i][0] + val[i][1]);
				double e = -1 * p * (Math.log(p) / Math.log(2)) - (1 - p) * (Math.log(1 - p) / Math.log(2));
				entropy = entropy + e * (val[i][0] + val[i][1]) / count;
			}

		}

		return entropy;
	}

}
