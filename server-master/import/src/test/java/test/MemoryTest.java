package test;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import java.net.UnknownHostException;

import mailinglistonline.server.export.database.DbClient;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author matej
 */
public class MemoryTest {
    
    public MemoryTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
    
    @Test
    public void testClientRemoval() throws UnknownHostException, IOException  {
        DbClient dbClient;
        for(int i=0; i<=1000;i++) {
            //dbClient= new DbClient();
            //dbClient.closeConnection();
        }
    }
}
