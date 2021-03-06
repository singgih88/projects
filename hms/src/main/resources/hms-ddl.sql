CREATE SCHEMA HMS;

USE HMS;

DROP TABLE IF EXISTS PATIENTS;
DROP TABLE IF EXISTS ADDRESSES;
DROP TABLE IF EXISTS CITIES;

CREATE TABLE CITIES(
	CITY_ID INT PRIMARY KEY,
	CITY_NAME VARCHAR(150),
	STATE VARCHAR(150),
	COUNTRY VARCHAR(150)
);

INSERT INTO CITIES(CITY_ID, CITY_NAME, STATE, COUNTRY) VALUES(1, 'Nagpur','Maharashtra','India');
INSERT INTO CITIES(CITY_ID, CITY_NAME, STATE, COUNTRY) VALUES(2, 'Mumbai','Maharashtra','India');
INSERT INTO CITIES(CITY_ID, CITY_NAME, STATE, COUNTRY) VALUES(3, 'Pune','Maharashtra','India');
INSERT INTO CITIES(CITY_ID, CITY_NAME, STATE, COUNTRY) VALUES(4, 'Chandrapur','Maharashtra','India');

CREATE TABLE ADDRESSES(
	ADDRESS_ID INT PRIMARY KEY AUTO_INCREMENT,
	LINE1 VARCHAR(100),
	LINE2 VARCHAR(150),
	AREA VARCHAR(150),
	CITY_ID INT,
	CONSTRAINT FK_ADDRESS_CITY FOREIGN KEY (CITY_ID) REFERENCES CITIES(CITY_ID) 
);

CREATE TABLE PATIENTS(
	PATIENT_ID INT PRIMARY KEY AUTO_INCREMENT,
	FIRST_NAME VARCHAR(100),
	LAST_NAME VARCHAR(100),
	DATE_OF_BIRTH DATE,
	PHONE_NO VARCHAR(20),
	MOBILE VARCHAR(10),
	EMAIL VARCHAR(200),
	ADDRESS_ID INT,
	CONSTRAINT FK_PATIENT_ADDRESS FOREIGN KEY (ADDRESS_ID) REFERENCES ADDRESSES(ADDRESS_ID) ON DELETE CASCADE ON UPDATE CASCADE
);

DROP TABLE IF EXISTS DOCTORS;

CREATE TABLE DOCTORS(
	DOCTOR_ID INT PRIMARY KEY AUTO_INCREMENT,
	DOCTOR_NAME VARCHAR(200),
	DESIGNATION VARCHAR(200),
	QUALIFICATION VARCHAR(255),
	CONSULTATION_FEE DOUBLE(10,2)
);

INSERT INTO DOCTORS(DOCTOR_NAME,DESIGNATION, QUALIFICATION,CONSULTATION_FEE) VALUES('Deepak','Consultant','MBBS',200);
INSERT INTO DOCTORS(DOCTOR_NAME,DESIGNATION, QUALIFICATION,CONSULTATION_FEE) VALUES('Sanjeev','General Physician','MBBS, MD',350);


CREATE TABLE APPOINTMENTS(
	APPOINTMENT_ID INT PRIMARY KEY AUTO_INCREMENT,
	
	
);