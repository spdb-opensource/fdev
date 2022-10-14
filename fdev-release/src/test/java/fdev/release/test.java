package fdev.release;

import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

public class test {
private static UsernamePasswordCredentialsProvider provider = 
new UsernamePasswordCredentialsProvider("xxx",
		"xxx");


	public static void main(String[] args) {
		int i =0;
		int j =0;
		boolean diff = false;
		while (i < Integer.MAX_VALUE) {
			while (j < Integer.MAX_VALUE) {
				if( ( (i == j) && ((double)i != (double)j )) ){
					diff = true;
					System.out.println(" Different at: " + i + " " + j);
					System.out.println(" Different at double: " + (double)i + " " + (double)j);
					break;
				}
				if(j < Integer.MAX_VALUE)
					j++;
			}
			System.out.println("i = " + i);
			if ( i > 10000 )  {
				System.out.println("i = " + i);
				break;
			}
			if ( diff )
				break;
			j = 0;
			if(i < Integer.MAX_VALUE)
				i++;
		}
		if (diff)
			System.out.println("found " + diff + " different");
	}
}
