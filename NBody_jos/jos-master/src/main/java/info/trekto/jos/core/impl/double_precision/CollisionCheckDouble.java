/* ---------------------------------------------------------------
Práctica 1.
Código fuente : CollisionCheckDouble.java
Grau Informàtica
53923695B ,Faysal Badaoui Mahdad.
--------------------------------------------------------------- */




package info.trekto.jos.core.impl.double_precision;

import com.aparapi.Kernel;

import java.util.Arrays;

public class CollisionCheckDouble extends Kernel {
    public final boolean[] collisionExists;
    public final int n;

    public final double[] positionX;
    public final double[] positionY;
    public final double[] radius;
    public final boolean[] deleted;
    public int Nthreads=0;
    public CollisionCheckDouble(int n, double[] positionX, double[] positionY, double[] radius, boolean[] deleted, int N) {
        this.n = n;
        collisionExists = new boolean[1];

        this.positionX = positionX;
        this.positionY = positionY;
        this.radius = radius;
        this.deleted = deleted;
        this.Nthreads=N;
    }

    public void prepare() {
        collisionExists[0] = false;
    }

    public boolean collisionExists() {
        return collisionExists[0];
    }

    /**
     * !!! DO NOT CHANGE THIS METHOD and methods called from it if you don't have experience with Aparapi library!!!
     * This code is translated to OpenCL and executed on the GPU.
     * You cannot use even simple 'break' here - it is not supported by Aparapi.
     */
    @Override
    public void run() {
        if (collisionExists[0]) {
            return;
        }
        int i = getGlobalId();
        if (!deleted[i]) {
            for (int j = 0; j < n; j++) {
                if (i != j && !deleted[j]) {
                    // distance between centres
                    double x = positionX[j] - positionX[i];
                    double y = positionY[j] - positionY[i];
                    double distance = Math.sqrt(x * x + y * y);

                    if (distance < radius[i] + radius[j]) {
                        collisionExists[0] = true;
                        return;
                    }
                }
            }
        }
    }

    public void calculateCollision(int start, int finish){
        for (int i = start; i < finish; i++) {
            if (!deleted[i]) {
                for (int j = 0; j < n; j++) {
                    if (i != j && !deleted[j]) {
                        // distance between centres
                        double x = positionX[j] - positionX[i];
                        double y = positionY[j] - positionY[i];
                        double distance = Math.sqrt(x * x + y * y);

                        if (distance < radius[i] + radius[j]) {
                            collisionExists[0] = true;
                            return;
                        }
                    }
                }
            }
        }
    }
    public void checkAllCollisions() {
        if (collisionExists[0]) {
            return;
        }
        Thread[] th = new Thread[Nthreads];
        int control_start=0;
        int control_end=0;
        int MaxIndex = ((positionX.length));
        int NumIndexes = (MaxIndex / Nthreads);
        for (int h = 0; h < Nthreads; h++){
            int StartIndex, FinishIndex;
            StartIndex = (int)((NumIndexes * h)+control_start);
            FinishIndex = (int)((NumIndexes * (h + 1))+control_end);
            if ((MaxIndex % Nthreads) > h) {
                control_end++;
                FinishIndex += control_end;
            }
            System.out.println("Fill : " + h + " "+StartIndex +" "+ FinishIndex);
            int finalStartIndex = StartIndex;
            int finalFinishIndex = FinishIndex;
            th[h] = new Thread(() -> calculateCollision(finalStartIndex, finalFinishIndex));
            th[h].start();
            control_start=control_end;
        }

    }

}
