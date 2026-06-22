htps:/blog.csdn.net/ q_45814762/article/details/1075 87

⽂章⽬录

State Geters Mutation Actions

这是Vuex官⽅给出的图⽚，看起来有点不好理解，上详解！！！！ 开始介绍之前，我们需要将Vuex挂载到Vue实例上⾯，这样我们才能在Vue的所有组件中都可以使⽤这 些数据，这⾥我们store⽂件夹下⾯的index.js存放我们的Vuex代码。 记得在⼊⼝⽂件main.js⾥⾯导⼊store对象 State

暂时可以将他看作是data中的属性，也就是我们当前的状态。 state⾥⾯的这些属性都会被加⼊到响应式系统⾥⾯，⽽响应式系统会监听属性的变化，⼀旦属性发⽣ 变化，会通知所有⽤到这个属性的界⾯并进⾏刷新。 但有⼀个前提，所有的属性都必须先初始化好，如果直接在mutations⾥⾯添加⼀个新的属性，界⾯是 不会显示的。

/Vuex const store = new Vuex.Store({ sate:{ count: 0

}， mutations:{

increament(state){ state.count + }, decreament(state){ state.count }

} })

/Ap.vue <template>

<div id="ap"> <p>{count}</p> <buton @click="increment">+1</buton> <buton @ click="decrement">-1</buton>

</div> </template> <script> export default {

name:' Ap', components:{ }, computed:{

count: function(){ /可以通过$this.$store.sate访问Vuex⾥⾯state的状态

return this. $store.state.count }

},

methods:{ increment: function ){ /通过this.Store.comit(mutations中的⽅法)来修改状态 this. Sstore. comit(' increment')

}, decrement: function ){

this. Sstore. comit(' decrement') }

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
- 30
- 31
- 32


- 3


- 34
- 35
- 36
- 37
- 38
- 39
- 40
- 41
- 42
- 43 Geters


有时候我们需要获取⼀些state变异后的状态，就需要使⽤到我们的Geters了，有点类似于我们的 computed计算属性 我们想要获取students数组⾥⾯年龄⼤于20的数据，我们可以适应computed属性获取，但是我们想要 在多个⻚⾯获取变化后的数据，就需要在每⼀个组件⾥⾯都添加⼀个计算属性，效率极低！！！ Geters该出场了！！！！

/Vuex const store = new Vuex.Store({ sate:{

students:[ {id:10,name:'zhangsan',age:18}, {id: 1,name:'lisi',age:21}, {id:12,name:'wangwu',age:25},

]

}, geters:{

/这样我们就获取到了年龄⼤于20的数据，在需要的组件直接展示即可 getmore20:state =>

return state.students.filter(s => s.age > 20) }

})

- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8
- 9
- 10 1


- 12
- 13
- 14
- 15


- 16
- 17 geters默认是不能传递参数的，如果我们希望她能传递⼀个参数，我们只能让geters本身返回另⼀个 函数 我们想要获取年龄⼤于20的个数


/Vuex geters:{

- /1.我们可以直接添加length： {getmore20.length}获取 getmore20(state){

return state.students.filter(s => s.age > 20) }

- /2.将geters作为⼀个参数传递 getmore20Len(state,geters){

return geters.getmore20.length }

- /3.我们⾃定义在组件展示的地⽅传⼊⼀个年龄⽐如<h2>{getmoreAge(30)}</h2> /这⾥我们想要⾃定义获取⼀个年龄⼤于30的数据


getmoreAge(state){ return function(age){

return state.students.filter(s => s.age >= age) }

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
- 18 Mutation


在State⾥⾯已经⼤概了解了Mutations是什么，但是还有很多细节部分 Vuex官⽅明确：store⾥⾯state状态更新的唯⼀⽅式就是通过提交Mutations

- 2.mutations主要包含两部分：事件类型和回调函数 mutations⾥⾯的⽅法必须是同步的，异步⽤actions mutations:{


increament(state){

state.count + },

} /这⾥的increament被称为时间类型，其余被称为回调函数，state是回调函数的第⼀个参数

/mutations更新的⽅式就是上⽂提到的通过this.$store.comit的⽅式

- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8 当我们需要点击按钮进⾏数据的+5或者+10 的时候，我们可以⾃⼰传⼊5或者10的参数进⾏展示。


/Ap.vue <buton @click="adFive(5)">+5</buton>

methods:{ /1.第⼀种提交⻛格，count是⼀个数字 adFive(count){

this.$store.comit('adCount',count) }

/第⼆种提交⻛格，payload是⼀个对象 adFive(count){

this.$store.comit({ type: 'adCount', count: 5

}) }

}

/Vuex mutation:{ /1.第⼀种提交⻛格 adCount(state,count){

state.counter += count }

/第⼆种提交⻛格 adCount(state,payload){

state.counter += payload.count }

}

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
- 29 向students⾥⾯再添加⼀个学⽣信息，我们在更新数据的时候，我们可能希望携带⼀些额外的参数 （stu），这个参数被称为mutations的载荷（payload）


/Ap.vue <buton @click="adStudents">+5</buton>

methods:{

adStudents(){ const stu = {id:15,name:'kobe',age:40} this.$store.comit('adStu',stu)

} }

/Vuex mutation:{ adStu(state,stu){

state.students.push(stu) }

}

- 1
- 2
- 3
- 4
- 5
- 6
- 7


- 8
- 9
- 10 1


- 12
- 13
- 14
- 15
- 16
- 17 添加和删除⾮响应式数据


/Ap.vue <h2>{$store.state.info}</h2>

<buton @click="updateInfo">修改信息</buton>

methods:{

updateInfo() { this.$store.comit('updateInfo') }

}

/Vuex state:{

info: { name: '张三', age: 40, height: 180

}

} updateInfo(state) {

/ state.info.adres='湖南' /⽆效,做不到响应式 Vue.set(state.info, 'adres', '湖南')

/ delete state.info.name /⽆效,做不到响应式 Vue.delete(state.info, 'height')

}

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
- 25 Actions


actions类似于mutations，但是是⽤来代替muations进⾏异步操作的 点击按钮之后过⼀段时间在执⾏

/Vuex state :{

counter: 1 0, }

mutations:{ increment(state){

state.counter + }

} actions:{

increament(context){ /context:上下⽂，这⾥可以理解为store对象 setTimeOut()=>{

context.comit('increment') },1 0) }

}

/Ap.vue <h2>{counter}</h2>

<buton @click="increment">异步修改</buton>

methods:{ increment(){

this.$store.dispath('increament')/给actions发出⼀个⾃定义事件 }

} 1 2 3 4 5 6 7 8 9 10

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
- 29 actions返回的Promise


/Vuex state :{

counter: 1 0, }

mutations:{ increment(state){

state.counter + }

} actions:{

increament(context){ return new Promise(resolve)=>{

setTimeOut()=>{ context.comit('increment') resolve()

},1 0) }

}) }

/Ap.vue <h2>{counter}</h2>

<buton @click="increment">异步修改</buton>

methods:{ increment(){ this.$store.dispath('increament').then(res=>{

console.log('完成了更新') })

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
- 30
- 31
- 32


- 3


⸻版权声明：本⽂为CSDN博主「⼩铃铛的打怪之路」的原创⽂章，遵循 C 4.0 BY-SA版权协议，转载 请附上原⽂出处链接及本声明。 原⽂链接：htps:/blog.csdn.net/ q_45814762/article/details/1075 87

