-- Optional India starter data. Run explicitly against ehs_db, not as a schema migration.
-- Existing keys are preserved. IDs are application lookup IDs, not official geographic codes.
START TRANSACTION;
INSERT INTO reference_item (kind,id,name,country_id,state_id,code,currency,symbol,zone_id)
VALUES
('COUNTRY',1001,'India',NULL,NULL,'91','INR','₹',NULL),
('STATE',1101,'Telangana',1001,NULL,NULL,NULL,NULL,NULL),
('STATE',1102,'Andhra Pradesh',1001,NULL,NULL,NULL,NULL,NULL),
('STATE',1103,'Karnataka',1001,NULL,NULL,NULL,NULL,NULL),
('STATE',1104,'Tamil Nadu',1001,NULL,NULL,NULL,NULL,NULL),
('STATE',1105,'Maharashtra',1001,NULL,NULL,NULL,NULL,NULL),
('CITY',1201,'Hyderabad',1001,1101,NULL,NULL,NULL,NULL),
('CITY',1202,'Warangal',1001,1101,NULL,NULL,NULL,NULL),
('CITY',1203,'Visakhapatnam',1001,1102,NULL,NULL,NULL,NULL),
('CITY',1204,'Vijayawada',1001,1102,NULL,NULL,NULL,NULL),
('CITY',1205,'Bengaluru',1001,1103,NULL,NULL,NULL,NULL),
('CITY',1206,'Chennai',1001,1104,NULL,NULL,NULL,NULL),
('CITY',1207,'Mumbai',1001,1105,NULL,NULL,NULL,NULL),
('CITY',1208,'Pune',1001,1105,NULL,NULL,NULL,NULL),
('LANGUAGE',1301,'English',1001,NULL,'en',NULL,NULL,NULL),
('LANGUAGE',1302,'Hindi',1001,NULL,'hi',NULL,NULL,NULL),
('LANGUAGE',1303,'Telugu',1001,NULL,'te',NULL,NULL,NULL),
('LANGUAGE',1304,'Kannada',1001,NULL,'kn',NULL,NULL,NULL),
('LANGUAGE',1305,'Tamil',1001,NULL,'ta',NULL,NULL,NULL),
('LANGUAGE',1306,'Marathi',1001,NULL,'mr',NULL,NULL,NULL),
('TIME_ZONE',1401,'India Standard Time',1001,NULL,NULL,NULL,NULL,'Asia/Kolkata')
ON DUPLICATE KEY UPDATE id=reference_item.id;
COMMIT;
