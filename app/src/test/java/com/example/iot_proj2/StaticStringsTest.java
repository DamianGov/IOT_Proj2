package com.example.iot_proj2;


import org.junit.Test;
import static org.junit.Assert.*;


public class StaticStringsTest {

    @Test
    public void testHashingPasswordMethodForCorrectOutput()
    {
        assertEquals("x}gsGY", StaticStrings.hashPassword("123456"));
        assertNotEquals("y^jkaqw", StaticStrings.hashPassword("P@ssword01"));
    }
}