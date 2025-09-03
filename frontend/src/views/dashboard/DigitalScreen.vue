<template>
  <div class="digital-screen">
    <!-- 顶部标题栏 -->
    <div class="screen-header">
      <div class="header-left">
        <h1 class="main-title">领商供应链数字大屏</h1>
      </div>
      <div class="header-center">
        <div class="user-counter">
          <span class="counter-label">累计服务用户</span>
          <div class="counter-digits">
            <span class="digit">2</span>
            <span class="digit">3</span>
            <span class="digit">2</span>
            <span class="digit">9</span>
            <span class="digit">6</span>
            <span class="digit">5</span>
          </div>
        </div>
      </div>
      <div class="header-right">
        <div class="time-info">
          <div class="current-date">{{ currentDate }}</div>
          <div class="current-time">{{ currentTime }}</div>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="screen-content">
      <!-- 左侧面板 -->
      <div class="left-panel">
        <!-- 进度指标面板 -->
        <div class="module-card progress-panel">
          <h3 class="panel-title">计划执行情况</h3>
          <div class="progress-container">
            <div class="progress-item">
              <div class="progress-ring">
                <div class="progress-value">100%</div>
                <div class="progress-label">月度计划</div>
              </div>
              <div class="progress-number">100</div>
            </div>
            <div class="progress-item">
              <div class="progress-ring">
                <div class="progress-value">85%</div>
                <div class="progress-label">执行计划</div>
              </div>
              <div class="progress-number">85</div>
            </div>
          </div>
        </div>

        <!-- 区域分布面板 -->
        <div class="module-card distribution-panel">
          <h3 class="panel-title">明日区域分布</h3>
          <div class="chart-container">
            <v-chart class="distribution-chart" :option="distributionChartOption" />
          </div>
          <div class="data-summary">
            <span class="summary-icon">📊</span>
            <span class="summary-count">156个</span>
          </div>
        </div>

        <!-- 项目行业分布面板 -->
        <div class="module-card industry-panel">
          <h3 class="panel-title">项目行业分布</h3>
          <div class="industry-content">
            <div class="industry-gauge">
              <div class="gauge-value">12</div>
              <div class="gauge-unit">分钟</div>
            </div>
            <div class="industry-list">
              <div class="industry-item">
                <span class="industry-name">SPAL</span>
                <span class="industry-count">201</span>
              </div>
              <div class="industry-item">
                <span class="industry-name">2201145</span>
                <span class="industry-count">156</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 场馆健康状态面板 -->
        <div class="module-card health-panel">
          <h3 class="panel-title">系统健康状态</h3>
          <div class="health-gauge">
            <div class="gauge-value">95%</div>
            <div class="gauge-ring"></div>
          </div>
          <div class="health-bars">
            <div class="health-bar good"></div>
            <div class="health-bar good"></div>
            <div class="health-bar warning"></div>
            <div class="health-bar good"></div>
          </div>
        </div>
      </div>

      <!-- 中央地图面板 -->
      <div class="center-panel">
        <div class="module-card map-module">
          <div class="map-header">
            <h3 class="panel-title">订单物流分布图</h3>
            <div class="time-selector">
              <el-radio-group v-model="selectedTimeRange" @change="updateMapData" size="small">
                <el-radio-button label="today">今日</el-radio-button>
                <el-radio-button label="week">本周</el-radio-button>
                <el-radio-button label="month">本月</el-radio-button>
              </el-radio-group>
            </div>
          </div>
          <div class="map-container">
            <v-chart class="china-map" :option="mapOption" />
          </div>
          <div class="map-summary">
            <div class="summary-item">
              <div class="summary-value">1,290,900</div>
              <div class="summary-label">总订单数</div>
            </div>
            <div class="summary-item">
              <div class="summary-value">12,457</div>
              <div class="summary-label">今日订单</div>
              <div class="summary-bar"></div>
            </div>
            <div class="summary-item">
              <div class="summary-value">12,457</div>
              <div class="summary-label">本月累计</div>
              <div class="summary-bar"></div>
            </div>
          </div>
          <div class="trend-chart">
            <v-chart class="trend-line" :option="trendChartOption" />
          </div>
        </div>
      </div>

      <!-- 右侧面板 -->
      <div class="right-panel">
        <!-- 用户数据面板 -->
        <div class="module-card user-panel">
          <h3 class="panel-title">用户图表</h3>
          <div class="user-content">
            <div class="user-bars">
              <v-chart class="user-bar-chart" :option="userBarChartOption" />
            </div>
            <div class="user-bottom">
              <div class="user-pie">
                <v-chart class="user-pie-chart" :option="userPieChartOption" />
              </div>
              <div class="user-gauge">
                <div class="gauge-value">78%</div>
                <div class="gauge-ring"></div>
              </div>
            </div>
          </div>
        </div>

        <!-- 导航使用次数面板 -->
        <div class="module-card navigation-panel">
          <h3 class="panel-title">系统使用统计</h3>
          <div class="navigation-content">
            <div class="nav-numbers">
              <div class="nav-item">
                <div class="nav-value">156</div>
                <div class="nav-label">入口</div>
              </div>
              <div class="nav-item">
                <div class="nav-value">89,456</div>
                <div class="nav-label">总计</div>
              </div>
            </div>
            <div class="nav-chart">
              <v-chart class="nav-bar-chart" :option="navBarChartOption" />
            </div>
          </div>
        </div>

        <!-- 及时性面板 -->
        <div class="module-card timeliness-panel">
          <h3 class="panel-title">响应及时性</h3>
          <div class="timeliness-content">
            <div class="timeliness-list">
              <div class="timeliness-item">
                <span class="time-range">0-60分钟</span>
                <span class="time-percent">12%</span>
              </div>
              <div class="timeliness-item">
                <span class="time-range">1-2小时</span>
                <span class="time-percent">18%</span>
              </div>
              <div class="timeliness-item">
                <span class="time-range">2-4小时</span>
                <span class="time-percent">25%</span>
              </div>
              <div class="timeliness-item">
                <span class="time-range">4-8小时</span>
                <span class="time-percent">22%</span>
              </div>
              <div class="timeliness-item">
                <span class="time-range">8小时以上</span>
                <span class="time-percent">23%</span>
              </div>
            </div>
            <div class="timeliness-gauge">
              <div class="gauge-value">85%</div>
              <div class="gauge-ring"></div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部服务概览 -->
    <div class="bottom-panel">
      <div class="service-overview">
        <div class="service-item">
          <div class="service-title">广东</div>
          <div class="service-data">
            <div class="data-row">月订单: 12,310</div>
            <div class="data-row">发货率: 89%</div>
            <div class="data-row">满意度: 95%</div>
          </div>
        </div>
        <div class="service-item">
          <div class="service-title">浙江</div>
          <div class="service-data">
            <div class="data-row">月订单: 9,856</div>
            <div class="data-row">发货率: 92%</div>
            <div class="data-row">满意度: 93%</div>
          </div>
        </div>
        <div class="service-item">
          <div class="service-title">江苏</div>
          <div class="service-data">
            <div class="data-row">月订单: 8,234</div>
            <div class="data-row">发货率: 88%</div>
            <div class="data-row">满意度: 91%</div>
          </div>
        </div>
        <div class="service-item">
          <div class="service-title">山东</div>
          <div class="service-data">
            <div class="data-row">月订单: 6,789</div>
            <div class="data-row">发货率: 85%</div>
            <div class="data-row">满意度: 89%</div>
          </div>
        </div>
        <div class="service-item">
          <div class="service-title">河南</div>
          <div class="service-data">
            <div class="data-row">月订单: 5,432</div>
            <div class="data-row">发货率: 87%</div>
            <div class="data-row">满意度: 90%</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { MapChart, BarChart, LineChart, PieChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  VisualMapComponent,
  GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import * as echarts from 'echarts'
import { dashboardApi, type LogisticsMapData, type DashboardStats } from '@/api/dashboard'

// 注册必须的组件
use([
  CanvasRenderer,
  MapChart,
  BarChart,
  LineChart,
  PieChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  VisualMapComponent,
  GridComponent
])

// 注册中国地图数据
const registerChinaMap = async () => {
  try {
    // 尝试使用在线中国地图数据
    const response = await fetch('https://geo.datav.aliyun.com/areas_v3/bound/100000_full.json')
    if (response.ok) {
      const chinaMapData = await response.json()
      echarts.registerMap('china', chinaMapData)
      console.log('中国地图数据加载成功')
    } else {
      throw new Error('无法获取中国地图数据')
    }
  } catch (error) {
    console.warn('加载中国地图数据失败，使用备用方案:', error)
    // 备用方案：创建一个简化的中国地图数据
    const simpleMapData = {
      type: 'FeatureCollection' as const,
      features: [
        { type: 'Feature' as const, properties: { name: '北京' }, geometry: { type: 'Point' as const, coordinates: [116.4, 39.9] } },
        { type: 'Feature' as const, properties: { name: '上海' }, geometry: { type: 'Point' as const, coordinates: [121.5, 31.2] } },
        { type: 'Feature' as const, properties: { name: '广东' }, geometry: { type: 'Point' as const, coordinates: [113.3, 23.1] } },
        { type: 'Feature' as const, properties: { name: '江苏' }, geometry: { type: 'Point' as const, coordinates: [118.8, 32.0] } },
        { type: 'Feature' as const, properties: { name: '浙江' }, geometry: { type: 'Point' as const, coordinates: [120.2, 30.3] } },
        { type: 'Feature' as const, properties: { name: '山东' }, geometry: { type: 'Point' as const, coordinates: [117.0, 36.7] } },
        { type: 'Feature' as const, properties: { name: '河南' }, geometry: { type: 'Point' as const, coordinates: [113.7, 34.7] } },
        { type: 'Feature' as const, properties: { name: '四川' }, geometry: { type: 'Point' as const, coordinates: [104.1, 30.7] } },
        { type: 'Feature' as const, properties: { name: '湖北' }, geometry: { type: 'Point' as const, coordinates: [114.3, 30.6] } },
        { type: 'Feature' as const, properties: { name: '湖南' }, geometry: { type: 'Point' as const, coordinates: [112.9, 28.2] } },
        { type: 'Feature' as const, properties: { name: '福建' }, geometry: { type: 'Point' as const, coordinates: [119.3, 26.1] } },
        { type: 'Feature' as const, properties: { name: '安徽' }, geometry: { type: 'Point' as const, coordinates: [117.3, 31.9] } },
        { type: 'Feature' as const, properties: { name: '河北' }, geometry: { type: 'Point' as const, coordinates: [114.5, 38.0] } },
        { type: 'Feature' as const, properties: { name: '陕西' }, geometry: { type: 'Point' as const, coordinates: [108.9, 34.3] } },
        { type: 'Feature' as const, properties: { name: '江西' }, geometry: { type: 'Point' as const, coordinates: [115.9, 28.7] } }
      ]
    }
    echarts.registerMap('china', simpleMapData)
    console.log('使用点状地图备用方案')
  }
}

// 响应式数据
const currentTime = ref('')
const currentDate = ref('')
const lastUpdateTime = ref('')
const selectedTimeRange = ref('today')

// 数据状态
const orderStats = ref({
  todayOrders: 0,
  monthOrders: 0
})

const warehouseStats = ref({
  totalInventory: 0,
  lowStockCount: 0
})

const salesStats = ref({
  todaySales: '¥0',
  monthSales: '¥0'
})

const productStats = ref({
  totalProducts: 0,
  hotProducts: 0
})

// 地图数据
const mapData = ref<LogisticsMapData>({})

// 图表配置
const distributionChartOption = ref({
  title: { text: '', show: false },
  tooltip: { trigger: 'axis' },
  grid: { top: 10, right: 10, bottom: 20, left: 40 },
  xAxis: { type: 'category', data: ['广东', '浙江', '江苏', '山东', '河南', '四川'], axisLabel: { color: '#fff', fontSize: 10 } },
  yAxis: { type: 'value', axisLabel: { color: '#fff', fontSize: 10 } },
  series: [{
    data: [26, 20, 17, 14, 10, 13],
    type: 'bar',
    itemStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: '#00ffff' }, { offset: 1, color: '#0066ff' }] } }
  }]
})

const trendChartOption = ref({
  title: { text: '', show: false },
  tooltip: { trigger: 'axis' },
  grid: { top: 10, right: 10, bottom: 20, left: 40 },
  xAxis: { type: 'category', data: ['1月', '2月', '3月', '4月', '5月', '6月'], axisLabel: { color: '#fff', fontSize: 10 } },
  yAxis: { type: 'value', axisLabel: { color: '#fff', fontSize: 10 } },
  series: [{
    data: [820, 932, 901, 934, 1290, 1330],
    type: 'line',
    smooth: true,
    lineStyle: { color: '#00ffff', width: 2 },
    itemStyle: { color: '#00ffff' },
    areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(0,255,255,0.3)' }, { offset: 1, color: 'rgba(0,255,255,0.1)' }] } }
  }]
})

const userBarChartOption = ref({
  title: { text: '', show: false },
  tooltip: { trigger: 'axis' },
  grid: { top: 10, right: 10, bottom: 20, left: 40 },
  xAxis: { type: 'category', data: ['92', '0.5', '十', 'B', '2', '3月', '52', 'He'], axisLabel: { color: '#fff', fontSize: 10 } },
  yAxis: { type: 'value', axisLabel: { color: '#fff', fontSize: 10 } },
  series: [{
    data: [45, 32, 28, 22, 18, 15, 12, 8],
    type: 'bar',
    itemStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: '#ff6b6b' }, { offset: 1, color: '#ee5a24' }] } }
  }]
})

const userPieChartOption = ref({
  title: { text: '', show: false },
  tooltip: { trigger: 'item' },
  series: [{
    type: 'pie',
    radius: ['40%', '70%'],
    data: [
      { value: 44, name: 'A类', itemStyle: { color: '#00ffff' } },
      { value: 26, name: 'B类', itemStyle: { color: '#ff6b6b' } },
      { value: 20, name: 'C类', itemStyle: { color: '#4ecdc4' } },
      { value: 11, name: 'D类', itemStyle: { color: '#45b7d1' } }
    ],
    label: { color: '#fff', fontSize: 10 }
  }]
})

const navBarChartOption = ref({
  title: { text: '', show: false },
  tooltip: { trigger: 'axis' },
  grid: { top: 10, right: 10, bottom: 20, left: 40 },
  xAxis: { type: 'category', data: ['周一', '周二', '周三', '周四', '周五'], axisLabel: { color: '#fff', fontSize: 10 } },
  yAxis: { type: 'value', axisLabel: { color: '#fff', fontSize: 10 } },
  series: [{
    data: [120, 132, 101, 134, 90],
    type: 'bar',
    itemStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: '#4ecdc4' }, { offset: 1, color: '#45b7d1' }] } }
  }]
})

const orderChartOption = ref({
  title: { text: '', show: false },
  tooltip: { trigger: 'axis' },
  grid: { top: 10, right: 10, bottom: 20, left: 40 },
  xAxis: { type: 'category', data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'], axisLabel: { color: '#fff', fontSize: 10 } },
  yAxis: { type: 'value', axisLabel: { color: '#fff', fontSize: 10 } },
  series: [{
    data: [120, 132, 101, 134, 90, 230, 210],
    type: 'line',
    smooth: true,
    lineStyle: { color: '#00ffff', width: 2 },
    itemStyle: { color: '#00ffff' },
    areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(0,255,255,0.3)' }, { offset: 1, color: 'rgba(0,255,255,0.1)' }] } }
  }]
})

const warehouseChartOption = ref({
  title: { text: '', show: false },
  tooltip: { trigger: 'item' },
  series: [{
    type: 'pie',
    radius: ['40%', '70%'],
    data: [
      { value: 1048, name: '正常库存', itemStyle: { color: '#00ff00' } },
      { value: 235, name: '低库存', itemStyle: { color: '#ffaa00' } },
      { value: 580, name: '超储', itemStyle: { color: '#ff0000' } }
    ],
    label: { color: '#fff', fontSize: 10 }
  }]
})

const salesChartOption = ref({
  title: { text: '', show: false },
  tooltip: { trigger: 'axis' },
  grid: { top: 10, right: 10, bottom: 20, left: 40 },
  xAxis: { type: 'category', data: ['1月', '2月', '3月', '4月', '5月', '6月'], axisLabel: { color: '#fff', fontSize: 10 } },
  yAxis: { type: 'value', axisLabel: { color: '#fff', fontSize: 10 } },
  series: [{
    data: [820, 932, 901, 934, 1290, 1330],
    type: 'bar',
    itemStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: '#ff6b6b' }, { offset: 1, color: '#ee5a24' }] } }
  }]
})

const productChartOption = ref({
  title: { text: '', show: false },
  tooltip: { trigger: 'item' },
  series: [{
    type: 'pie',
    radius: '70%',
    data: [
      { value: 335, name: '热销', itemStyle: { color: '#ff6b6b' } },
      { value: 310, name: '正常', itemStyle: { color: '#4ecdc4' } },
      { value: 234, name: '滞销', itemStyle: { color: '#45b7d1' } }
    ],
    label: { color: '#fff', fontSize: 10 }
  }]
})

// 中国地图配置
const mapOption = ref({
  title: {
    text: '',
    show: false
  },
  tooltip: {
    trigger: 'item',
    backgroundColor: 'rgba(0,0,0,0.8)',
    borderColor: '#00ffff',
    textStyle: { color: '#ffffff' },
    formatter: function(params: any) {
      if (params.data) {
        return `${params.name}<br/>
                订单总数: ${params.data.orderCount}<br/>
                已发货: ${params.data.shippedCount}<br/>
                待发货: ${params.data.pendingCount}<br/>
                已送达: ${params.data.deliveredCount}<br/>
                总金额: ¥${params.data.totalAmount}<br/>
                平均配送时间: ${params.data.avgDeliveryTime}小时`
      }
      return params.name
    }
  },
  visualMap: {
    min: 0,
    max: 100,
    text: ['高', '低'],
    realtime: false,
    calculable: true,
    inRange: {
      color: ['#313695', '#4575b4', '#74add1', '#abd9e9', '#e0f3f8', '#ffffcc', '#fee090', '#fdae61', '#f46d43', '#d73027', '#a50026']
    },
    textStyle: { color: '#fff' },
    left: 'left',
    top: 'bottom'
  },
  series: [{
    name: '订单数量',
    type: 'scatter',
    coordinateSystem: 'geo',
    roam: true,
    zoom: 1.2,
    center: [105, 36],
    itemStyle: {
      color: '#00ffff',
      shadowBlur: 10,
      shadowColor: '#00ffff'
    },
    emphasis: {
      itemStyle: {
        color: '#ff6b6b',
        shadowBlur: 20,
        shadowColor: '#ff6b6b'
      }
    },
    data: [
      { name: '北京', value: 85, orderCount: 1250, shippedCount: 980, pendingCount: 270, deliveredCount: 890, totalAmount: '125,000', avgDeliveryTime: 24, coord: [116.4, 39.9] },
      { name: '上海', value: 92, orderCount: 1890, shippedCount: 1560, pendingCount: 330, deliveredCount: 1420, totalAmount: '189,000', avgDeliveryTime: 22, coord: [121.5, 31.2] },
      { name: '广东', value: 78, orderCount: 2340, shippedCount: 1890, pendingCount: 450, deliveredCount: 1670, totalAmount: '234,000', avgDeliveryTime: 26, coord: [113.3, 23.1] },
      { name: '江苏', value: 65, orderCount: 1560, shippedCount: 1230, pendingCount: 330, deliveredCount: 980, totalAmount: '156,000', avgDeliveryTime: 28, coord: [118.8, 32.0] },
      { name: '浙江', value: 72, orderCount: 1780, shippedCount: 1420, pendingCount: 360, deliveredCount: 1180, totalAmount: '178,000', avgDeliveryTime: 25, coord: [120.2, 30.3] },
      { name: '山东', value: 58, orderCount: 1230, shippedCount: 980, pendingCount: 250, deliveredCount: 780, totalAmount: '123,000', avgDeliveryTime: 30, coord: [117.0, 36.7] },
      { name: '河南', value: 45, orderCount: 890, shippedCount: 670, pendingCount: 220, deliveredCount: 520, totalAmount: '89,000', avgDeliveryTime: 32, coord: [113.7, 34.7] },
      { name: '四川', value: 38, orderCount: 670, shippedCount: 520, pendingCount: 150, deliveredCount: 380, totalAmount: '67,000', avgDeliveryTime: 35, coord: [104.1, 30.7] },
      { name: '湖北', value: 52, orderCount: 980, shippedCount: 780, pendingCount: 200, deliveredCount: 580, totalAmount: '98,000', avgDeliveryTime: 29, coord: [114.3, 30.6] },
      { name: '湖南', value: 48, orderCount: 890, shippedCount: 720, pendingCount: 170, deliveredCount: 540, totalAmount: '89,000', avgDeliveryTime: 31, coord: [112.9, 28.2] },
      { name: '福建', value: 55, orderCount: 1020, shippedCount: 820, pendingCount: 200, deliveredCount: 620, totalAmount: '102,000', avgDeliveryTime: 27, coord: [119.3, 26.1] },
      { name: '安徽', value: 42, orderCount: 780, shippedCount: 620, pendingCount: 160, deliveredCount: 460, totalAmount: '78,000', avgDeliveryTime: 33, coord: [117.3, 31.9] },
      { name: '河北', value: 35, orderCount: 650, shippedCount: 520, pendingCount: 130, deliveredCount: 390, totalAmount: '65,000', avgDeliveryTime: 34, coord: [114.5, 38.0] },
      { name: '陕西', value: 32, orderCount: 580, shippedCount: 460, pendingCount: 120, deliveredCount: 340, totalAmount: '58,000', avgDeliveryTime: 36, coord: [108.9, 34.3] },
      { name: '江西', value: 28, orderCount: 520, shippedCount: 410, pendingCount: 110, deliveredCount: 300, totalAmount: '52,000', avgDeliveryTime: 37, coord: [115.9, 28.7] }
    ]
  }]
})

// 加载仪表盘数据
const loadDashboardData = async () => {
  try {
    const stats = await dashboardApi.getDashboardStats()
    
    // 更新统计数据
    orderStats.value = stats.orderStats
    warehouseStats.value = stats.warehouseStats
    salesStats.value = stats.salesStats
    productStats.value = stats.productStats
    
    // 更新图表数据
    orderChartOption.value.series[0].data = stats.orderStats.weekTrend
    salesChartOption.value.series[0].data = stats.salesStats.monthlyTrend.slice(0, 6)
    
    // 更新仓库图表
    warehouseChartOption.value.series[0].data = stats.warehouseStats.stockDistribution.map(item => ({
      value: item.value,
      name: item.name,
      itemStyle: { color: item.color }
    }))
    
    // 更新商品图表
    productChartOption.value.series[0].data = stats.productStats.categoryDistribution.map(item => ({
      value: item.value,
      name: item.name,
      itemStyle: { color: item.color }
    }))
    
  } catch (error) {
    console.error('加载仪表盘数据失败:', error)
  }
}

// 加载地图数据
const loadMapData = async (timeRange: string = 'today') => {
  try {
    const data = await dashboardApi.getLogisticsMapData(timeRange)
    mapData.value = data
    
    // 转换数据格式，添加坐标信息
    const mapChartData = Object.entries(data).map(([province, provinceData]) => ({
      name: province,
      value: provinceData.orderCount,
      orderCount: provinceData.orderCount,
      shippedCount: provinceData.shippedCount,
      pendingCount: provinceData.pendingCount,
      deliveredCount: provinceData.deliveredCount,
      totalAmount: provinceData.totalAmount,
      avgDeliveryTime: provinceData.avgDeliveryTime,
      coord: getProvinceCoordinates(province)
    }))
    
    // 更新地图数据
    mapOption.value.series[0].data = mapChartData
    
    // 更新visualMap的最大值
    const maxValue = Math.max(...mapChartData.map(item => item.value))
    mapOption.value.visualMap.max = maxValue
    
  } catch (error) {
    console.error('加载地图数据失败:', error)
    // 如果API调用失败，使用默认数据
    console.log('使用默认地图数据')
    // 保持现有的默认数据，不需要重新设置
  }
}

// 获取省份坐标的辅助函数
const getProvinceCoordinates = (province: string): [number, number] => {
  const coordinates: Record<string, [number, number]> = {
    '北京': [116.4, 39.9],
    '上海': [121.5, 31.2],
    '广东': [113.3, 23.1],
    '江苏': [118.8, 32.0],
    '浙江': [120.2, 30.3],
    '山东': [117.0, 36.7],
    '河南': [113.7, 34.7],
    '四川': [104.1, 30.7],
    '湖北': [114.3, 30.6],
    '湖南': [112.9, 28.2],
    '福建': [119.3, 26.1],
    '安徽': [117.3, 31.9],
    '河北': [114.5, 38.0],
    '陕西': [108.9, 34.3],
    '江西': [115.9, 28.7]
  }
  return coordinates[province] || [0, 0]
}

// 更新时间
const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', { hour12: false })
  currentDate.value = now.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
  lastUpdateTime.value = now.toLocaleString('zh-CN')
}

// 更新地图数据
const updateMapData = (timeRange: string) => {
  console.log('更新时间范围:', timeRange)
  loadMapData(timeRange)
}

// 定时器
let timeTimer: NodeJS.Timeout

onMounted(async () => {
  updateTime()
  timeTimer = setInterval(updateTime, 1000)
  
  // 确保地图数据已设置
  console.log('地图配置:', mapOption.value)
  console.log('地图数据:', mapOption.value.series[0].data)
  
  // 加载初始数据
  await loadDashboardData()
  await loadMapData('today')
  
  // 再次确认地图数据
  console.log('加载后地图数据:', mapOption.value.series[0].data)
})

onUnmounted(() => {
  if (timeTimer) {
    clearInterval(timeTimer)
  }
})
</script>

<style scoped>
.digital-screen {
  width: 100vw;
  height: 100vh;
  background: linear-gradient(135deg, #0a0e1a 0%, #1a2332 50%, #0a0e1a 100%);
  color: white;
  overflow: hidden;
  font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif;
  position: relative;
}

.digital-screen::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 20%, rgba(0, 255, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 80%, rgba(255, 107, 107, 0.1) 0%, transparent 50%);
  pointer-events: none;
}

/* 顶部标题栏 */
.screen-header {
  height: 80px;
  background: linear-gradient(90deg, rgba(0, 255, 255, 0.15) 0%, rgba(0, 255, 255, 0.05) 50%, rgba(255, 107, 107, 0.15) 100%);
  border-bottom: 2px solid rgba(0, 255, 255, 0.3);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 40px;
  position: relative;
  backdrop-filter: blur(10px);
}

.screen-header::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent 0%, rgba(0, 255, 255, 0.8) 50%, transparent 100%);
}

.header-left .main-title {
  font-size: 28px;
  font-weight: bold;
  background: linear-gradient(45deg, #00ffff, #ff6b6b);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 0 20px rgba(0, 255, 255, 0.5);
  margin: 0;
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
}

.user-counter {
  text-align: center;
}

.counter-label {
  font-size: 14px;
  color: #cccccc;
  margin-bottom: 8px;
  display: block;
}

.counter-digits {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.digit {
  width: 40px;
  height: 50px;
  background: linear-gradient(135deg, #1a2332 0%, #2a3442 100%);
  border: 2px solid rgba(0, 255, 255, 0.5);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
  color: #00ffff;
  text-shadow: 0 0 10px rgba(0, 255, 255, 0.8);
  box-shadow: 0 0 20px rgba(0, 255, 255, 0.3);
}

.header-right .time-info {
  text-align: right;
}

.current-date {
  font-size: 16px;
  color: #cccccc;
  margin-bottom: 4px;
}

.current-time {
  font-size: 24px;
  font-weight: bold;
  color: #00ffff;
  text-shadow: 0 0 10px rgba(0, 255, 255, 0.5);
}

/* 主要内容区域 */
.screen-content {
  display: flex;
  height: calc(100vh - 180px);
  padding: 20px;
  gap: 20px;
}

.left-panel, .right-panel {
  width: 380px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.center-panel {
  flex: 1;
}

/* 模块卡片 */
.module-card {
  background: linear-gradient(135deg, rgba(0, 255, 255, 0.08) 0%, rgba(0, 255, 255, 0.03) 100%);
  border: 1px solid rgba(0, 255, 255, 0.3);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(15px);
  position: relative;
  overflow: hidden;
}

.module-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, #00ffff, #ff6b6b);
}

.panel-title {
  margin: 0 0 20px 0;
  font-size: 16px;
  color: #00ffff;
  text-align: center;
  font-weight: 600;
  text-shadow: 0 0 10px rgba(0, 255, 255, 0.5);
}

/* 进度指标面板 */
.progress-container {
  display: flex;
  justify-content: space-around;
  gap: 20px;
}

.progress-item {
  text-align: center;
  flex: 1;
}

.progress-ring {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  border: 4px solid rgba(0, 255, 255, 0.3);
  border-top: 4px solid #00ffff;
  margin: 0 auto 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  animation: rotate 2s linear infinite;
}

@keyframes rotate {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.progress-value {
  font-size: 18px;
  font-weight: bold;
  color: #00ffff;
}

.progress-label {
  font-size: 12px;
  color: #cccccc;
  margin-top: 2px;
}

.progress-number {
  font-size: 24px;
  font-weight: bold;
  color: #00ffff;
  text-shadow: 0 0 10px rgba(0, 255, 255, 0.5);
}

/* 区域分布面板 */
.chart-container {
  height: 120px;
  margin-bottom: 15px;
}

.distribution-chart {
  width: 100%;
  height: 100%;
}

.data-summary {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 14px;
}

.summary-icon {
  font-size: 16px;
}

.summary-count {
  color: #00ffff;
  font-weight: bold;
}

/* 项目行业分布面板 */
.industry-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.industry-gauge {
  text-align: center;
  flex: 1;
}

.gauge-value {
  font-size: 32px;
  font-weight: bold;
  color: #00ffff;
  text-shadow: 0 0 15px rgba(0, 255, 255, 0.8);
}

.gauge-unit {
  font-size: 14px;
  color: #cccccc;
}

.industry-list {
  flex: 1;
}

.industry-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  padding: 8px;
  background: rgba(0, 255, 255, 0.1);
  border-radius: 6px;
  border-left: 3px solid #00ffff;
}

.industry-name {
  color: #ffffff;
  font-size: 14px;
}

.industry-count {
  color: #00ffff;
  font-weight: bold;
}

/* 场馆健康状态面板 */
.health-gauge {
  text-align: center;
  margin-bottom: 20px;
  position: relative;
}

.health-gauge .gauge-value {
  font-size: 36px;
  font-weight: bold;
  color: #00ff00;
  text-shadow: 0 0 15px rgba(0, 255, 0, 0.8);
  z-index: 2;
  position: relative;
}

.health-gauge .gauge-ring {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100px;
  height: 100px;
  border-radius: 50%;
  border: 6px solid rgba(0, 255, 0, 0.3);
  border-top: 6px solid #00ff00;
  animation: rotate 3s linear infinite;
}

.health-bars {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.health-bar {
  width: 20px;
  height: 40px;
  border-radius: 4px;
  background: linear-gradient(to top, #00ff00, #00cc00);
  box-shadow: 0 0 10px rgba(0, 255, 0, 0.5);
}

.health-bar.warning {
  background: linear-gradient(to top, #ffaa00, #ff8800);
  box-shadow: 0 0 10px rgba(255, 170, 0, 0.5);
}

/* 地图模块 */
.map-module {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.map-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.time-selector {
  margin-left: 20px;
}

.map-container {
  flex: 1;
  position: relative;
  margin-bottom: 20px;
}

.china-map {
  width: 100%;
  height: 100%;
}

/* 地图摘要 */
.map-summary {
  display: flex;
  justify-content: space-around;
  margin-bottom: 20px;
  gap: 20px;
}

.summary-item {
  text-align: center;
  flex: 1;
}

.summary-value {
  font-size: 24px;
  font-weight: bold;
  color: #00ffff;
  text-shadow: 0 0 10px rgba(0, 255, 255, 0.5);
  margin-bottom: 5px;
}

.summary-label {
  font-size: 12px;
  color: #cccccc;
  margin-bottom: 8px;
}

.summary-bar {
  width: 100%;
  height: 4px;
  background: rgba(0, 255, 255, 0.3);
  border-radius: 2px;
  position: relative;
  overflow: hidden;
}

.summary-bar::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  width: 70%;
  background: linear-gradient(90deg, #00ffff, #0066ff);
  border-radius: 2px;
  animation: progress 2s ease-out;
}

@keyframes progress {
  0% { width: 0%; }
  100% { width: 70%; }
}

.trend-chart {
  height: 80px;
}

.trend-line {
  width: 100%;
  height: 100%;
}

/* 用户数据面板 */
.user-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.user-bars {
  height: 100px;
}

.user-bar-chart {
  width: 100%;
  height: 100%;
}

.user-bottom {
  display: flex;
  gap: 15px;
}

.user-pie {
  flex: 1;
  height: 80px;
}

.user-pie-chart {
  width: 100%;
  height: 100%;
}

.user-gauge {
  flex: 1;
  text-align: center;
  position: relative;
}

.user-gauge .gauge-value {
  font-size: 24px;
  font-weight: bold;
  color: #ff6b6b;
  text-shadow: 0 0 10px rgba(255, 107, 107, 0.5);
  z-index: 2;
  position: relative;
}

.user-gauge .gauge-ring {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 60px;
  height: 60px;
  border-radius: 50%;
  border: 4px solid rgba(255, 107, 107, 0.3);
  border-top: 4px solid #ff6b6b;
  animation: rotate 2s linear infinite;
}

/* 导航使用次数面板 */
.navigation-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.nav-numbers {
  display: flex;
  justify-content: space-around;
  gap: 20px;
}

.nav-item {
  text-align: center;
  flex: 1;
}

.nav-value {
  font-size: 24px;
  font-weight: bold;
  color: #4ecdc4;
  text-shadow: 0 0 10px rgba(78, 205, 196, 0.5);
  margin-bottom: 5px;
}

.nav-label {
  font-size: 12px;
  color: #cccccc;
}

.nav-chart {
  height: 60px;
}

.nav-bar-chart {
  width: 100%;
  height: 100%;
}

/* 及时性面板 */
.timeliness-content {
  display: flex;
  gap: 15px;
}

.timeliness-list {
  flex: 1;
}

.timeliness-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  padding: 6px 10px;
  background: rgba(0, 255, 255, 0.1);
  border-radius: 6px;
  border-left: 2px solid #00ffff;
}

.time-range {
  color: #ffffff;
  font-size: 12px;
}

.time-percent {
  color: #00ffff;
  font-weight: bold;
  font-size: 12px;
}

.timeliness-gauge {
  flex: 1;
  text-align: center;
  position: relative;
}

.timeliness-gauge .gauge-value {
  font-size: 20px;
  font-weight: bold;
  color: #00ffff;
  text-shadow: 0 0 10px rgba(0, 255, 255, 0.5);
  z-index: 2;
  position: relative;
}

.timeliness-gauge .gauge-ring {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 50px;
  height: 50px;
  border-radius: 50%;
  border: 3px solid rgba(0, 255, 255, 0.3);
  border-top: 3px solid #00ffff;
  animation: rotate 2.5s linear infinite;
}

/* 底部服务概览 */
.bottom-panel {
  height: 120px;
  padding: 0 20px 20px;
}

.service-overview {
  display: flex;
  gap: 20px;
  height: 100%;
}

.service-item {
  flex: 1;
  background: linear-gradient(135deg, rgba(0, 255, 255, 0.08) 0%, rgba(0, 255, 255, 0.03) 100%);
  border: 1px solid rgba(0, 255, 255, 0.3);
  border-radius: 8px;
  padding: 15px;
  backdrop-filter: blur(10px);
  position: relative;
  overflow: hidden;
}

.service-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, #00ffff, #ff6b6b);
}

.service-title {
  font-size: 16px;
  font-weight: bold;
  color: #00ffff;
  text-align: center;
  margin-bottom: 10px;
  text-shadow: 0 0 10px rgba(0, 255, 255, 0.5);
}

.service-data {
  font-size: 12px;
}

.data-row {
  color: #cccccc;
  margin-bottom: 4px;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 1400px) {
  .left-panel, .right-panel {
    width: 350px;
  }
  
  .main-title {
    font-size: 24px;
  }
  
  .digit {
    width: 35px;
    height: 45px;
    font-size: 20px;
  }
}

@media (max-width: 1200px) {
  .left-panel, .right-panel {
    width: 320px;
  }
  
  .screen-content {
    padding: 15px;
    gap: 15px;
  }
  
  .service-overview {
    gap: 15px;
  }
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: rgba(0, 255, 255, 0.1);
  border-radius: 3px;
}

::-webkit-scrollbar-thumb {
  background: rgba(0, 255, 255, 0.5);
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 255, 255, 0.7);
}
</style>
