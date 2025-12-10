package es.upm.grise.profundizacion.grupo.aa;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AdderTest {
    private Adder adder;

    @BeforeEach
    void setup(){
        adder = new Adder();
    }


    @Test
    void addOneOne() {
        assertEquals(2, adder.add(1,1));
        assertEquals(3, adder.add(2,1));
        assertEquals(4, adder.add(3,1));
    }
}