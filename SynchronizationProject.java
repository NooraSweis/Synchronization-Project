package synchronizationproject;

import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import static synchronizationproject.SynchronizationProject.jenin_students;
import static synchronizationproject.SynchronizationProject.nablus_students;
import static synchronizationproject.SynchronizationProject.ramallah_students;

/**
 *
 * @author Noora Sweis
 */
public class SynchronizationProject {

    /*
    * The Three Queues below used to store students who are waiting for a car
    * depending on their destination
    * used in both classes: Student(to add them) and Park(to remove travelled ones)
     */
    public static MyQueue<Student> jenin_students = new MyQueue();
    public static MyQueue<Student> nablus_students = new MyQueue();
    public static MyQueue<Student> ramallah_students = new MyQueue();

    public static void main(String[] args) {
        Park park = new Park();
        Car car = new Car(park);
        Student student = new Student(park);
    }

}

/*
* Class Park:
* manages the list of students based on the destination they wish to travel to, add and remove.
* manages the cars based on the destination to keep track of number of cars,
* add the car when waiting and remove from the list when leaving
 */
class Park {

    // Semaphore park ensures that cars cannot enter the park at the same moment
    Semaphore park = new Semaphore(1, true);

    /* The three semaphores below to ensure that only 10 cars for each destination can stand in the park
    *  and other must wait until one of them be moved
     */
    Semaphore jenin_sem = new Semaphore(10, true);
    Semaphore nablus_sem = new Semaphore(10, true);
    Semaphore ramallah_sem = new Semaphore(10, true);

    // cars_in_park LinkedList will store the cars which already exists in the park
    public LinkedList<Car> cars_in_park = new LinkedList<>();

    /* The three semaphores below to ensure that only one car for each destination can take students
    *  and other must wait until it is filled
     */
    Semaphore jenin_car = new Semaphore(1);
    Semaphore nablus_car = new Semaphore(1);
    Semaphore ramallah_car = new Semaphore(1);

    public Park() {
    }

    public void enterPark(Car car) throws InterruptedException {
        try {
            park.acquire(); // if a car entered the critical section, other cars will wait until the park semaphore released
            /*
            * depending on the destination, its semaphore will be aquired 
            * to prevent more than 10 cars enters the park for each destination:
             */
            switch (car.destination) {
                case "jenin":
                    jenin_sem.acquire();
                    break;
                case "nablus":
                    nablus_sem.acquire();
                    break;
                case "ramallah":
                    ramallah_sem.acquire();
                    break;
            }
            System.out.println("number of cars in the park = " + cars_in_park.size()
                    + ", " + (10 - jenin_sem.availablePermits()) + " to Jenin, "
                    + (10 - nablus_sem.availablePermits()) + " to Nablus, "
                    + (10 - ramallah_sem.availablePermits()) + " to Ramallah\n");
        } catch (InterruptedException e1) {
            System.out.println("InterruptedException in class Park");
        }

        cars_in_park.add(car); // car entered the park and will be added to the LinkedList of standing cars
        System.out.println("Car parked with destination to " + car.destination + " and capacity of " + car.capacity);
        checkStudents();
        park.release(); // releases (signals) this semaphore will allow one of the waiting cars to enter the critical section (the park)
    }

    /*
    * checkStudents method manages the list of students based on their destination
    * remove them from the Queues if they went up a car
     */
    public void checkStudents() throws InterruptedException {
        for (int i = 0; i < cars_in_park.size(); i++) {
            Car c = cars_in_park.get(i);
            switch (c.destination) {
                case "jenin":
                    jenin_car.acquire();
                    if (jenin_students.size() >= c.capacity && cars_in_park.contains(c)) {
                        for (int j = 0; j < c.capacity; j++) {
                            jenin_students.deque();
                        }
                        jenin_sem.release();
                        cars_in_park.remove(c);
                        System.out.println("Car moved to " + c.destination + " with " + c.capacity + " students");
                    }
                    jenin_car.release();
                    break;
                case "nablus":
                    nablus_car.acquire();
                    if (nablus_students.size() >= c.capacity && cars_in_park.contains(c)) {
                        for (int j = 0; j < c.capacity; j++) {
                            nablus_students.deque();
                        }
                        nablus_sem.release();
                        cars_in_park.remove(c);
                        System.out.println("Car moved to " + c.destination + " with " + c.capacity + " students");
                    }
                    nablus_car.release();
                    break;
                case "ramallah":
                    ramallah_car.acquire();
                    if (ramallah_students.size() >= c.capacity && cars_in_park.contains(c)) {
                        for (int j = 0; j < c.capacity; j++) {
                            ramallah_students.deque();
                        }
                        ramallah_sem.release();
                        cars_in_park.remove(c);
                        System.out.println("Car moved to " + c.destination + " with " + c.capacity + " students");
                    }
                    ramallah_car.release();
                    break;
            }
        }
    }
}

/*
* Class car stores the destination and the capacity for each car,
* starts 6 Threads to create cars of different kinds:
* Jenin(16 with 7 seats and 11 with four seats)
* Nablus(25 with 7 seats and 11 with four seats)
* Ramallah(12 with 7 seats and 5 with four seats)
* and sends them to the park...
 */
class Car implements Runnable {

    public int capacity;
    public String destination;
    public Park park;

    public Car() {
    }

    public Car(Park park) {
        this.park = park;
        new Thread(this, "jenin7").start();
        new Thread(this, "jenin4").start();
        new Thread(this, "nablus7").start();
        new Thread(this, "nablus4").start();
        new Thread(this, "ramallah7").start();
        new Thread(this, "ramallah4").start();
    }

    public Car(int capacity, String destination) {
        this.capacity = capacity;
        this.destination = destination;
    }

    @Override
    public void run() {
        try {
            switch (Thread.currentThread().getName()) {
                case "jenin7":
                    for (int i = 0; i < 16; i++) {
                        park.enterPark(new Car(7, "jenin"));
                        Thread.sleep(1000);
                    }
                    break;
                case "jenin4":
                    for (int i = 0; i < 11; i++) {
                        park.enterPark(new Car(4, "jenin"));
                        Thread.sleep(1000);
                    }
                    break;
                case "nablus7":
                    for (int i = 0; i < 25; i++) {
                        park.enterPark(new Car(7, "nablus"));
                        Thread.sleep(1000);
                    }
                    break;
                case "nablus4":
                    for (int i = 0; i < 11; i++) {
                        park.enterPark(new Car(4, "nablus"));
                        Thread.sleep(1000);
                    }
                    break;
                case "ramallah7":
                    for (int i = 0; i < 12; i++) {
                        park.enterPark(new Car(7, "ramallah"));
                        Thread.sleep(1000);
                    }
                    break;
                case "ramallah4":
                    for (int i = 0; i < 5; i++) {
                        park.enterPark(new Car(4, "ramallah"));
                        Thread.sleep(1000);
                    }
                    break;
            }
        } catch (InterruptedException ex) {
            System.out.println("Interrupted Exception in class Car");
        }
    }
}

/*
* Class Student stores the destination of each student 
* and starts a three threads which manages the enterence of: 
* 103 students from Jenin, 213 from Nablus and 57 from Ramallah
 */
class Student implements Runnable {

    public String destination;
    public Park park;

    public final int JENIN_STUDENTS_NUMBER = 103;
    public final int NABLUS_STUDENTS_NUMBER = 213;
    public final int RAMALLAH_STUDENTS_NUMBER = 57;

    public Student(String destination) {
        this.destination = destination;
    }

    public Student(Park park) {
        this.park = park;
        new Thread(this, "jenin").start();
        new Thread(this, "nablus").start();
        new Thread(this, "ramallah").start();
    }

    @Override
    public void run() {
        try {
            switch (Thread.currentThread().getName()) {
                case "jenin":
                    for (int i = 0; i < JENIN_STUDENTS_NUMBER; i++) {
                        jenin_students.enque(new Student("jenin"));
                        Thread.sleep(500);
                        park.checkStudents();
                    }
                    break;
                case "nablus":
                    for (int i = 0; i < NABLUS_STUDENTS_NUMBER; i++) {
                        nablus_students.enque(new Student("nablus"));
                        Thread.sleep(500);
                        park.checkStudents();
                    }
                    break;
                case "ramallah":
                    for (int i = 0; i < RAMALLAH_STUDENTS_NUMBER; i++) {
                        ramallah_students.enque(new Student("ramallah"));
                        Thread.sleep(500);
                        park.checkStudents();
                    }
                    break;
            }
        } catch (InterruptedException ex) {
            System.out.println("Interrupted Exception in class Student");
        }
    }
}

/* Class MyQueue is a Data Structure used instead of ready-made Queue class in Java, 
*  to save students who are waiting for cars, depending on their destination
 */
class MyQueue<E> {

    LinkedList<E> a = new LinkedList<>();

    public MyQueue() {
    }

    public MyQueue(E[] array) {
        for (E array1 : array) {
            a.addLast(array1);
        }
    }

    public void enque(E e) {
        a.addLast(e);
    }

    public E deque() {
        E e = a.removeFirst();
        return e;
    }

    public E peek() {
        return a.get(0);
    }

    public int size() {
        return a.size();
    }
}

