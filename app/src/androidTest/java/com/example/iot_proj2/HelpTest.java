package com.example.iot_proj2;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;

import junit.framework.TestCase;

import org.junit.Test;

public class HelpTest extends TestCase {

    private String name = "Test Name";
    private String email = "iotgrp2023@gmail.com";
    private String comment = "This is a test comment";

    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testHelpIfEmailSendsSuccessfully()
    {
        Intents.init();
        ActivityScenario<Help> scenario = ActivityScenario.launch(Help.class);
        Espresso.onView(withId(R.id.edtHelpName)).perform(typeText(name));
        // Close Keyboard
        Espresso.closeSoftKeyboard();
        //Type Password
        Espresso.onView(withId(R.id.edtHelpEmail)).perform(typeText(email));
        //Close Keyboard
        Espresso.closeSoftKeyboard();
        //Type comment
        Espresso.onView(withId(R.id.edtHelpComment)).perform(typeText(comment));
        //Close Keyboard
        Espresso.closeSoftKeyboard();
       // Attempt Send Email
        Espresso.onView(withId(R.id.btnHelpSubmit)).perform(click());
        intended(IntentMatchers.hasComponent(MainActivity.class.getName()));
        Intents.release();
    }

    public void tearDown() throws Exception {
        //Intents.release();
    }
}