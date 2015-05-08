/*
 * Copyright 2015, Inocybe Technologies
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This allows you to keep a single queue of all unprocessed intents irrespective of the number of
 * threads running concurrently. As intents get processed, we pull them from this queue.
 * If an update is triggered, the intent already on the queue is pulled out and replaced. If the
 * intent has already been processed, it will get queued up once again for processing.
 */

public class IntentRunningRegistry {
    protected static final Logger LOG =
            LoggerFactory.getLogger(SingletonTask.class);

    private static IntentRunningRegistry instance = null;
    private BlockingQueue<Intent> intentQueue = new LinkedBlockingQueue<Intent>();

    protected IntentRunningRegistry(){

    }

    public static IntentRunningRegistry getInstance(){
        if(instance == null){
            synchronized(IntentRunningRegistry.class){
                instance = new IntentRunningRegistry();
            }
        }

        LOG.debug("Created a new intent registry: "  + instance);
        return instance;
    }

    public int getAvailableIntents(){
        return intentQueue.size();
    }

    /**
     * Returns the next intent in the queue
     * @return
     * @throws InterruptedException
     */
    public Intent getNext() throws InterruptedException{
        return intentQueue.take();
    }

    /**
     * Adds a new intent
     * @param intent
     * @throws InterruptedException
     */
    public void addIntent(Intent intent) throws InterruptedException{
        if(getMatchingIntent(intent) == null)
            intentQueue.put(intent);
    }


    public void removeIntent(Intent intent){
        intentQueue.remove(this.getMatchingIntent(intent));
    }


    public void removeAllIntents(){
        intentQueue.clear();
    }

    /**
     * Updates an existing intent  by removing the existing copy and
     * replacing it with the new update. If the intent has already been
     * processed, then it is queued up for processing
     * @param newIntent
     * @throws InterruptedException
     */
    public void updateIntent(Intent newIntent) throws InterruptedException{
        this.removeIntent(newIntent);
        this.addIntent(newIntent);
    }

    /**
     * Checks if an intent is already in the queue
     * @param intent
     * @return
     */
    private Intent getMatchingIntent(Intent intent){
        for(Intent i : intentQueue){
            if(intent.getId().toString().equalsIgnoreCase(i.getId().toString()))
                return i;
        }

        return null;
    }
}
