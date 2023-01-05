package info.trekto.jos.core.impl.arbitrary_precision;

import info.trekto.jos.core.Simulation;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.numbers.Number;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

//import static info.trekto.jos.core.impl.arbitrary_precision.SimulationRecursiveAction.threshold;

class CollisionCheckAP {
    private final Semaphore sem2;
    private int fromIndex;
    private int toIndex;
    private final Simulation simulation;
    private final Semaphore sem;
    private final Object lock;
    private long startTime;
    private long finishTime;
    private long timeExec;
    private int currentIt;
    private ArrayList<Long> times = new ArrayList<>();
    private ArrayList<Integer> delPart = new ArrayList<>();
    private ArrayList<Integer> anzdPart = new ArrayList<>();
    private ArrayList<Integer> thIds = new ArrayList<>();
    private long id;
    private long lastPrint = 0;
    private final Lock idlock = new ReentrantLock();
    public CollisionCheckAP(int fromIndex, int toIndex, Semaphore sem, Semaphore ended, Object mu, Simulation simulation) {
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
        this.simulation = simulation;
        this.sem = sem;
        this.sem2 = ended;
        this.lock = mu;
    }
    private void printInfo(){
        //System.out.println("emtrp");
        long timesAdd = times.stream()
                .mapToLong(a -> a)
                .sum();
        long delParts = delPart.stream()
                .mapToLong(a -> a)
                .sum();
        long anzdParts = anzdPart.stream()
                .mapToLong(a -> a)
                .sum();

        System.out.println(" Thread: " + id + " ns " + "Exec Time: " + timesAdd + " Deleted Particles: " + delParts + " Analized Particles: " + anzdParts);
    }
    private void saveStatics(){
        times.add(timeExec);
        if(simulation.getAuxiliaryObjects() != null)
            delPart.add(simulation.getObjects().size() - simulation.getAuxiliaryObjects().size());
        anzdPart.add(toIndex-fromIndex);
        idlock.lock();
        try{
            thIds.add((int) id);
        }finally {
            idlock.unlock();
        }
    }
    public void checkAllCollisions(int userid, int max) throws InterruptedException {
        while(true){
            sem.acquire();
            startTime = System.nanoTime();
            try{
                idlock.lock();
                id = userid;
            }finally {
                idlock.unlock();
            }
            if(simulation.getCurrentIterationNumber() >= currentIt){
                saveStatics();
                currentIt = (int) simulation.getCurrentIterationNumber();
            }
            if(simulation.getCurrentIterationNumber() == lastPrint + 25){
                printInfo();
                lastPrint = simulation.getCurrentIterationNumber();
            }
            synchronized (lock){
                fromIndex = simulation.getFromIndex();
                toIndex = simulation.getToIndex();
            }
            int x = 0;
            if (simulation.isCollisionExists()) {
                sem2.release();
                return;
            }
            //System.out.println("Executing, form index:" + fromIndex + "to index: "+toIndex +"ID " + id);
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
            finishTime = System.nanoTime();
            timeExec = finishTime - startTime;
            sem2.release();
        }
    }
}