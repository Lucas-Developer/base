package org.safehaus.subutai.plugin.spark.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.plugin.spark.impl.handler.StartNodeOperationHandler;
import org.safehaus.subutai.plugin.spark.impl.mock.SparkImplMock;

public class StartNodeOperationHandlerTest {

    private SparkImplMock mock;
    private AbstractOperationHandler handler;

    @Before
    public void setUp() {
        mock = new SparkImplMock();
        handler = new StartNodeOperationHandler( mock, "test-cluster", "test-host", true );
    }

    @Test
    public void testWithoutCluster() {

        handler.run();

        ProductOperation po = handler.getProductOperation();
        Assert.assertTrue( po.getLog().contains( "not exist" ) );
        Assert.assertEquals(po.getState(), ProductOperationState.FAILED);
    }

    @Test
    public void testFail() {
        mock.setClusterConfig( new SparkClusterConfig() );
        handler.run();

        ProductOperation po = handler.getProductOperation();
        Assert.assertTrue(po.getLog().contains("not connected"));
        Assert.assertEquals(po.getState(), ProductOperationState.FAILED);
    }
}