package com.mnmlist.fjssp.logic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.mnmlist.fjssp.data.ProblemInfo;
/**
 * @author mnmlist@163.com
 * @blog http://blog.csdn.net/mnmlist
 * @version v1.0
 * first:generate all the parameters of the scheduling problem from problem description file
 * second:generate all the machine sequence for the DNA,there are three types,namely by local
 * search,global search,random search
 */
public class InitProblemDescription
{
	public static void main(String args[])
	{
		File file=new File("testCases/mk01.txt");
		InitProblemDescription.getProblemDesFromFile(file);
	}
	/**
	 * @param file
	 *            the problem description stored location
	 * @return the problem description which has been arranged
	 */
	public static ProblemInfo getProblemDesFromFile(File file)
	{
		ProblemInfo input = new ProblemInfo();
		BufferedReader reader = getBufferedReader(file);
		String prodesStrArr[] = null;
		int proDesMatrix[][] = null;
		String proDesString;
		int[] operationCountArr = null;
		int[]machineCountArr=null;
		List<Integer> operationCountList = new ArrayList<Integer>();
		try
		{
			proDesString = reader.readLine();
			String proDesArr[] = proDesString.split("\\s+");
			int jobNum = Integer.valueOf(proDesArr[0]);
			operationCountArr=new int[jobNum];
			int machineNum = Integer.valueOf(proDesArr[1]);
			input.setJobCount(jobNum);
			input.setMachineCount(machineNum);
			prodesStrArr = new String[jobNum];
			int count = 0;// caculate how many orders in the problem
			int index = 0;// store the index of first blank
			int maxOperationCount = 0, tempCount = 0;
			// find the max operation count of the job arrays
			for (int i = 0; i < jobNum; i++)
			{
				prodesStrArr[i] = reader.readLine().trim();
				index = prodesStrArr[i].indexOf(' ');
				tempCount = Integer
						.valueOf(prodesStrArr[i].substring(0, index));
				count += tempCount;
				if (maxOperationCount < tempCount)
					maxOperationCount = tempCount;
			}
			int[][] operationToIndex = new int[jobNum][maxOperationCount];// 用来存储i工件j工序所对应的problemDesMatrix[][]的index
			input.setMaxOperationCount(maxOperationCount);
			proDesMatrix = new int[count][];
			String opeationDesArr[];
			int operationCount = 0;
			int operationTotalIndex = 0;
			int selectedMachineCount = 0;
			int machineNo = 0, operationTime = 0;
			proDesMatrix[0] = new int[machineNum];
			for (int i = 0; i < jobNum; i++)
			{
				opeationDesArr = prodesStrArr[i].split("\\s+");
				// the opeartion count of every job
				operationCount = Integer.valueOf(opeationDesArr[0]);
				operationCountArr[i]=operationCount;
				int k = 1;
				for (int j = 0; j < operationCount; j++)
				{
					if (k < opeationDesArr.length)
					{
						selectedMachineCount = Integer
								.valueOf(opeationDesArr[k++]);
						for (int m = 0; m < selectedMachineCount; m++)
						{
							machineNo = Integer.valueOf(opeationDesArr[k++]);
							operationTime = Integer.valueOf(opeationDesArr[k++]);
							proDesMatrix[operationTotalIndex][machineNo - 1] = operationTime;
						}
						// 存储每个工序的备选机器数目
						operationCountList.add(selectedMachineCount);
					}
					// 用来存储i工件j工序所对应的problemDesMatrix[][]的index
					operationToIndex[i][j] = operationTotalIndex;
					operationTotalIndex++;
					if (operationTotalIndex < count)
					{
						proDesMatrix[operationTotalIndex] = new int[machineNum];
					}

				}
			}
			int listSize = operationCountList.size();
			machineCountArr=new int[listSize];
			for (int i = 0; i < listSize; i++)
				machineCountArr[i] = operationCountList.get(i);
			input.setMachineCountArr(machineCountArr);
			input.setProDesMatrix(proDesMatrix);
			input.setTotalOperationCount(proDesMatrix.length);
			input.setOperationToIndex(operationToIndex);
//			for(int i=0;i<operationToIndex.length;i++)
//				System.out.println(Arrays.toString(operationToIndex[i]));
			input.setOperationCountArr(operationCountArr);
			reader.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		//use to store problem description to disk
		//storeProdesInfoToDisk(input,proDesMatrix);
		return input;
	}
	/**
	 * @param file
	 *            a .txt file which contains the time and order information
	 * @return BufferedReader of the file
	 */
	static BufferedReader getBufferedReader(File file)
	{
		BufferedReader reader = null;
		try
		{
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		return reader;
	}
	/**
	 * @param problemInput
	 * 			 the problem description which has been arranged
	 * @return	
	 * 			a feasible solution merely include machine sequence
	 */
	public static int[] localSearch(ProblemInfo problemInput)
	{
		int proDesMatrix[][] = problemInput.getProDesMatrix();
		int machineCount = problemInput.getMachineCount();
		int jobCount = problemInput.getJobCount();
		int machineTimeArr[] = new int[machineCount];
		int dnaLen=proDesMatrix.length*2;
		int machineNoSequence[] = new int[dnaLen];
		int[][] operationToIndex = problemInput.getOperationToIndex();// 某工件工序所对应的index
		int start = 0, end = 0;
		int jobNo = 0;
		while (jobNo < jobCount)
		{
			start = operationToIndex[jobNo][0];
			if (jobNo + 1 < jobCount)
				end = operationToIndex[jobNo + 1][0] - 1;
			else
			{
				end = proDesMatrix.length - 1;
			}
			int minIndex = 0, j = 0, minTime = 0, machineEncode = 0;
			for (int i = start; i <= end; i++)
			{
				j = 0;
				while (j < machineCount && proDesMatrix[i][j] == 0)
					j++;
				minIndex = j;
				minTime = machineTimeArr[j] + proDesMatrix[i][j];
				j++;
				while (j < machineCount)
				{
					if (proDesMatrix[i][j] != 0
							&& machineTimeArr[j] + proDesMatrix[i][j] < minTime)
					{
						minIndex = j;
						minTime = machineTimeArr[j] + proDesMatrix[i][j];
					}
					j++;
				}
				machineTimeArr[minIndex] = minTime;// update the machine time
													// array
				// caculate the machine encode
				j = 0;
				machineEncode = 0;
				while (j <= minIndex)
				{
					if (proDesMatrix[i][j] != 0 && j <= minIndex)
						machineEncode++;
					j++;
				}
				machineNoSequence[i] = machineEncode;// the gene of the
														// machinesequence
			}
			Arrays.fill(machineTimeArr, 0);
			jobNo++;
		}
		return machineNoSequence;
	}

	/**
	 * @param problemInput
	 *            the problem description which has been arranged
	 * @return    a feasible solution merely include machine sequence
	 */
	public static int[] randomSearch(ProblemInfo problemInput)
	{
		int proDesMatrix[][] = problemInput.getProDesMatrix();
		int jobCount = problemInput.getJobCount();
		int dnaLen=proDesMatrix.length*2;
		int machineNoSequence[] = new int[dnaLen];
		int[][] operationToIndex = problemInput.getOperationToIndex();// 某工件工序所对应的index
		int machineCountArr[] = problemInput.getMachineCountArr();
		int start = 0, end = 0;
		int jobNo = 0;
		Random random = new Random();
		int count = 0, index = 0;
		int j = 0;
		while (jobNo < jobCount)
		{
			start = operationToIndex[jobNo][0];
			if (jobNo + 1 < jobCount)
				end = operationToIndex[jobNo + 1][0] - 1;
			else
			{
				end = proDesMatrix.length - 1;
			}
			for (int i = start; i <= end; i++)
			{
				count = random.nextInt(machineCountArr[i]) + 1;
				j = 0;
				index = 0;
				while (j < count)
				{
					if (proDesMatrix[i][index] != 0)
						j++;
					index++;
				}
				index--;
				machineNoSequence[i] = count;
			}
			jobNo++;
		}
		return machineNoSequence;
	}

	/**
	 * @param problemInput
	 *            the problem description which has been arranged
	 * @return    a feasible solution merely include machine sequence
	 */
	public static int[] globalSearch(ProblemInfo problemInput)
	{
		int proDesMatrix[][] = problemInput.getProDesMatrix();
		int machineCount = problemInput.getMachineCount();
		int jobCount = problemInput.getJobCount();
		int machineTimeArr[] = new int[machineCount];
		int dnaLen=problemInput.getTotalOperationCount()*2;
		int machineSequence[] = new int[dnaLen];
		int[][] operationToIndex = problemInput.getOperationToIndex();// 某工件工序所对应的index
		List<Integer> jobNoList = new ArrayList<Integer>();
		for (int i = 0; i < jobCount; i++)
			jobNoList.add(i);
		Random random = new Random();
		int randomIndex = 0;
		int listSize = jobNoList.size();
		int start = 0, end = 0;
		int jobNo = 0;
		while (listSize > 0)
		{
			randomIndex = random.nextInt(listSize);
			jobNo = jobNoList.get(randomIndex);
			start = operationToIndex[jobNo][0];
			if (jobNo != jobCount - 1)
				end = operationToIndex[jobNo + 1][0] - 1;
			else
			{
				end = proDesMatrix.length - 1;
			}
			int minIndex = 0, j = 0, minTime = 0, machineEncode = 0;
			for (int i = start; i <= end; i++)
			{
				j = 0;
				while (j < machineCount && proDesMatrix[i][j] == 0)
					j++;
				minIndex = j;
				minTime = machineTimeArr[j] + proDesMatrix[i][j];
				j++;
				while (j < machineCount)
				{
					if (proDesMatrix[i][j] != 0
							&& machineTimeArr[j] + proDesMatrix[i][j] < minTime)
					{
						minIndex = j;
						minTime = machineTimeArr[j] + proDesMatrix[i][j];

					}
					j++;
				}
				// update the machine time array
				machineTimeArr[minIndex] = minTime;
				// caculate the machine encode
				j = 0;
				machineEncode = 0;
				while (j <= minIndex)
				{
					if (proDesMatrix[i][j] != 0 && j <= minIndex)
						machineEncode++;
					j++;
				}
				// the gene of the machine sequence
				machineSequence[i] = machineEncode;
			}
			jobNoList.remove(randomIndex);
			listSize = jobNoList.size();
		}
		return machineSequence;
	}

	
	/**
	 * @param input the problem description which has been arranged
	 * @param prodesMatrix the problem description which has been arranged
	 */
	public static void storeProdesInfoToDisk(ProblemInfo input,int prodesMatrix[][])
	{
		int operationCountofEveryJobArr[]=input.getOperationCountArr();
		int len=operationCountofEveryJobArr.length;
		int sum=0;
		for(int num:operationCountofEveryJobArr)
			sum+=num;
		System.out.println(sum);
		File file=new File("proDesMatrixPro1.txt");
		try
		{
			BufferedWriter writer=new BufferedWriter(new FileWriter(file));
			int index=0,j=0,i=0;
			for(i=0;i<len;i++)
			{
				for(j=0;j<operationCountofEveryJobArr[i];j++)
					writer.write((index+1)+"-("+i+","+(j+1)+"):"+Arrays.toString(prodesMatrix[index++])+"\n");
			}
			writer.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
