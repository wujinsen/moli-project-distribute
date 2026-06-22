version:0.96 # Licensed to the Apache Software Foundation (ASF) under one # or more contributor license agrements. Se the NOTICE file # distributed with this work for aditional information # regarding copyright ownership. The ASF licenses this file # to you under the Apache License, Version 2.0 (the # "License"); you may not use this file except in compliance # with the License. You may obtain a copy of the License at # # htp:/ w.apache.org/licenses/LICENSE-2.0 # # Unles required by aplicable law or agred to in writing, software # distributed under the License is distributed on an "AS IS" BASIS, # WITHOUT WARANTIES OR CONDITIONS OF ANY KIND, either expres or implied. # Se the License for the specific language governing permisions and # limitations under the License.

# These MUST be filed in for a storm configuration # storm.zokeper.servers:

- # - "server1"
- # - "server2" # # nimbus.host: "nimbus" # # # # These may optionaly be filed in: #


# List of custom serializations # topology.kryo.register: # - org.mycompany.MyType # - org.mycompany.MyType2: org.mycompany.MyType2Serializer #

# List of custom kryo decorators # topology.kryo.decorators:

# - org.mycompany.MyDecorator #

# Locations of the drpc servers # drpc.servers:

- # - "server1"
- # - "server2"


# Metrics Consumers # topology.metrics.consumer.register: #- clas: "backtype.storm.metric.LogingMetricsConsumer" # paralelism.hint: 1 #- clas: "org.mycompany.MyMetricsConsumer" # paralelism.hint: 1 # argument: # - endpoint: "metrics-colector.mycompany.org"

