package io.github.data4all.smoothing;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for the BasicSensorSmoother class
 * 
 * @author Steeve
 *
 */
public class BasicSensorSmootherTest {

    /** object of the class which is tested */
    private BasicSensorSmoother basicSmoothing;

    /** array for input values */
    private float[] input;

    /** array for output values */
    private float[] output;

    @Before
    public void setUp() {
        basicSmoothing = new BasicSensorSmoother();
        input = new float[3];
        output = new float[3];
    }

    /**
     * we presume output = null
     */
    @Test
    public void filterTest_WhenOuput_isNull() {
        input[0] = 7.5f;
        input[1] = -2.5f;
        input[2] = 4.1f;
        output = null;
        float[] filteredValues = basicSmoothing.filter(input, output);
        assertEquals(filteredValues, input);
    }

    /**
     * we presume input = null
     */
    @Test
    public void filterTest_WhenInput_isNull() {
        input = null;
        output[0] = 7.5f;
        output[1] = -2.5f;
        output[2] = 4.1f;
        float[] filteredValues = basicSmoothing.filter(input, output);
        assertEquals(filteredValues, output);
    }

    /**
     * we presume that inputs and output are not null and we want to check the
     * filteredValues
     */
    @Test
    public void filterTest_Normal() {
        input[0] = 7.5f;
        input[1] = -2.5f;
        input[2] = 0.0f;
        output[0] = 3.0f;
        output[1] = 2.0f;
        output[2] = 0.0f;
        
        float[] filteredValues = basicSmoothing.filter(input, output);
        //if ALPHA is 0.25
        assertEquals(filteredValues[0], 4.125f, 0.1);
        assertEquals(filteredValues[1], 0.875f, 0.1);
        assertEquals(filteredValues[2], 0.0f, 0.1);
    }

}
