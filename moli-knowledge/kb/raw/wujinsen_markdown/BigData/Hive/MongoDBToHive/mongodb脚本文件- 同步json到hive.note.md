- 1. /opt/mongodb-4.4/bin/mongoexport -h 192.168.32.170 17017 -d yzl -c dispatch_rule -u yzl -p Yzl12345#-type=json -o /opt/mongodb-4.4/data/dispatch_rule_ful.json

hdfs dfs -mkdir /warehouse/tablespace/external/hive/mongo.db/dispatch_rule_ful; hadop fs -chmod 7 /warehouse/tablespace/external/hive/mongo.db/dispatch_rule_ful;

- 2. /opt/mongodb-4.4/bin/mongoexport -h 192.168.32.170 17017 -d yzl -c dispatch_rule_setup -u yzl p Yzl12345#-type=json -o /opt/mongodb-4.4/data/dispatch_rule_setup_ful.json
- 3. /opt/mongodb-4.4/bin/mongoexport -h 192.168.32.170 17017 -d yzl -c expresOrder -u yzl -p Yzl12345#-type=json -o /opt/mongodb-4.4/data/expresOrder_ful.json
- 4. /opt/mongodb-4.4/bin/mongoexport -h 192.168.32.170 17017 -d yzl -c gods_in_transit_detail -u yzl -p Yzl12345#-type=json -o /opt/mongodb-4.4/data/gods_in_transit_detail_ful.json
- 5. /opt/mongodb-4.4/bin/mongoexport -h 192.168.32.170 17017 -d yzl -c group_ref_member -u yzl -p Yzl12345#-type=json -o /opt/mongodb-4.4/data/group_ref_member_ful.json
- 6. /opt/mongodb-4.4/bin/mongoexport -h 192.168.32.170 17017 -d yzl -c member_alarm -u yzl -p Yzl12345#-type=json -o /opt/mongodb-4.4/data/member_alarm_ful.json
- 7. /opt/mongodb-4.4/bin/mongoexport -h 192.168.32.170 17017 -d yzl -c member_crowd_group -u yzl -p Yzl12345#-type=json -o /opt/mongodb-4.4/data/member_crowd_group_ful.json
- 8. /opt/mongodb-4.4/bin/mongoexport -h 192.168.32.170 17017 -d yzl -c member_label -u yzl -p Yzl12345#-type=json -o /opt/mongodb-4.4/data/member_label_ful.json
- 9. /opt/mongodb-4.4/bin/mongoexport -h 192.168.32.170 17017 -d yzl -c member_questionaire -u yzl -p Yzl12345#-type=json -o /opt/mongodb-4.4/data/member_questionaire_ful.json
- 10.


/opt/mongodb-4.4/bin/mongoexport -h 192.168.32.170 17017 -d yzl -c product_detail_setlemented -u yzl -p Yzl12345#-type=json -o /opt/mongodb4.4/data/product_detail_setlemented_ful.json

/warehouse/tablespace/external/hive/mongo.db/staf_crowd_group_ful

