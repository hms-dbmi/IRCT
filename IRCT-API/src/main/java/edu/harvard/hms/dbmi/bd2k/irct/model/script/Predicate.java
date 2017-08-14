package edu.harvard.hms.dbmi.bd2k.irct.model.script;

import java.util.Map.Entry;

public class Predicate {
	Field field;
	String predicate;
	Entry<String, String>[] fields;
}

/**
 * 
"field": {
  "pui": "/GRIN-dev/Demo/Genetics of Severe Early Childhood Obseity (GECO)/Genetics of Severe Early Childhood Obseity (GECO)/Exomes/SYMBOL/MRAP2",
  "dataType": "STRING"
 },
 "predicate": "CONTAINS",
 "fields": {
   "ENOUNTER": "NO"
 }
},
{
"field": {
  "pui": "/GRIN-dev/Demo/Genetics of Severe Early Childhood Obseity (GECO)/Genetics of Severe Early Childhood Obseity (GECO)/EHR I2B2/Vitals/BMI for Age Z- Score (CDC-Calculation)",
  "dataType": "INTEGER"
 },
 "predicate": "CONSTRAIN_VALUE",
 "fields": {
   "OPERATOR": "GE",
   "CONSTRAINT": "3",
   "UNIT_OF_MEASURE": "kg/m2",
   "ENOUNTER": "NO"
 },
 "logicalOperator" : "AND"
}
]
}
 * 
 * 
 * 
 */