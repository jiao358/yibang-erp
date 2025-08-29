import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import './style.css'

// 开发环境：设置模拟用户信息，绕过登录验证
if (import.meta.env.DEV) {
  localStorage.setItem('token', 'dev-mock-token-2024')
  localStorage.setItem('userInfo', JSON.stringify({
    id: 1,
    username: 'admin',
    realName: '系统管理员',
    email: 'admin@yibang.com',
    roleId: 1,
    companyId: 1,
    status: 'ACTIVE'
  }))
  localStorage.setItem('userRoles', JSON.stringify(['ADMIN']))
}

// 创建Vue应用实例
const app = createApp(App)

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 使用插件
app.use(router)
app.use(ElementPlus)

// 挂载应用
app.mount('#app')
