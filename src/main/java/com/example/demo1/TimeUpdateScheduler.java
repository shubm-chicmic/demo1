package com.example.demo1;
import java.util.Timer;
import java.util.TimerTask;

public class TimeUpdateScheduler {
    private Timer timer;
    private static String empCode = ""; // Initialize with current empCode
    private UpdateTask updateTask;
    public void startUpdateTask(UpdateTask updateTask) {
        // Stop the current timer task
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        this.updateTask = updateTask;
        // Start the provided timer task
        timer = new Timer();
        timer.schedule(updateTask, 0, 5000);
    }
    public void stopUpdateTask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
            System.out.println("Timer task canceled.");
        }
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
            System.out.println("UpdateTask canceled.");
        }
    }

}
class UpdateTask extends TimerTask {
    private HelloApplication helloApplication; // Reference to the HelloApplication instance
    private String lastPunch = "";
    private String lastPunchId = "";
    private String empCode  = "";
    private String empTime = "";

    public UpdateTask(HelloApplication helloApplication, String empCode, String empTime) {
        this.helloApplication = helloApplication;
        this.empCode = empCode;
        this.empTime = empTime;
    }

    @Override
    public void run() {
        // TODO: Make API request to fetch the array of in and out punches
        TimePunch timePunch = TimePunchApi.timePunchApiCall(empCode);

        // Assuming timePunches is a list of punch records in chronological order
        if (timePunch != null) {
            String currentPunch = timePunch.getPunchType();
            String currentPunchId = timePunch.getId();
            if (currentPunch.equals("IN") && !currentPunchId.equals(lastPunchId)) {
                lastPunch = currentPunch;
                lastPunchId = currentPunchId;
                System.out.println("\u001B[38m" + "values inside scheduler : " + empTime + " " + empCode);
                // Update employee time if last punch is "in"
                String updatedHelloText = HelloApplication.empTimeCal(empCode, empTime);
                helloApplication.updateUIComponents(updatedHelloText);
            }
        }
    }



}

class TimePunch {
    public String getPunchType() {
        return punchType;
    }

    public void setPunchType(String punchType) {
        this.punchType = punchType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private String punchType;
    private String id;
}

