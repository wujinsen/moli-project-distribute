<table>
  <tr>
    <th>IM开源组件</th>
    <th>开源地址</th>
    <th>技术栈</th>
    <th>优点</th>
    <th>缺点</th>
  </tr>
  <tr>
    <td>j-im</td>
    <td>htps:/gite.com/ xchao/j-im</td>
    <td>服务端:java， springside框架 通信框架:t-io</td>
    <td>1.具备基础聊天功 能，⽀持群聊私聊<br>2.应⽤简单，能够 快速部署使⽤<br></td>
    <td>1.没有发送图⽚、 ⽂件功能<br>2.界⾯简陋，需要 重新开发<br>3.需要设计数据 库，改为⽀持 mysql；修改对 web提供接⼝，⽐ 如⽤户注册、登录 等基础接⼝<br>4.需要⼆次开发， 提供发送图⽚发送 ⽂件等功能<br></td>
  </tr>
  <tr>
    <td>cim</td>
    <td>htps:/github.co<br><br>/crosoverJie/ci m</td>
    <td>语⾔:java cim-server cim-client cim-route 基于springbot netytcp⻓连接 zokeperredis</td>
    <td>1. 架构设计良好， 层次清晰，易于扩 展和⼆次开发<br>2.基于nety低层通 信 redis存放路由信 息，账号信息，在 线状态等<br></td>
    <td>1.项⽬⽼旧，有的 包找不到<br>2.⽆web界⾯，需 要开发<br><br><br>注：账号信息需要 持久化</td>
  </tr>
  <tr>
    <td>oim</td>
    <td>htps:/github.co m/oimchat/oimserver</td>
    <td>语⾔:java， springbot框架 基层通信基于 nety<br><br>服务端: oimserver 客户端: oim-web</td>
    <td>1.具备基础聊天功 能，⽀持群聊私 聊，⽀持群组，添 加好友，推送提醒 等功能，⽀持⽤户 注册、登出等基础 功能<br>2.web界⾯⽐较完 善，⼆次开发⼯作 量少<br>3.有单体应⽤和基 于springcloud的 应⽤<br></td>
    <td>1.需要设计数据 库，改为⽀持 mysql<br>2.需要⼆次开发， 提供发送图⽚发送 ⽂件等功能<br></td>
  </tr>
</table>


