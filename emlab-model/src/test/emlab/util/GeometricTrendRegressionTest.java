/*******************************************************************************
 * Copyright 2012 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package emlab.util;



import org.apache.commons.math.stat.regression.SimpleRegression;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.template.Neo4jOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import emlab.domain.market.ClearingPoint;
import emlab.domain.market.CommodityMarket;
import emlab.domain.technology.Substance;
import emlab.repository.ClearingPointRepository;
import emlab.util.GeometricTrendRegression;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/emlab-test-context.xml"})
@Transactional
public class GeometricTrendRegressionTest {
	

	
	@Autowired Neo4jOperations template;

	@Autowired
	ClearingPointRepository clearingPointRepository;
    
	@Before
    @Transactional
    public void setUp() throws Exception {

    }
	
	@Test
    public void testLinearTrendEstimation(){
    	double[][] input = {{0,1},{1,1.1},{2,1.2},{3,1.3},{4,1.4}};
    	double[] predictionYears = {5,6,7,8};
    	double[] expectedResults = {1.5,1.6	,1.7,1.8};
    	SimpleRegression sr = new SimpleRegression();
    	sr.addData(input);
    	for (int i = 0; i<predictionYears.length; i++){
    		assert(expectedResults[i]==sr.predict(predictionYears[i]));
    	}
    }

	@Test
	public void testGeometricTrendEstimation() {
		double[][] input = {{0,1},{1,1.1},{2,1.21},{3,1.331},{4,1.4641}};
		double[] predictionYears = {5,6,7,8};
		double[] expectedResults = {1.61051	,1.771561,1.9487171,2.14358881};
    	GeometricTrendRegression gtr = new GeometricTrendRegression();
    	gtr.addData(input);
    	for (int i = 0; i<predictionYears.length; i++){
    		assert(expectedResults[i]==gtr.predict(predictionYears[i]));
    	}
	}
	
	@Test
	public void testGeometricTrendEstimationFromQuery() {
		double[][] input = {{0,1},{1,1.1},{2,1.21},{3,1.331},{4,1.4641}};
		Substance substance = new Substance();
		substance.persist();
		CommodityMarket market = new CommodityMarket();
		market.setSubstance(substance);
		market.persist();
		for (double[] d : input) {
			ClearingPoint cp = new ClearingPoint();
			cp.setTime((long) d[0]);
			cp.setPrice(d[1]);
			cp.setAbstractMarket(market);
			template.save(cp);
		}
		Iterable<ClearingPoint> cps = clearingPointRepository.findAllClearingPointsForSubstanceAndTimeRange(substance, 0, 4);
		GeometricTrendRegression gtr = new GeometricTrendRegression();
		for (ClearingPoint clearingPoint : cps) {
			gtr.addData(clearingPoint.getTime(), clearingPoint.getPrice());
		}
		double[] predictionYears = {5,6,7,8};
		double[] expectedResults = {1.61051	,1.771561,1.9487171,2.14358881};
    	for (int i = 0; i<predictionYears.length; i++){
    		assert(expectedResults[i]==gtr.predict(predictionYears[i]));
    	}
	}

}
