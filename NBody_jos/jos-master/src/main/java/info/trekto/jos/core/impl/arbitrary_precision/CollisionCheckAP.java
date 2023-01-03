package info.trekto.jos.core.impl.arbitrary_precision;

import info.trekto.jos.core.Simulation;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.numbers.Number;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.Semaphore;

//import static info.trekto.jos.core.impl.arbitrary_precision.SimulationRecursiveAction.threshold;

class CollisionCheckAP {
    private final Semaphore sem2;
    private int fromIndex;
    private int toIndex;
    private final Simulation simulation;
    private final Semaphore sem;

    public CollisionCheckAP(int fromIndex, int toIndex, Semaphore sem, Semaphore ended, Simulation simulation) {
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
        this.simulation = simulation;
        this.sem = sem;
        this.sem2 = ended;
    }

    public void checkAllCollisions() throws InterruptedException {

        while(true){
            sem.acquire();
            fromIndex = simulation.getFromIndex();
            toIndex = simulation.getToIndex();
            int x = 0;
            if (simulation.isCollisionExists()) {
                sem2.release();
                return;
            }
            System.out.println("Executing, form index:" + fromIndex + "to index: "+toIndex);
            for (SimulationObject object : simulation.getAuxiliaryObjects().subList(fromIndex, toIndex)) {

                if (simulation.isCollisionExists()) {
                    break;
                }
                for (SimulationObject object1 : simulation.getAuxiliaryObjects()) {
                    if (object == object1) {
                        continue;
                    }
                    // distance between centres
                    Number distance = simulation.calculateDistance(object, object1);

                    if (distance.compareTo(object.getRadius().add(object1.getRadius())) <= 0) {
                        simulation.upCollisionExists();
                        break;
                    }
                }
            }
            sem2.release();
        }
    }
}