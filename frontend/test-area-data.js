// 测试 china-area-data 包
import pkg from 'china-area-data'
const { pcaa } = pkg

console.log('=== 省份数据测试 ===')
console.log('省份总数:', Object.keys(pcaa['86']).length)
console.log('前5个省份:')
Object.keys(pcaa['86']).slice(0, 5).forEach(code => {
  console.log(`  ${code}: ${pcaa['86'][code]}`)
})

console.log('\n=== 城市数据测试 ===')
const beijingCode = '110000'
if (pcaa[beijingCode]) {
  console.log('北京市的城市:')
  Object.keys(pcaa[beijingCode]).forEach(code => {
    console.log(`  ${code}: ${pcaa[beijingCode][code]}`)
  })
}

console.log('\n=== 区县数据测试 ===')
const beijingCityCode = '110100'
if (pcaa[beijingCityCode]) {
  console.log('北京市的区县:')
  Object.keys(pcaa[beijingCityCode]).forEach(code => {
    console.log(`  ${code}: ${pcaa[beijingCityCode][code]}`)
  })
}
