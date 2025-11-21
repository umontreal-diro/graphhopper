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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.ParseException;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Tester DateRangeParser avec un Calendar mocké
// Amir Hannache - Matricule 20060308
@ExtendWith(MockitoExtension.class)
public class DateRangeParserMockTest {

    @Mock
    private Calendar mockCalendar;

    // Tester la création de DateRangeParser avec un Calendar mocké
    // Input: mockCalendar configuré
    // Output: DateRangeParser valide
    // Vérifie que DateRangeParser accepte un Calendar injecté
    // Exemple: new DateRangeParser(mockCalendar)
    @Test
    public void testCreateParserWithMockedCalendar() {
        DateRangeParser parser = new DateRangeParser(mockCalendar);

        assertNotNull(parser);
    }

    // Tester getRange avec mock pour isoler la logique
    // Input: mockCalendar, DateRangeParser
    // Output: DateRange créé
    // Permet de tester getRange sans dépendre de la vraie implémentation Calendar
    // Exemple: getRange("Mar-Oct") avec mockCalendar
    @Test
    public void testGetRangeWithMockedCalendar() throws ParseException {
        DateRangeParser parser = new DateRangeParser(mockCalendar);

        DateRange range = parser.getRange("Mar-Oct");

        assertNotNull(range);
    }
}
