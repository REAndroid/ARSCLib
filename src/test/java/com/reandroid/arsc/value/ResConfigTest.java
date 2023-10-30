package com.reandroid.arsc.value;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class ResConfigTest {

    @Test
    public void testResConfig(){
        for(String qualifier : QUALIFIERS){
            ResConfig resConfig = new ResConfig();
            String[] errors = resConfig.parseQualifiers(qualifier);

            Assert.assertNull("Qualifier = " + qualifier
                    + ", error " + Arrays.toString(errors)
                    + ", parsed = " + resConfig, errors);

            Assert.assertEquals(qualifier, resConfig.getQualifiers());
        }
        for(String qualifier : ERROR_QUALIFIERS){
            ResConfig resConfig = new ResConfig();
            String[] errors = resConfig.parseQualifiers(qualifier);

            Assert.assertNotNull("Error qualifier = " + qualifier
                    + ", parsed = " + resConfig, errors);

        }
    }

    private static final String[] QUALIFIERS = new String[]{
            "",
            "-en",
            "-de-rDE",
            "-mcc10-mnc20",
            "-xxhdpi",
            "-180dpi",
            "-480x1024",
            "-v26",
            "-12key",
            "-dpad",
            "-normal",
            "-night",
            "-long",
            "-neuter",
            "-feminine",
            "-masculine",
            "-mcc10-mnc20-en-rUS-xxhdpi-480x1024-v26-long-night-sw180dp-h480dp-"
                    + ResConfig.UNKNOWN_BYTES + "001a2b34",
    };
    private static final String[] ERROR_QUALIFIERS = new String[]{
            "something",
            "-abcd",
            "1234",
            "a b",
            "-+",
            "-values",
            "-anim"
    };
}
