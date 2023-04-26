package com.example.iot_proj2;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;

import junit.framework.TestCase;

import org.junit.Test;

public class MainActivityTest extends TestCase {


    public void setUp() throws Exception {
        super.setUp();
       // Intents.init();
    }

    @Test
    public void testGoingToHelpActivity()
    {
        Intents.init();
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        Espresso.onView(withId(R.id.imgHelp)).perform(click());
        intended(IntentMatchers.hasComponent(Help.class.getName()));
        Intents.release();
    }

    public void tearDown() throws Exception {
       // Intents.release();
    }
}