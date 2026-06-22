htps:/blog.csdn.net/ q_3760506/article/details/1230801

⼀、问题描述：今天测试反馈了⼀个bug：修改⽤户操作⾯板上，点击多选的下拉选择框（el-select） 弹出⾯板–>点击选中或取消选中其中的选择项—>点击⽆反应

<el-dialog title="修改⽤户" :visible.sync="open" width="60px" apend-to-body>

.

<el-form-item label="企业可⻅权限" prop="clientUnitIds">

<el-select v-model="form.clientUnitIds" multiple :colapse-tags="true" filterable placeholder="请选择" style="width: 10%">

<el-option v-for="(item, index) in clientUnitIds" :key="index" :label="item.unitName" :value="item.id" ></el-option>

</el-select> </el-form-item>

. </el-dialog>

export default { data() {

return { open: false, from: {}, clientUnitIds: [],

}

}, methods: {

/ 查询⽤户信息

async getUserInfo(){ cosnt response = await getUser({id: 1}) this.form.clientUnitIds = response.suplyUnitIds.map(item => item.unitId)/ 企业可⻅权限

},

} }

- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8
- 9
- 10


- 1

- 12
- 13
- 14
- 15
- 16
- 17
- 18
- 19
- 20
- 21


- 2


- 23
- 24
- 25
- 26
- 27
- 28
- 29
- 30 ⼆、问题分析：查看了⼀下代码，原来是在初始化form对象属性时，并没有将 clientUnitIds 属性添加 到 form中， 导致后⾯直接设置 this.form.clientUnitIds后，数据的值是改变了，但是render函数并没有⾃动更新， 视图也⾃然没有进⾏刷新。后⾯查看了⼀下Vue官⽅⽂档，⾥⾯也有提到，我截图了下来：


三、问题解决：使⽤ this.$set 来设置变量的值，让视图重新render export default {

data() {

return { open: false, from: {}, clientUnitIds: [],

}

}, methods: {

/ 查询⽤户信息

async getUserInfo(){ cosnt response = await getUser({id: 1}) this.$set(this.form, 'clientUnitIds', response.clientUnitIds.map(item => item.unitId)/ 企业可

⻅权限

}, }

}

⸻版权声明：本⽂为CSDN博主「俺是⽼王」的原创⽂章，遵循 C 4.0 BY-SA版权协议，转载请附上原 ⽂出处链接及本声明。 原⽂链接：htps:/blog.csdn.net/ q_3760506/article/details/1230801

