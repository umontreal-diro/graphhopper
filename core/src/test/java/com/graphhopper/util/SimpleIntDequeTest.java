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
package com.graphhopper.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayNameGenerator.Simple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Karich
 */
public class SimpleIntDequeTest {
    @Test
    public void testSmall() {
        SimpleIntDeque deque = new SimpleIntDeque(8, 2f);
        assertTrue(deque.isEmpty());
        assertEquals(0, deque.getSize());
    }

    @Test
    public void testEmpty() {
        SimpleIntDeque deque = new SimpleIntDeque(1, 2f);
        deque.push(1);
        assertEquals(1, deque.getSize());
        deque.pop();
        assertEquals(0, deque.getSize());
        deque.push(2);
        assertEquals(1, deque.getSize());
    }

    @Test
    public void testPush() {
        SimpleIntDeque deque = new SimpleIntDeque(8, 2f);

        for (int i = 0; i < 60; i++) {
            deque.push(i);
            assertEquals(i + 1, deque.getSize());
        }

        assertEquals(60, deque.getSize());

        assertEquals(0, deque.pop());
        assertEquals(59, deque.getSize());

        assertEquals(1, deque.pop());
        assertEquals(58, deque.getSize());

        deque.push(2);
        assertEquals(59, deque.getSize());
        deque.push(3);
        assertEquals(60, deque.getSize());

        for (int i = 0; i < 50; i++) {
            assertEquals(i + 2, deque.pop());
        }

        assertEquals(10, deque.getSize());
        assertEquals(39, deque.getCapacity());

        deque.push(123);
        assertEquals(11, deque.getSize());

        assertEquals(52, deque.pop());
        assertEquals(10, deque.getSize());
    }

    @Test
    public void testToStringEmpty() {
        SimpleIntDeque deque = new SimpleIntDeque(8, 2f);
        assertEquals("", deque.toString(), "Empty deque should be represented as []");
    }

    @Test
    public void testToStringSingleElement() {
        SimpleIntDeque deque = new SimpleIntDeque(8, 2f);
        deque.push(42);
        assertEquals("42", deque.toString(), "Deque with one element should be represented correctly");
    }

    @Test
    public void testToStringNonEmpty() {
        SimpleIntDeque deque = new SimpleIntDeque(8, 2f);
        deque.push(1);
        deque.push(2);
        deque.push(3);
        assertEquals("1, 2, 3", deque.toString(), "Deque with elements should be represented correctly");
    }

    @Test
    public void testToStringAfterPop() {
        SimpleIntDeque deque = new SimpleIntDeque();
        deque.push(10);
        deque.push(20);
        deque.push(30);
        deque.pop();
        assertEquals("20, 30", deque.toString(), "Deque after popping an element should be represented correctly");
    }
}
