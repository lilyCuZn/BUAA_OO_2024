#include<stdio.h>
#include<stdlib.h>
#include<math.h>
#include<ctype.h>
#include<string.h>
#include<time.h>
#include<malloc.h>

int main() {
	FILE *output;
	output = fopen("input6.txt", "w");
	int i, j;
	fprintf(output, "ln 100\n");
	for (i = 0; i < 100; i++) {
		fprintf(output, "%d ", i);
	}
	fprintf(output, "\n");
	for (i = 0; i < 100; i++) {
		fprintf(output, "%d ", i);
	}
	fprintf(output, "\n");
	for (i = 1; i <= 100; i++) {
		fprintf(output, "%d ", i);
	}
	fprintf(output, "\n");
	//加了一百个人
	for (i = 1; i <= 99; i++) {
		for (j = 0; j < i; j++) {
			fprintf (output, "100 ");
		}
		fprintf (output, "\n");
	} 
	for (i = 0; i < 100; i++) {
		for (j = i; j < 100; j++) {
			if (i != j)
			{
				fprintf(output, "mr %d %d -200\n", i, j);
			}
		}
	}
	return 0;
}
