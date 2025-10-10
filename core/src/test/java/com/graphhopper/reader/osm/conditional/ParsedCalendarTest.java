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
package com.graphhopper.reader.osm.conditional;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.Test;

import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests pour la classe ParsedCalendar.
 *
 * @author Amir Hannache
 */
public class ParsedCalendarTest {

    /**
     * Teste que getMax() configure correctement MILLISECOND à sa valeur maximale (999).
     */
    @Test
    public void testGetMaxSetsMillisecondCorrectly() {
        Calendar cal = DateRangeParser.createCalendar();
        cal.set(2024, Calendar.MARCH, 15, 10, 30, 45);
        cal.set(Calendar.MILLISECOND, 0);

        ParsedCalendar parsedCal = new ParsedCalendar(
            ParsedCalendar.ParseType.YEAR_MONTH_DAY,
            cal
        );

        Calendar result = parsedCal.getMax();

        assertEquals(999, result.get(Calendar.MILLISECOND));
    }

    /**
     * Teste que getMin() configure DAY_OF_MONTH à 1 pour les dates sans jour spécifique.
     */
    @Test
    public void testGetMinSetsDayOfMonthCorrectly() {
        Calendar cal = DateRangeParser.createCalendar();
        cal.set(2024, Calendar.MARCH, 15);

        ParsedCalendar parsedCal = new ParsedCalendar(
            ParsedCalendar.ParseType.YEAR_MONTH,
            cal
        );

        Calendar result = parsedCal.getMin();

        assertEquals(1, result.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * Teste que getMin() configure HOUR_OF_DAY à 0.
     */
    @Test
    public void testGetMinSetsHourOfDayCorrectly() {
        Calendar cal = DateRangeParser.createCalendar();
        cal.set(Calendar.MONTH, Calendar.MARCH);
        cal.set(Calendar.DAY_OF_MONTH, 15);
        cal.set(Calendar.HOUR_OF_DAY, 14);

        ParsedCalendar parsedCal = new ParsedCalendar(
            ParsedCalendar.ParseType.MONTH_DAY,
            cal
        );

        Calendar result = parsedCal.getMin();

        assertEquals(0, result.get(Calendar.HOUR_OF_DAY));
    }

    /**
     * Teste que getMin() configure MINUTE à 0.
     */
    @Test
    public void testGetMinSetsMinuteCorrectly() {
        Calendar cal = DateRangeParser.createCalendar();
        cal.set(2024, Calendar.MARCH, 15, 10, 45, 30);

        ParsedCalendar parsedCal = new ParsedCalendar(
            ParsedCalendar.ParseType.YEAR_MONTH_DAY,
            cal
        );

        Calendar result = parsedCal.getMin();

        assertEquals(0, result.get(Calendar.MINUTE));
    }

    /**
     * Teste que getMin() configure SECOND à 0.
     */
    @Test
    public void testGetMinSetsSecondCorrectly() {
        Calendar cal = DateRangeParser.createCalendar();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.SECOND, 45);

        ParsedCalendar parsedCal = new ParsedCalendar(
            ParsedCalendar.ParseType.DAY,
            cal
        );

        Calendar result = parsedCal.getMin();

        assertEquals(0, result.get(Calendar.SECOND));
    }

    /**
     * Teste que getMin() configure MILLISECOND à 0.
     */
    @Test
    public void testGetMinSetsMillisecondCorrectly() {
        Calendar cal = DateRangeParser.createCalendar();
        cal.set(Calendar.MONTH, Calendar.MARCH);
        cal.set(Calendar.MILLISECOND, 500);

        ParsedCalendar parsedCal = new ParsedCalendar(
            ParsedCalendar.ParseType.MONTH,
            cal
        );

        Calendar result = parsedCal.getMin();

        assertEquals(0, result.get(Calendar.MILLISECOND));
    }

    /**
     * Teste que toString() retourne une chaîne correctement formatée.
     * Utilise Java-Faker pour générer des dates aléatoires.
     */
    @Test
    public void testToStringWithRandomDateFromFaker() {
        Faker faker = new Faker();

        Calendar cal = DateRangeParser.createCalendar();
        java.util.Date randomDate = faker.date().birthday(0, 100);
        cal.setTime(randomDate);

        ParsedCalendar parsedCal = new ParsedCalendar(
            ParsedCalendar.ParseType.YEAR_MONTH_DAY,
            cal
        );

        String result = parsedCal.toString();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains("YEAR_MONTH_DAY"));
        assertTrue(result.contains(";"));
        assertTrue(result.length() > 20);
    }
}
