INSERT INTO albums VALUES (1, 'Texture', 0, 762);
INSERT INTO albums VALUES (2, 'Bettina', 0, 10152);
INSERT INTO albums VALUES (3, 'Cats', 0, 7577);
INSERT INTO albums VALUES (4, 'Wildlife', 0, 9191);
INSERT INTO albums VALUES (5, 'Slides',0,771);

INSERT IGNORE INTO picture_file VALUES (762, 'https://drive.google.com/file/d/1xPGwLfrsfHGyHqsHW25TtQBpmg9qs7OO/view?usp=drivesdk','Exterior of Oerlikon station building, Zürich','2020-05-31',1,2);
INSERT IGNORE INTO picture_file VALUES (761, 'https://drive.google.com/file/d/10HienPUgo5A1dGn8zcccw-yyMsoqdgbb/view?usp=drivesdk','Breton shire horses in meadow near Callac','2020-05-31',1,1);
INSERT IGNORE INTO picture_file VALUES (763, 'https://drive.google.com/file/d/1ZB5jMnD57UQMUM8iGKkr44UnEzI6zU6k/view?usp=drivesdk','Entrenched platform of the railway in Überlingen, Lake Constance','2020-05-31',1,3);
INSERT IGNORE INTO picture_file VALUES (764, 'https://drive.google.com/file/d/1xPGwLfrsfHGyHqsHW25TtQBpmg9qs7OO/view?usp=drivesdk','Primula elatior; oxlips flowering in ditch in Flums, Switzerland','2020-05-31',1,1);
INSERT IGNORE INTO picture_file VALUES (766, 'https://drive.google.com/file/d/1xPGwLfrsfHGyHqsHW25TtQBpmg9qs7OO/view?usp=drivesdk','Canal at dawn, Rüthi SG in the Swiss Rhine valley','2020-05-31',1,2);
INSERT IGNORE INTO picture_file VALUES (768, 'https://drive.google.com/file/d/1xPGwLfrsfHGyHqsHW25TtQBpmg9qs7OO/view?usp=drivesdk','Lavertezzo; the Maldives of Italy, located in the Swiss canton of Ticino','2020-05-31',1,3);
INSERT IGNORE INTO picture_file VALUES (769, 'https://drive.google.com/file/d/1xPGwLfrsfHGyHqsHW25TtQBpmg9qs7OO/view?usp=drivesdk','Buildings in the Old Town part of Chur, Graubünden','2020-05-31',1,1);
INSERT IGNORE INTO picture_file VALUES (770, 'https://drive.google.com/file/d/1xPGwLfrsfHGyHqsHW25TtQBpmg9qs7OO/view?usp=drivesdk','The Creux du Van, inland cove in the limestone of Neuchastel','2020-05-31',1,2);
INSERT IGNORE INTO picture_file VALUES (772, 'https://drive.google.com/file/d/1AsMj3GdaZeJ_JNgDFFxkIy76rP6j_VQX/view?usp=drivesdk','S. Maria al Castello; Swiss church in Mesocco, Grisons','2020-05-31',1,3);
INSERT IGNORE INTO picture_file VALUES (774, 'https://drive.google.com/file/d/1xPGwLfrsfHGyHqsHW25TtQBpmg9qs7OO/view?usp=drivesdk','Swiss Alpine village of Splügen in Graubünden (Grisons),291448965000000','2020-05-31',1,1);
INSERT IGNORE INTO picture_file VALUES (775, 'https://drive.google.com/file/d/1xPGwLfrsfHGyHqsHW25TtQBpmg9qs7OO/view?usp=drivesdk','Newly-created station forecourt and bus stop in Sargans, showing Spring foliage and dramatic lighting pillars','2020-05-31',1,2);
INSERT IGNORE INTO picture_file VALUES (779, 'https://drive.google.com/file/d/1xPGwLfrsfHGyHqsHW25TtQBpmg9qs7OO/view?usp=drivesdk','Pyrrhocorax graculus; Alpine or yellow-billed chough on Säntis, Appenzell','2020-05-31',1,3);
INSERT IGNORE INTO picture_file VALUES (780, 'https://drive.google.com/file/d/1xPGwLfrsfHGyHqsHW25TtQBpmg9qs7OO/view?usp=drivesdk','Rocks on Sticks; informal garden in the air at the Eggishorn station, Valais','2020-05-31',1,1);
INSERT IGNORE INTO picture_file VALUES (781, 'https://drive.google.com/file/d/1HSYDhMGj_kREgOBMkWfWwFW8zBIMUUJ6/view?usp=drivesdk','Village scene in Maienfeld, Swiss Rhine valley','2020-05-31',1,2);
INSERT IGNORE INTO picture_file VALUES (7153,'https://drive.google.com/file/d/1xPGwLfrsfHGyHqsHW25TtQBpmg9qs7OO/view?usp=drivesdk','View of morning clouds from above, shot from Säntis in the Swiss Alps','2020-05-31',1,3);
INSERT IGNORE INTO picture_file VALUES (7154,'https://drive.google.com/file/d/1xPGwLfrsfHGyHqsHW25TtQBpmg9qs7OO/view?usp=drivesdk','Autumn woods of Appenzell, Switzerland','2020-05-31',1,1);
INSERT IGNORE INTO picture_file VALUES (7155,'https://drive.google.com/file/d/1xPGwLfrsfHGyHqsHW25TtQBpmg9qs7OO/view?usp=drivesdk','View across Canton Appenzell from the Alpstein','2020-05-31',1,2);
INSERT IGNORE INTO picture_file VALUES (7156,'https://drive.google.com/file/d/1xPGwLfrsfHGyHqsHW25TtQBpmg9qs7OO/view?usp=drivesdk','View across the fields of Appenzell from Ebenalp, Swiss Alps','2020-05-31',1,3);

INSERT INTO keywords VALUES(1,'graubünden,maienfeld,schweiz,spring',4);
INSERT INTO keywords VALUES(2,'gates,iron,maienfeld,schweiz,wrought',5);
INSERT INTO keywords VALUES(3,'barn,building,entrance,exterior,farm,graubuenden,graubünden,maienfeld,raised,schweiz,spring,stairs,stairway,timber,twin,valley,village,wooden,¨rhine',19);
INSERT INTO keywords VALUES(4,'graubünden,maienfeld,schweiz',3);
INSERT INTO keywords VALUES(5,'alps,barn,blue sky,building,europe,exterior,graubuenden,graubünden,maienfeld,outdoors,quaint,rural,scenic,spring,street,sunshine,switzerland,town,village',19);

INSERT INTO types VALUES (1, 'Switzerland');

INSERT INTO picture_file (id,filename,title,last_modified,path_id,keywords_id,sortkey,added_on,taken_on,location,primary_category,secondary_category,hits)
    VALUES (0,'/resources/images/Baar/jpegs/dsc_114288-dsc_114305_4156061_2014_07_17_12_46_53_27.jpg','Church at roundabout in Bad Ragaz','2021-01-13',1,1,1,'2021-01-13','2021-01-13',1,1,1,1),
           (7364,'/resources/images/Aarburg/jpegs/dse_023420-dse_023422_6066523_2023_09_28_13_40_46_99_02_00.jpg','View of Aarburg castle, Canton of Aarau','2021-01-13',1,1,1,'2021-01-13','2021-01-13',1,1,1,1),
           (8700,'/resources/images/Aarburg/jpegs/dse_023623-dse_023653_6066523_2023_09_28_14_16_43_28_02_00.jpg','Courtyard of Aargau castle, Canton of Aargau','2021-01-13',1,1,1,'2021-01-13','2021-01-13',1,1,1,1),
           (35002,'/resources/images/TestCamelCase/jpegs/mantiuk_0249-mantiuk_0274-0.jpg','Atmospheric inversion in the Swiss Rhine valley trapping a sea of fog seen from the Gonzen','2020-06-10',1,1,1,'2020-06-10','2020-06-10',1,1,1,1),
           (7491,'/resources/images/TestCamelCase/jpegs/dsc_129576.jpg','Bettina in the Bodensee','2021-01-12',1,1,1,'2021-01-12','2021-01-12',1,1,1,1),
           (745,'/resources/images/TestCamelCase/jpegs/Ammonites.jpg','Ammonite fossils in the paving stone of the Piazza Erbe in Verona','2020-05-25',1,1,1,'2020-05-25','2020-05-25',1,1,1,1),
           (7586,'/resources/images/TestCamelCase/jpegs/dsc_129571.jpg','Bettina in the Bodensee','2021-01-05',1,1,1,'2021-01-05','2021-01-05',1,1,1,1)
           ;

INSERT INTO albums (id, name, picture_count, thumbnail_id)
  VALUES (1, 'Texture', 1, 7364),
         (2, 'Aarburg', 1, 35002),
         (3, 'Cats', 1, 7491),
         (4, 'Wildlife', 1, 745),
         (5, 'Slides', 1, 7506)
         ;

INSERT INTO albumcontent VALUES(0,1,7364);
INSERT INTO albumcontent VALUES(1,1,7586);
INSERT INTO albumcontent VALUES(2,1,745);
INSERT INTO albumcontent VALUES(3,1,745);
INSERT INTO albumcontent VALUES(4,1,7586);
INSERT INTO albumcontent VALUES(5,2,8700);
INSERT INTO albumcontent VALUES(6,3,35002);
INSERT INTO albumcontent VALUES(7,4,7491);
INSERT INTO albumcontent VALUES(8,5,7491);
