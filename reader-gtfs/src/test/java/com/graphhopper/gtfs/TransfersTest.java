/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.graphhopper.gtfs;

import com.conveyal.gtfs.GTFSFeed;
import com.conveyal.gtfs.model.Transfer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.github.javafaker.Faker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TransfersTest {

    private Transfers sampleFeed;
    private Transfers anotherSampleFeed;

    @BeforeAll
    public void init() throws IOException {
        GTFSFeed gtfsFeed1 = new GTFSFeed();
        gtfsFeed1.loadFromZipfileOrDirectory(new File("files/sample-feed"), "");
        sampleFeed = new Transfers(gtfsFeed1);
        GTFSFeed gtfsFeed2 = new GTFSFeed();
        gtfsFeed2.loadFromZipfileOrDirectory(new File("files/another-sample-feed"), "");
        anotherSampleFeed = new Transfers(gtfsFeed2);
    }

    @Test
    public void testTransfersByToRoute() {
        assertTrue(anotherSampleFeed.hasNoRouteSpecificArrivalTransferRules("MUSEUM"), "Transfer model says we don't have route-dependent arrival platform at from-stop");
        assertTrue(anotherSampleFeed.hasNoRouteSpecificDepartureTransferRules("NEXT_TO_MUSEUM"), "Transfer model says we don't have route-dependent departure platform at to-stop");
        List<Transfer> transfersToStop = anotherSampleFeed.getTransfersToStop("NEXT_TO_MUSEUM", null);
        assertEquals(2, transfersToStop.size());
        Transfer transfer = transfersToStop.get(0);
        assertEquals("MUSEUM", transfer.from_stop_id);
        assertEquals("NEXT_TO_MUSEUM", transfer.to_stop_id);
        Assertions.assertNull(transfer.from_route_id);
        Assertions.assertNull(transfer.to_route_id);
        Assertions.assertEquals(600, transfer.min_transfer_time);

        Transfer withinStationTransfer = transfersToStop.get(1);
        assertEquals("NEXT_TO_MUSEUM", withinStationTransfer.from_stop_id);
        assertEquals("NEXT_TO_MUSEUM", withinStationTransfer.to_stop_id);
        assertNull(withinStationTransfer.from_route_id);
        assertNull(withinStationTransfer.to_route_id);
    }

    @Test
    public void testInternalTransfersByToRouteIfRouteSpecific() {
        List<Transfer> transfersToStop = sampleFeed.getTransfersToStop("BEATTY_AIRPORT", "AB");
        assertEquals(5, transfersToStop.size());
        assertEquals("AB", transfersToStop.get(0).from_route_id);
        assertEquals("FUNNY_BLOCK_AB", transfersToStop.get(1).from_route_id);
        assertEquals("STBA", transfersToStop.get(2).from_route_id);
        assertEquals("AAMV", transfersToStop.get(3).from_route_id);
        assertEquals("ABBFC", transfersToStop.get(4).from_route_id);
    }

    
    /// We here assure that the filter is working as expected, excluding transfer_type = 1.
    @Test
    public void testFiltersGetTransfersFromStop() {
         // 1st TEST : transfer_type=1 not included : BULLFROG,BULLFROG,AB,,1,100
         // Line added in transfers.txt, if changed the test won't pass
         // .filter(t -> t.transfer_type == 0 || t.transfer_type == 2) => FALSE
         // .filter(t -> t.from_route_id == null || fromRouteId.equals(t.from_route_id)) => FALSE
        assertEquals(Collections.emptyList(), sampleFeed.getTransfersFromStop("BULLFROG", "BULLFROG"));
    }

    /// We assure that if fromStopID is wrong, not an existing stop or a null argument,
    /// the function will return an empty list, as allOutboundTransfers is empty from the beginning.
    @Test
    public void testGetTransfersFromStopNotExistingFromStopID() {
        // 2nd TEST : not an existing fromStopID, or even null
        assertEquals(Collections.emptyList(), sampleFeed.getTransfersFromStop("TOTO", "AB"));
        assertEquals(Collections.emptyList(), sampleFeed.getTransfersFromStop(null, "TOTO"));
        assertEquals(Collections.emptyList(), sampleFeed.getTransfersFromStop("TOTO", null));
        assertEquals(Collections.emptyList(), sampleFeed.getTransfersFromStop(null, null));
    }

    @Test
    public void testGetTransfersFromStopNotExistingFromRouteID() {
        // 3rd TEST : not an existing fromRouteID, but existing FromStopID
        assertNotEquals(Collections.emptyList(), sampleFeed.getTransfersFromStop("BEATTY_AIRPORT", null));
        // if fromRouteId == null, the program fails (fromRouteId.equals in the filter) ! 
        assertNotEquals(Collections.emptyList(), sampleFeed.getTransfersFromStop("BEATTY_AIRPORT", "titi"));
    }

    @Test
    public void testGetTransfersFromStopGetExistingToStop() {
        // 4th test : testing different fromRouteId, transfers always existing if valid fromStopId
        List<Transfer> existingFromRouteID = sampleFeed.getTransfersFromStop("BEATTY_AIRPORT", "AB");
        List<Transfer> notExistingFromRouteID = sampleFeed.getTransfersFromStop("BEATTY_AIRPORT", "TOTO");
        assertNotEquals(Collections.emptyList(), existingFromRouteID);
        assertNotEquals(Collections.emptyList(), notExistingFromRouteID);
            // fromRouteId can be an imaginary route but cannot be null. We can admit coming from an unknown route as a valid case
        assertNotEquals(existingFromRouteID, notExistingFromRouteID);
    }

    @Test
    public void testWithFaker() {
        Faker faker = new Faker();

        // arrange

        // initialization of random values
        String randomRouteId = faker.letterify("RANDOM_????");
        String knownStopId = "BEATTY_AIRPORT";
        Transfers transfers = sampleFeed;

        // act

        // real stop, random route.
        List<Transfer> randomTransfers = transfers.getTransfersFromStop(knownStopId, randomRouteId);

        // asserts

        // as seen before, it should return a non-empty transfer list
        assertNotEquals(Collections.emptyList(), randomTransfers);
        
        // every transfer came from the same stop.
        assertTrue(randomTransfers.stream()
            .allMatch(t -> knownStopId.equals(t.from_stop_id)),
            "Every transfers must come from " + knownStopId);

        // Just to know what was created.
        System.out.println("Random route: " + randomRouteId);
        System.out.println("Generated transfers: " + randomTransfers.size());
    }
    
}
 