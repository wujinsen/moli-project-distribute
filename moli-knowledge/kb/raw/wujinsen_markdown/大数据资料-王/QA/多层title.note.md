# { text: time1, columns: [] }

- 1

- 2


- 1 {

- 2 text: 'Stock Price',

- 3 columns: [{

- 4 text : 'Price',

- 5 width : 75,

- 6 sortable : true,

- 7 renderer : 'usMoney',

- 8 dataIndex: 'price'

- 9 }, {

- 10 text : 'Change',

- 11 width : 75,

- 12 sortable : true,

- 13 renderer : change,

- 14 dataIndex: 'change'

- 15 }, {

- 16 text : '% Change',

- 17 width : 75,

- 18 sortable : true,

- 19 renderer : pctChange,

- 20 dataIndex: 'pctChange'

- 21 }]

- 22 },


