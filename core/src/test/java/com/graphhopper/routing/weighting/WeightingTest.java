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
package com.graphhopper.routing.weighting;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.*;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.WayAccess;
import com.graphhopper.routing.util.parsers.CarAccessParser;
import com.graphhopper.util.PMap;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.util.PointList;
import com.graphhopper.util.FetchMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Peter Karich
 */
@ExtendWith(MockitoExtension.class)
public class WeightingTest {
    @Test
    public void testToString() {
        assertTrue(Weighting.isValidName("blup"));
        assertTrue(Weighting.isValidName("blup_a"));
        assertTrue(Weighting.isValidName("blup|a"));
        assertFalse(Weighting.isValidName("Blup"));
        assertFalse(Weighting.isValidName("Blup!"));
    }
   
     

    @Test
    public void testGetName(){
        var lookup = new EncodingManager.Builder().add(VehicleAccess.create("car")).add(VehicleSpeed.create("car",5, 5,true)).add(Roundabout.create()).build();
         
        var speedEnc = lookup.getDecimalEncodedValue(VehicleSpeed.key("car"));
        var weighting = new SpeedWeighting(speedEnc);

        assertEquals("speed", weighting.getName()); // le nom doit être "speed" pour SpeedWeighting
    }

    @Test
    public void testCarAccessParseBehaviour(){
        var lookup = new EncodingManager.Builder().add(VehicleAccess.create("car")).add(VehicleSpeed.create("car",5, 5,true)).add(Roundabout.create()).build();
        var parser = new CarAccessParser(lookup,new PMap());

        ReaderWay way1= new ReaderWay(1);
        way1.setTag("highway", "motorway");
        assertEquals(WayAccess.WAY, parser.getAccess(way1)); // les voitures peuvent accéder aux autoroutes

        ReaderWay way2= new ReaderWay(2);
        way2.setTag("highway", "track");
        way2.setTag("tracktype", "grade4");
        assertEquals(WayAccess.CAN_SKIP, parser.getAccess(way2)); // les pistes de type grade4 sont refusées

        ReaderWay way3= new ReaderWay(3);
        way3.setTag("highway","service");
        way3.setTag("service", "emergency_access");
        assertEquals(WayAccess.CAN_SKIP, parser.getAccess(way3)); // les routes de service d'urgence sont refusées
    }


    @Test
    public void testCalcEdgeWeight(){
        var lookup= new EncodingManager.Builder().add(VehicleSpeed.create("car",5, 5,true)).build();
        var speedEnc= lookup.getDecimalEncodedValue(VehicleSpeed.key("car"));
        var weighting = new SpeedWeighting(speedEnc);

        
        EdgeIteratorState edge1= new FakeEdge(100,0);
        double result1 = weighting.calcEdgeWeight(edge1, false);
        assertEquals(Double.POSITIVE_INFINITY, result1); //Une vitesse de 0 doit retourner un poids infini

        EdgeIteratorState edge2= new FakeEdge(100,50);
        double result2= weighting.calcEdgeWeight(edge2, false);
        assertEquals(2.0,result2, 1e-6);// poids attendu = distance / vitesse
    }

    @Mock
    private TurnCostProvider mockTurnCostProvider;

    @Test
    public void testCalcTurnWeightAvecMockTurnCostProvider() {
        var lookup = new EncodingManager.Builder().add(VehicleSpeed.create("car", 5, 5, true)).build();
        var speedEnc = lookup.getDecimalEncodedValue(VehicleSpeed.key("car"));
        var weighting = new SpeedWeighting(speedEnc, mockTurnCostProvider);

        int inEdge = 10;
        int viaNode = 20;
        int outEdge = 30;
        double expectedWeight = 2.5;

        when(mockTurnCostProvider.calcTurnWeight(inEdge, viaNode, outEdge))
                .thenReturn(expectedWeight);

        double result = weighting.calcTurnWeight(inEdge, viaNode, outEdge);

        assertEquals(expectedWeight, result, 1e-6);
        verify(mockTurnCostProvider).calcTurnWeight(inEdge, viaNode, outEdge);
    }

    static class FakeEdge implements EdgeIteratorState {

        private final double distance;
        private final double speed;

        public FakeEdge(double distance, double speed){
            this.distance= distance;
            this.speed = speed;
        }
        @Override
        public double getDistance(){return distance;}

        @Override
        public double get(DecimalEncodedValue property){return speed; }

        @Override
        public double getReverse(DecimalEncodedValue property){ return speed;}

        //setters neutres pour eviter les erreurs de compilation
        @Override public EdgeIteratorState set(DecimalEncodedValue property, double fwd,double bwd){return this;}
        @Override public EdgeIteratorState set(DecimalEncodedValue property,double value){return this ; }
        @Override public EdgeIteratorState setReverse(DecimalEncodedValue property, double value){ return this;}
        @Override public EdgeIteratorState set(IntEncodedValue property,int fwd, int bwd){return this;}
        @Override public EdgeIteratorState set(IntEncodedValue property,int value ){return this ;}
        @Override public EdgeIteratorState setReverse(IntEncodedValue property, int value){return this; }
        @Override public EdgeIteratorState set(BooleanEncodedValue property, boolean fwd, boolean bwd){  return this;}
        @Override public EdgeIteratorState set(BooleanEncodedValue property,boolean value){return  this;}
        @Override public EdgeIteratorState setReverse(BooleanEncodedValue property,boolean value){ return this;}
        @Override public <T extends Enum<?>> EdgeIteratorState set(EnumEncodedValue<T> property, T fwd,T bwd){return this;}
        @Override public <T extends Enum<?>> EdgeIteratorState set(EnumEncodedValue<T> property, T value){return this;}
        @Override public <T extends Enum<?>> EdgeIteratorState setReverse(EnumEncodedValue<T> property,T value){return this; }
        @Override public EdgeIteratorState set(StringEncodedValue property,String fwd, String bwd){return this;}
        @Override public EdgeIteratorState set(StringEncodedValue property, String value){return this;}
        @Override public EdgeIteratorState setReverse(StringEncodedValue property, String value){return this;}
        @Override public EdgeIteratorState setWayGeometry(PointList list){  return this;}
        @Override public EdgeIteratorState setDistance(double dist){return this; }
        @Override public EdgeIteratorState setFlags(IntsRef edgeFlags){ return this; }
        @Override public EdgeIteratorState setKeyValues(java.util.Map<String, com.graphhopper.search.KVStorage.KValue> map){  return this;}
        @Override public EdgeIteratorState copyPropertiesFrom(EdgeIteratorState e){return this; } 

        //getters simples pour renvoyer des valeurs neutres
        @Override public int getEdge(){ return 0;}
        @Override public int getEdgeKey(){ return 0;}
        @Override public int getReverseEdgeKey(){return 0;}
        @Override public int getBaseNode(){return  0 ;}
        @Override public int getAdjNode(){return 0;}
        @Override public IntsRef getFlags(){return new IntsRef(0); }
        @Override public boolean get(BooleanEncodedValue property){ return false;}
        @Override public boolean getReverse(BooleanEncodedValue property){return  false;}
        @Override public int get(IntEncodedValue property){ return 0;}
        @Override public int getReverse(IntEncodedValue property){return 0 ;}
        @Override public <T extends Enum<?>> T get(EnumEncodedValue<T> property){return null ; }
        @Override public <T extends Enum<?>> T getReverse(EnumEncodedValue<T> property){ return null; }
        @Override public String get(StringEncodedValue property){ return null;}
        @Override public String getReverse(StringEncodedValue property){return null; }
        @Override public String getName(){return "FakeEdge" ; }
        @Override public java.util.Map<String, com.graphhopper.search.KVStorage.KValue> getKeyValues(){ return java.util.Collections.emptyMap();}
        @Override public Object getValue(String key){return null;}
        @Override public PointList fetchWayGeometry(FetchMode mode){return null ;}
        @Override public EdgeIteratorState detach(boolean reverse){return this;}

        @Override
        public String toString(){
           return "FakeEdge{" + "distance="+ distance + ", speed=" + speed + '}';
        }
    }
        
}
