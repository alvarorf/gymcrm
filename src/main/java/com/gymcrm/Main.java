package com.gymcrm;

import com.gymcrm.facade.GymFacade;
import com.gymcrm.util.CrmDemoRunner;


public class Main {
    public static void main(String[] args) {
        // Initialize the Facade (which handles Spring Context internally)
        GymFacade gym = new GymFacade();

        // In a real Spring app, we would get this from the context,
        // but for this demo, we instantiate the runner.
        CrmDemoRunner demoRunner = new CrmDemoRunner();
        demoRunner.runDemo(gym);
    }
}