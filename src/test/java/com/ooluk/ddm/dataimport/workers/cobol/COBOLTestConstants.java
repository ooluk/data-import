/*
 *  Copyright 2015 Ooluk Corporation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.ooluk.ddm.dataimport.workers.cobol;

/**
 * @author Siddhesh Prabhu
 * @since 1.0
 *
 */
public interface COBOLTestConstants {
	
	String[][] ATTR_DATA = {
			{"N-01","UINT(5)","INT(5)","ZEROES"},
			{"N-02","UINT(5)","INT(5)",""},
			{"N-03","UINT(5)","INT(5)",""},
			{"N-04","UINT(5)","INT(5)",""},
			{"N-05","UINT(5)","INT(5)",""},
			{"N-06","UINT(5)","INT(5)",""},
			{"N-07","SINT(5)","INT(5)",""},
			{"N-08","SINT(5)","INT(5)",""},
			{"N-09","SINT(5)","INT(5)",""},
			{"N-10","SINT(5)","INT(5)",""},
			{"N-11","SINT(5)","INT(5)",""},
			{"N-12","SINT(5)","INT(5)",""},
			{"N-13","SINT(5)","INT(5)",""},
			{"N-14","SINT(5)","INT(5)",""},
			{"N-15","SINT(5)","INT(5)",""},
			{"N-16","SINT(5)","INT(5)",""},
			{"N-17","SINT(5)","INT(5)",""},
			{"N-18","---99","",""},
			{"N-20","UNUM(3,2)","DECIMAL(3,2)",""},
			{"N-21","UNUM(3,2)","DECIMAL(3,2)",""},
			{"N-22","UNUM(3,2)","DECIMAL(3,2)",""},
			{"N-23","UNUM(3,2)","DECIMAL(3,2)",""},
			{"N-24","SNUM(3,2)","DECIMAL(3,2)",""},
			{"N-25","SNUM(3,2)","DECIMAL(3,2)",""},
			{"N-26","SNUM(3,2)","DECIMAL(3,2)",""},
			{"N-27","SNUM(3,2)","DECIMAL(3,2)",""},
			{"N-28","SNUM(3,2)","DECIMAL(3,2)",""},
			{"N-29","SNUM(3,2)","DECIMAL(3,2)",""},
			{"N-30","SNUM(3,2)","DECIMAL(3,2)",""},
			{"N-31","SNUM(3,2)","DECIMAL(3,2)",""},
			{"N-32","SNUM(3,2)","DECIMAL(3,2)",""},
			{"N-33","SNUM(3,2)","DECIMAL(3,2)",""},
			{"N-34","SNUM(3,2)","DECIMAL(3,2)",""},
			{"N-36","ALPHA(5)","CHAR(5)",""},
			{"N-37","ALPHA(5)","CHAR(5)",""},
			{"N-38","ALPHA(5)","CHAR(5)",""},
			{"N-39","ALPHA(5)","CHAR(5)",""},
			{"N-40","ALPHANUM(5)","CHAR(5)",""},
			{"N-41","ALPHANUM(5)","CHAR(5)",""},
			{"N-42","ALPHANUM(5)","CHAR(5)",""},
			{"N-43","ALPHANUM(5)","CHAR(5)",""},
			{"N-44","ALPHANUM(5)","CHAR(5)",""},
			{"N-45","ALPHANUM(5)","CHAR(5)",""},
			{"N-46","ALPHANUM(5)","CHAR(5)",""},
			{"N-47","ALPHANUM(5)","CHAR(5)",""},
			{"N-48","ALPHANUM(5)","CHAR(5)",""},
			{"N-50","SINT(4)","INT(4)",""},
			{"N-51","SINT(4)","INT(4)",""},
			{"N-52","SINT(4)","INT(4)",""},
			{"N-54","COMP-1","FLOAT4",""},
			{"N-55","COMPUTATIONAL-1","FLOAT4",""},
			{"N-56","COMP-2","FLOAT8",""},
			{"N-57","COMPUTATIONAL-2","FLOAT8",""},
			{"N-58","SINT(4)","INT(4)",""},
			{"N-59","SINT(4)","INT(4)",""},
			{"N-60","SINT(4)","INT(4)",""},
			{"N-61","SINT(4)","INT(4)",""},
			{"N-62","SINT(4)","INT(4)",""},
			{"N-63","SINT(4)","INT(4)",""},
			{"N-64","SINT(4)","INT(4)",""},
			{"N-65","ALPHANUM(1)","CHAR(1)","N"}
	};
	
	String[][] CODE_DATA = {
			{"N", "NO"},
			{"Y", "YES"},
			{"'A', 'B', 'C'", "INITIAL"},
			{"'X', 'Y', 'Z'", "END"},
			{"M", "MID"}
	};
	
	String[][] TYPE_METADATA = {
			{"N-01","99999 DISPLAY","UINT","5","0","DISPLAY"},
			{"N-02","99(4)","UINT","5","0",""},
			{"N-03","9(4)9","UINT","5","0",""},
			{"N-04","9(2)9(3)","UINT","5","0",""},
			{"N-05","9(5)","UINT","5","0",""},
			{"N-06","9(5)V","UINT","5","0",""},
			{"N-07","S9(5)","SINT","5","0",""},
			{"N-08","S9(5)V","SINT","5","0",""},
			{"N-09","+9(5)","SINT","5","0",""},
			{"N-10","+9(5)V","SINT","5","0",""},
			{"N-11","-9(5)","SINT","5","0",""},
			{"N-12","-9(5)V","SINT","5","0",""},
			{"N-13","9(5)+","SINT","5","0",""},
			{"N-14","9(5)V+","SINT","5","0",""},
			{"N-15","9(5)-","SINT","5","0",""},
			{"N-16","99(4)V-","SINT","5","0",""},
			{"N-17","ZZ09(2)V-","SINT","5","0",""},
			{"N-18","---99","[CK] ---99","0","0",""},
			
			{"N-20","999V99","UNUM","5","2",""},
			{"N-21","99(2)V99","UNUM","5","2",""},
			{"N-22","9(3)V9(2)","UNUM","5","2",""},
			{"N-23","9(3).9(2)","UNUM","5","2",""},
			{"N-24","S9(3)V9(2)","SNUM","5","2",""},
			{"N-25","S9(3).9(2)","SNUM","5","2",""},
			{"N-26","+9(3)V9(2)","SNUM","5","2",""},
			{"N-27","+9(3).9(2)","SNUM","5","2",""},
			{"N-28","-9(3)V9(2)","SNUM","5","2",""},
			{"N-29","-9(3).9(2)","SNUM","5","2",""},
			{"N-30","9(3)V9(2)+","SNUM","5","2",""},
			{"N-31","9(3).9(2)+","SNUM","5","2",""},
			{"N-32","9(3)V9(2)-","SNUM","5","2",""},
			{"N-33","9(3).9(2)-","SNUM","5","2",""},
			{"N-34","Z09.9(2)-","SNUM","5","2",""},

			{"N-36","AAAAA","ALPHA","5","0",""},
			{"N-37","AA(4)","ALPHA","5","0",""},
			{"N-38","A(4)A","ALPHA","5","0",""},
			{"N-39","A(2)A(3)","ALPHA","5","0",""},
			{"N-40","XXXXX","ALPHANUM","5","0",""},
			{"N-41","XX(4)","ALPHANUM","5","0",""},
			{"N-42","X(4)X","ALPHANUM","5","0",""},
			{"N-43","X(2)X(3)","ALPHANUM","5","0",""},
			{"N-44","XAAXX","ALPHANUM","5","0",""},
			{"N-45","X(2)A(2)9","ALPHANUM","5","0",""},
			{"N-46","BAAXX9","ALPHANUM","5","0",""},
			{"N-47","AX99BA","ALPHANUM","5","0",""},
			{"N-48","AX/X99","ALPHANUM","5","0",""},

			{"N-50","S9(4) COMP","SINT","4","0","COMP"},
			{"N-51","S9(4) COMP","SINT","4","0","COMP"},
			{"N-52","S9(4) COMP","SINT","4","0","COMP"},

			{"N-54","COMP-1","FLOAT4","0","0","COMP-1"},
			{"N-55","COMPUTATIONAL-1","FLOAT4","0","0","COMP-1"},
			{"N-56","COMP-2","FLOAT8","0","0","COMP-2"},
			{"N-57","COMPUTATIONAL-2","FLOAT8","0","0","COMP-2"},
			{"N-58","S9(4) COMP-3","SINT","4","0","COMP-3"},
			{"N-59","S9(4) COMPUTATIONAL-3","SINT","4","0","COMP-3"},
			{"N-60","S9(4) COMP-4","SINT","4","0","COMP"},
			{"N-61","S9(4) COMPUTATIONAL-4","SINT","4","0","COMP"},
			{"N-62","S9(4) COMP-5","SINT","4","0","COMP-5"},
			{"N-63","S9(4) COMPUTATIONAL-5","SINT","4","0","COMP-5"},
			{"N-64","S9(4) BINARY","SINT","4","0","COMP"},
			{"N-65","X(1)","ALPHANUM","1","0",""}
	};
}
